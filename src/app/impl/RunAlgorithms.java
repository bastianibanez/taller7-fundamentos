package app.impl;

import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class RunAlgorithms {
    public RunAlgorithms(int dimension) {
        //Valores comunes
        RandomGrid g = new RandomGrid(dimension);
        System.out.println(dimension);
        g.showGrid(); // Consider commenting out for performance runs with many iterations
        ShortestPathProblem problem = new MyShortestPathProblem(g.get(), 0, dimension - 1);

        //Algoritmo secuencial
        long start_secuencial_ms = System.currentTimeMillis();
        long start_secuencial_nano = System.nanoTime();
        // Operate on a copy of the problem
        SequentialShortestPath solution = new SequentialShortestPath(problem.copy());
        List<Integer> bestPath = solution.findShortestPath();
        long end_secuencial_ms = System.currentTimeMillis();
        long end_secuencial_nano = System.nanoTime();

        //Algoritmo paralelo
        long start_paralelo_ms = System.currentTimeMillis();
        long start_paralelo_nano = System.nanoTime();
        int initialBestValue = Integer.MAX_VALUE;
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        // Operate on a copy of the problem for the root parallel task
        ParallelShortestPath parallelSolution = new ParallelShortestPath(problem.copy(), initialBestValue);
        List<Integer> bestParallelPath = forkJoinPool.invoke(parallelSolution);
        long end_paralelo_ms = System.currentTimeMillis();
        long end_paralelo_nano = System.nanoTime();

        long time_secuencial_ms = end_secuencial_ms - start_secuencial_ms;
        long time_secuencial_nano = end_secuencial_nano - start_secuencial_nano;
        long time_paralelo_ms = end_paralelo_ms - start_paralelo_ms;
        long time_paralelo_nano = end_paralelo_nano - start_paralelo_nano;

        if (bestPath != null) {
            System.out.println("Camino mas corto (secuencial): " + bestPath);
            System.out.println("Longitud del camino: " + bestPath.size());
            System.out.println("Tiempo: " + time_secuencial_ms + "ms");
            System.out.println("Tiempo: " + time_secuencial_nano + "ms");
        } else {
            System.out.println("No se encontró un camino válido (secuencial)");
        }

        if (bestParallelPath != null) {
            System.out.println("Camino mas corto (paralelo): " + bestParallelPath);
            System.out.println("Longitud del camino: " + bestParallelPath.size());
            System.out.println("Tiempo: " + time_paralelo_ms + "ms");
            System.out.println("Tiempo: " + time_paralelo_nano + "ms");
        } else {
            System.out.println("No se encontró un camino válido (paralelo)");
        }

        // Comparing execution times
        if (bestPath != null && bestParallelPath != null) { // Only compare if both found paths
            if (time_paralelo_nano > 0 && time_secuencial_nano > 0) { // Avoid division by zero
                double rendimiento = (double)time_secuencial_nano/time_paralelo_nano;
                System.out.println("Rendimiento paralelo: x" + rendimiento);

                }
            }
        System.out.println();
    }
}