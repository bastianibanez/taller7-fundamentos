package app;

import app.impl.MyShortestPathProblem;
import app.impl.ParallelShortestPath;
import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        int[][] graph = {
                {0,1,4,0},
                {0,0,2,6},
                {0,0,0,3},
                {0,0,0,0},
        };

        int initialBestPathLength = Integer.MAX_VALUE;
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        int start = 0;
        int end = 3;

        ShortestPathProblem problem = new MyShortestPathProblem(graph, start, end);
        ParallelShortestPath solution = new ParallelShortestPath(problem, initialBestPathLength);

        List<Integer> shortestPath = forkJoinPool.invoke(solution);

        if (shortestPath != null){
            System.out.println("Camino mas corto: " + shortestPath);
            System.out.println("Longitud del camino: " + shortestPath.size());
        } else {
            System.out.println("No se encontro un camino valido");
        }
    }
}