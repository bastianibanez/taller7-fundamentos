package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;

public class SequentialShortestPath {
    private final ShortestPathProblem problem;
    private int bestPathLength;
    private List<Integer> bestPath;

    public SequentialShortestPath(ShortestPathProblem problem){
        this.problem = problem;
        this.bestPathLength = Integer.MAX_VALUE;
        this.bestPath = null;
    }

    public List<Integer> findShortestPath(){
        solve();
        return bestPath;
    }

    private void solve(){
        if (problem.isSolution()){
            List<Integer> currentPath = problem.getCurrentPath();
            if (bestPath == null || currentPath.size() < bestPath.size()){
                bestPath = new ArrayList<>(currentPath);
                bestPathLength = currentPath.size();
            }
            return;
        }

        List<Integer> moves = problem.getPossibleMoves();
        for (int move:moves){
            problem.applyMove(move);
            if (problem.getCurrentPathLength() < bestPathLength){
                solve();
            }
            problem.undoMove(move);
        }
    }

    public static List<Integer> findShortestPath(ShortestPathProblem problem){
        SequentialShortestPath solver = new SequentialShortestPath(problem);
        return solver.findShortestPath();
    }
}