package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;

public class SequentialShortestPath {
    private final ShortestPathProblem problem;
    private int bestPathWeight; // Changed from bestPathLength to bestPathWeight
    private List<Integer> bestPath;

    public SequentialShortestPath(ShortestPathProblem problem){
        this.problem = problem;
        this.bestPathWeight = Integer.MAX_VALUE; // Initialize with max value for weight
        this.bestPath = null;
    }

    public List<Integer> findShortestPath(){
        solve();
        return bestPath;
    }

    // Helper to get the weight of the best path found so far
    public int getBestPathWeight() {
        return bestPathWeight;
    }

    private void solve(){
        if (problem.isSolution()){
            int currentWeight = problem.getCurrentPathWeight(); // Get current path's weight
            if (bestPath == null || currentWeight < bestPathWeight){ // Compare weights
                bestPath = new ArrayList<>(problem.getCurrentPath());
                bestPathWeight = currentWeight; // Store weight
            }
            return;
        }

        List<Integer> moves = problem.getPossibleMoves();
        for (int move:moves){
            problem.applyMove(move);
            // Prune if current path's weight is already not better than the best found
            if (problem.getCurrentPathWeight() < bestPathWeight){
                solve();
            }
            problem.undoMove(move);
        }
    }

    // Static helper might need adjustment if external callers need the weight
    public static List<Integer> findShortestPath(ShortestPathProblem problem){
        SequentialShortestPath solver = new SequentialShortestPath(problem);
        return solver.findShortestPath();
    }
}