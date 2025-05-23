package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference; // Importado

public class ParallelShortestPath extends RecursiveTask<Void> { // MODIFICADO a Void
    private final ShortestPathProblem problem;
    private final AtomicInteger sharedBestPathWeight;
    private final AtomicReference<List<Integer>> sharedBestPathRef; // AÑADIDO
    private final int currentDepth;

    private static final int SEQUENTIAL_THRESHOLD_DEPTH = 3;

    public ParallelShortestPath(ShortestPathProblem problemToCopy,
                                AtomicInteger initialSharedBestPathWeight,
                                AtomicReference<List<Integer>> bestPathRef) { // AÑADIDO bestPathRef
        this.problem = problemToCopy;
        this.sharedBestPathWeight = initialSharedBestPathWeight;
        this.sharedBestPathRef = bestPathRef; // AÑADIDO
        this.currentDepth = 0;
    }

    private ParallelShortestPath(ShortestPathProblem problemStateForThisTask,
                                 AtomicInteger sharedBestPathWeight,
                                 AtomicReference<List<Integer>> bestPathRef, // AÑADIDO
                                 int depth) {
        this.problem = problemStateForThisTask;
        this.sharedBestPathWeight = sharedBestPathWeight;
        this.sharedBestPathRef = bestPathRef; // AÑADIDO
        this.currentDepth = depth;
    }

    /**
     * Intenta actualizar el mejor peso global y el camino correspondiente.
     * Solo actualiza el camino si el peso es estrictamente mejor.
     */
    private void attemptUpdateGlobalBest(List<Integer> currentLocalPath, int currentLocalWeight) {
        int currentGlobalBestWeightValue;
        while (true) {
            currentGlobalBestWeightValue = sharedBestPathWeight.get();
            if (currentLocalWeight < currentGlobalBestWeightValue) {
                // Intenta actualizar el peso. Si tiene éxito, actualiza el camino.
                if (sharedBestPathWeight.compareAndSet(currentGlobalBestWeightValue, currentLocalWeight)) {
                    sharedBestPathRef.set(new ArrayList<>(currentLocalPath)); // Almacena una copia
                    return; // Actualización exitosa
                }
                // Si CAS falló para el peso, otro hilo actualizó. Reintentar el bucle.
            } else {
                // El peso actual no es estrictamente mejor, no hacer nada con el camino.
                return;
            }
        }
    }

    @Override
    protected Void compute() {
        // Poda si el camino actual ya es peor que el mejor global conocido
        if (problem.getCurrentPathWeight() >= sharedBestPathWeight.get() && problem.getCurrentPath().size() > 1) {
            return null;
        }

        if (problem.isSolution()) {
            // Se encontró una solución, intentar actualizar el mejor global.
            attemptUpdateGlobalBest(problem.getCurrentPath(), problem.getCurrentPathWeight());
            return null;
        }

        List<Integer> possibleMoves = problem.getPossibleMoves();

        if (currentDepth >= SEQUENTIAL_THRESHOLD_DEPTH) {
            for (int move : possibleMoves) {
                problem.applyMove(move);
                if (problem.getCurrentPathWeight() < sharedBestPathWeight.get()) {
                    compute(); // Llamada recursiva, el valor de retorno no se usa para el camino
                }
                problem.undoMove(move);
            }
        } else {
            List<ParallelShortestPath> tasks = new ArrayList<>();
            for (int move : possibleMoves) {
                ShortestPathProblem problemForChild = this.problem.copy();
                problemForChild.applyMove(move);

                if (problemForChild.getCurrentPathWeight() < sharedBestPathWeight.get()) {
                    ParallelShortestPath childTask = new ParallelShortestPath(
                            problemForChild,
                            this.sharedBestPathWeight,
                            this.sharedBestPathRef, // Pasar la referencia
                            currentDepth + 1
                    );
                    tasks.add(childTask);
                    childTask.fork();
                }
            }
            for (ParallelShortestPath task : tasks) {
                task.join(); // Esperar a que las tareas hijas terminen
            }
        }
        return null; // Tipo de retorno es Void
    }
}