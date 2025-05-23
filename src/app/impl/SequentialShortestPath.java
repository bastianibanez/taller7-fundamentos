package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;

public class SequentialShortestPath {
    private final ShortestPathProblem problem;
    private int bestPathWeight;
    private List<Integer> bestPath;

    public SequentialShortestPath(ShortestPathProblem problem){
        this.problem = problem;
        this.bestPathWeight = Integer.MAX_VALUE;
        this.bestPath = null;
    }

    public List<Integer> findShortestPath(){
        solve();
        return bestPath;
    }

    public int getBestPathWeight() {
        return bestPathWeight;
    }

    private void solve(){
        if (problem.isSolution()){
            int currentWeight = problem.getCurrentPathWeight();
            if (bestPath == null || currentWeight < bestPathWeight){
                bestPath = new ArrayList<>(problem.getCurrentPath());
                bestPathWeight = currentWeight;
            }
            return;
        }

        List<Integer> moves = problem.getPossibleMoves();
        for (int move:moves){
            problem.applyMove(move);
            if (problem.getCurrentPathWeight() < bestPathWeight){
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