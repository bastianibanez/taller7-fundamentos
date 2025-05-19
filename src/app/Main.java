package app;

import app.impl.MyShortestPathProblem;
import app.impl.ParallelShortestPath;
import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        int[][] sampleGrid = {
                {1,0,1,0},
                {1,1,1,0},
                {0,0,1,1},
                {0,1,0,1}
        };

        ShortestPathProblem problem = new MyShortestPathProblem(sampleGrid, 0, 0, 3, 3);
        int initialBestPathLength = Integer.MAX_VALUE;

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        ParallelShortestPath parallelShortestPath = new ParallelShortestPath(problem, initialBestPathLength);
        List<Integer> shortestPath = forkJoinPool.invoke(parallelShortestPath);

        if (shortestPath != null){
            System.out.println("Shortest path: " + shortestPath);
        } else {
            System.out.println("No path found");
        }

    }
}
