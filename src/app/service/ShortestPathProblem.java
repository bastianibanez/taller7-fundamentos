package app.service;

import app.impl.MyShortestPathProblem;

import java.util.List;

public interface ShortestPathProblem {
    boolean isSolution();
    void applyMove(int move);
    void undoMove(int move);
    List<Integer> getPossibleMoves();
    List<Integer> getCurrentPath();
    int getCurrentPathLength();
    int getCurrentPathWeight();
    MyShortestPathProblem copy();
}