package app;

import app.impl.ParallelShortestPath;
import app.impl.SequentialShortestPath;
import app.impl.MyShortestPathProblem;
import app.impl.RandomGrid;
import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        int dimension = 11;
        RandomGrid g = new RandomGrid(dimension);

        ShortestPathProblem problem = new MyShortestPathProblem(g.get(), 0, dimension - 1);

        SequentialShortestPath solution = new SequentialShortestPath(problem);
        List<Integer> bestPath = solution.findShortestPath();

        int initialBestValue = Integer.MAX_VALUE;
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        ParallelShortestPath parallelSolution = new ParallelShortestPath(problem, initialBestValue);

        List<Integer> bestParallelPath = forkJoinPool.invoke(parallelSolution);

        if (bestPath != null){
            System.out.println("Camino mas corto (secuencial): " + bestPath);
            System.out.println("Longitud del camino: " + bestPath.size());
        } else {
            System.out.println("No se encontró un camino válido");
        }

        if (bestParallelPath != null){
            System.out.println("Camino mas corto (paralelo): " + bestParallelPath);
            System.out.println("Longitud del camino: " + bestParallelPath.size());
        } else {
            System.out.println("No se encontró un camino válido");
        }
    }
}