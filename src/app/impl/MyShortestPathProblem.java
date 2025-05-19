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

    private int currentLength = 0;
    private List<Integer> path = new ArrayList<>();
    private Set<Integer> visited = new HashSet<>();

    public MyShortestPathProblem(int[][] graph, int start, int end) {
        this.graph = graph;
        this.start = start;
        this.end = end;

        path.add(start);
        visited.add(start);
    }

    @Override
    public boolean isSolution() {
        return path.getLast() == end;
    }

    @Override
    public void applyMove(int move) {
        if (path.isEmpty()) {
            return;
        }

        int from = path.getLast();
        currentLength += graph[from][move];
        path.add(move);
        visited.add(move);
    }

    @Override
    public void undoMove(int move) {
        if (path.size() <= 1) {
            return;
        }

        int to = path.getLast();
        path.removeLast();
        visited.remove(to);
        int from = path.getLast();
        currentLength -= graph[from][to];
    }

    @Override
    public List<Integer> getPossibleMoves() {
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
    public MyShortestPathProblem copy() {
        MyShortestPathProblem copy = new MyShortestPathProblem(graph, start, end);
        copy.path = new ArrayList<>(this.path);
        copy.visited = new HashSet<>(this.visited);
        copy.currentLength = this.currentLength;
        return copy;
    }
}