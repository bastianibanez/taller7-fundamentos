package app.impl;

import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class RunAlgorithms {
    public RunAlgorithms(int dimension) {
        //Valores comunes
        RandomGrid g = new RandomGrid(dimension);
        System.out.println(dimension);
        g.showGrid();
        ShortestPathProblem problem = new MyShortestPathProblem(g.get(), 0, dimension - 1);

        //Algoritmo secuencial
        long start_secuencial = System.nanoTime();
        long start_secuencial_ms = System.currentTimeMillis();
        SequentialShortestPath solution = new SequentialShortestPath(problem);
        List<Integer> bestPath = solution.findShortestPath();
        long end_secuencial = System.nanoTime();
        long end_secuencial_ms = System.currentTimeMillis();

        //Algoritmo paralelo
        long start_paralelo = System.nanoTime();
        long start_paralelo_ms = System.currentTimeMillis();
        int initialBestValue = Integer.MAX_VALUE;
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        ParallelShortestPath parallelSolution = new ParallelShortestPath(problem, initialBestValue);
        List<Integer> bestParallelPath = forkJoinPool.invoke(parallelSolution);
        long end_paralelo = System.nanoTime();
        long end_paralelo_ms = System.currentTimeMillis();

        // deltaTiempo en nanosegundos
        long time_secuencial = end_secuencial - start_secuencial;
        long time_paralelo = end_paralelo - start_paralelo;

        long time_secuencial_ms = end_secuencial_ms - start_secuencial_ms;
        long time_paralelo_ms = end_paralelo_ms - start_paralelo_ms;

        if (bestPath != null) {
            System.out.println("Camino mas corto (secuencial): " + bestPath);
            System.out.println("Longitud del camino: " + bestPath.size());
            System.out.println("Tiempo: " + time_secuencial_ms + "ms (" + time_secuencial + "ns)");
        } else {
            System.out.println("No se encontró un camino válido");
        }

        if (bestParallelPath != null) {
            System.out.println("Camino mas corto (paralelo): " + bestParallelPath);
            System.out.println("Longitud del camino: " + bestParallelPath.size());
            System.out.println("Tiempo: " + time_paralelo_ms + "ms (" + time_paralelo + "ns)");
        } else {
            System.out.println("No se encontró un camino válido");
        }

        if (time_paralelo > time_secuencial){
            long time_ratio  = ((time_paralelo/time_secuencial) - 1) * 100;
            System.out.println("Tiempo paralelo (+" + time_ratio + ")%");
            System.out.println();
        }

        if (time_paralelo < time_secuencial){
            long time_ratio  = ((time_secuencial/time_paralelo) - 1) * 100;
            System.out.println("Tiempo paralelo (-" + time_ratio + ")%");
            System.out.println();
        }

    }
}