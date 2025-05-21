package app.impl;

import app.service.ShortestPathProblem;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class MyShortestPathProblem implements ShortestPathProblem {
    private final int[][] graph;
    private final int start;
    private final int end;

    private int currentLength = 0; // This correctly stores the sum of weights
    private List<Integer> path = new ArrayList<>();
    private Set<Integer> visited = new HashSet<>();

    // Existing private constructor for copy optimization (if you added it)
    private MyShortestPathProblem(int[][] graph, int startNode, int endNode, boolean initializeCollections) {
        this.graph = graph;
        this.start = startNode;
        this.end = endNode;

        if (initializeCollections) {
            this.path = new ArrayList<>();
            this.path.add(startNode);
            this.visited = new HashSet<>();
            this.visited.add(startNode);
        } else {
            this.path = new ArrayList<>();
            this.visited = new HashSet<>();
        }
    }


    public MyShortestPathProblem(int[][] graph, int start, int end) {
        this(graph, start, end, true);
    }

    @Override
    public boolean isSolution() {
        return !path.isEmpty() && path.getLast() == end;
    }

    @Override
    public void applyMove(int move) {
        if (path.isEmpty()) { // Should not happen if start node is added in constructor
            return;
        }

        int from = path.getLast();
        currentLength += graph[from][move]; // currentLength accumulates weights
        path.add(move);
        visited.add(move);
    }

    @Override
    public void undoMove(int move) {
        if (path.size() <= 1) { // Path should at least contain the start node
            return;
        }

        int to = path.getLast();
        path.removeLast();
        visited.remove(to);

        if (!path.isEmpty()) {
            int from = path.getLast();
            currentLength -= graph[from][to];
        } else {
            currentLength = 0;
        }
    }

    @Override
    public List<Integer> getPossibleMoves() {
        if (path.isEmpty()) return new ArrayList<>();

        int from = path.getLast();
        List<Integer> moves = new ArrayList<>();

        for (int to = 0; to < graph.length; to++) {
            if (graph[from][to] > 0 && !visited.contains(to)) {
                moves.add(to);
            }
        }
        return moves;
    }

    @Override
    public List<Integer> getCurrentPath() {
        return path;
    }

    @Override
    public int getCurrentPathLength() {
        return path.size();
    }

    @Override
    public int getCurrentPathWeight() {
        return currentLength;
    }

    @Override
    public MyShortestPathProblem copy() {
        MyShortestPathProblem problemCopy = new MyShortestPathProblem(this.graph, this.start, this.end, false);
        problemCopy.path = new ArrayList<>(this.path);
        problemCopy.visited = new HashSet<>(this.visited);
        problemCopy.currentLength = this.currentLength;
        return problemCopy;
    }
}