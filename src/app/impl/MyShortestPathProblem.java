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

    public MyShortestPathProblem(int[][] graph, int start, int end){
        this.graph = graph;
        this.start = start;
        this.end = end;

        path.add(start);
        visited.add(start);
    }

    @Override
    public boolean isSolution(){
        return path.get(path.size() - 1) == end;
    }

    @Override
    public void applyMove(int move){
        int from = path.get(path.size() - 1);
        currentLength += graph[from][move];
        path.add(move);
        visited.add(move);
    }

    @Override
    public void undoMove(int move){
        path.remove(path.size() - 1);
        visited.remove(move);
        int from = path.get(path.size() - 1);
        currentLength -= graph[from][move];
    }

    @Override
    public List<Integer> getPossibleMoves(){
        int from = path.get(path.size() - 1);
        List<Integer> moves = new ArrayList<>();

        for (int to = 0; to < graph.length; to++){
            if (graph[from][to] > 0 && !visited.contains(to)){
                moves.add(to);
            }
        }
        return moves;
    }

    @Override
    public List<Integer> getCurrentPath(){
        return path;
    }

    @Override
    public int getCurrentPathLength(){
        return path.size();
    }
}