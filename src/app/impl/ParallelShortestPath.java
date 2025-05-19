package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class ParallelShortestPath extends RecursiveTask<List<Integer>> {
    private final ShortestPathProblem problem;
    private int bestPathLength;

    public ParallelShortestPath(ShortestPathProblem problem, int bestPathLength){
        this.problem = problem;
        this.bestPathLength = bestPathLength;
    }

    @Override
    protected List<Integer> compute(){
        if (problem.isSolution()){
            return problem.getCurrentPath();
        }

        List<Integer> shortestPath = null;
        List<Integer> moves = problem.getPossibleMoves();
        List<ParallelShortestPath> tasks = new ArrayList<>();

        for (int move:moves){
            problem.applyMove(move);

            if (problem.getCurrentPathLength() < bestPathLength){
                ParallelShortestPath child = new ParallelShortestPath(problem, bestPathLength);
                tasks.add(child);
            }
            problem.undoMove(move);
        }
        for (ParallelShortestPath task:tasks){
            task.fork();
        }

        for (ParallelShortestPath task:tasks){
            List<Integer> path = task.join();

            if (path != null && (shortestPath == null || path.size() < shortestPath.size())){
                shortestPath = path;
                bestPathLength = path.size();
            }
        }
        return shortestPath;
    }
}
