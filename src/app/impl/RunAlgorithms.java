package app.impl;

import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RunAlgorithms {

    public RunAlgorithms(int dimension) {
        RandomGrid g = new RandomGrid(dimension);
        System.out.println("Graph: " + dimension + " nodes");

        ShortestPathProblem problem = new MyShortestPathProblem(g.get(), 0, dimension - 1);
        runAndPrint(problem, dimension);
    }

    private void runAndPrint(ShortestPathProblem problemInstance, int problemSizeContext) {
        long start_secuencial_ms = System.currentTimeMillis();
        SequentialShortestPath solution = new SequentialShortestPath(problemInstance.copy());
        List<Integer> bestPathSequential = solution.findShortestPath();
        int bestWeightSequential = solution.getBestPathWeight();
        long end_secuencial_ms = System.currentTimeMillis();
        AtomicInteger sharedBestParallelPathWeight = new AtomicInteger(Integer.MAX_VALUE);

        long start_paralelo_ms = System.currentTimeMillis();
        AtomicReference<List<Integer>> sharedBestParallelPath = new AtomicReference<>(null);
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        ParallelShortestPath parallelSolution = new ParallelShortestPath(
                problemInstance.copy(),
                sharedBestParallelPathWeight,
                sharedBestParallelPath
        );
        forkJoinPool.invoke(parallelSolution);

        List<Integer> bestPathParallel = sharedBestParallelPath.get();
        int bestWeightParallel = sharedBestParallelPathWeight.get();
        long end_paralelo_ms = System.currentTimeMillis();

        long time_secuencial_ms = end_secuencial_ms - start_secuencial_ms;
        long time_paralelo_ms = end_paralelo_ms - start_paralelo_ms;

        if (bestPathSequential != null) {
            System.out.println("Camino mas corto (secuencial): " + bestPathSequential);
            System.out.println("Costo del camino (secuencial): " + bestWeightSequential);
            System.out.println("Nodos en el camino (secuencial): " + bestPathSequential.size());
            System.out.println("Tiempo: " + time_secuencial_ms + "ms");
        } else {
            System.out.println("No se encontró un camino válido (secuencial)");
        }

        if (bestPathParallel != null) {
            System.out.println("Camino mas corto (paralelo): " + bestPathParallel);
            System.out.println("Costo del camino (paralelo): " + bestWeightParallel);
            System.out.println("Nodos en el camino (paralelo): " + bestPathParallel.size());
            System.out.println("Tiempo: " + time_paralelo_ms + "ms");
        } else {
            if (bestWeightParallel < Integer.MAX_VALUE) {
                System.out.println("Se encontró un costo (paralelo): " + bestWeightParallel + ", pero no se pudo determinar la lista del camino (error interno o sin camino)."); //
            } else {
                System.out.println("No se encontró un camino válido (paralelo)");
            }
        }

        if (bestPathSequential != null && bestPathParallel != null) {
            if (time_paralelo_ms > 0 && time_secuencial_ms > 0) {
                if (time_paralelo_ms > time_secuencial_ms) {
                    double ratio = ((double) time_paralelo_ms / time_secuencial_ms - 1) * 100;
                    System.out.printf("Tiempo paralelo (+%.2f)%%\n", ratio);
                } else if (time_paralelo_ms < time_secuencial_ms) {
                    double ratio = ((double) time_secuencial_ms / time_paralelo_ms - 1) * 100;
                    System.out.printf("Tiempo paralelo (-%.2f)%%\n", ratio);
                } else {
                    System.out.println("Tiempo paralelo y secuencial son iguales.");
                }
            }
        }
        System.out.println();
    }
}