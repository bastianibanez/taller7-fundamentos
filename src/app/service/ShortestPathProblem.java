package app.service;

import java.util.List;

public interface ShortestPathProblem {
    boolean isSolution();
    void applyMove(int move);
    void undoMove(int move);
    List<Integer> getPossibleMoves();
    List<Integer> getCurrentPath();
}

