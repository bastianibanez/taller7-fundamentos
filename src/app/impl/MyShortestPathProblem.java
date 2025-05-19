package app.impl;

import app.service.ShortestPathProblem;

import java.util.List;
import java.util.ArrayList;

public class MyShortestPathProblem implements ShortestPathProblem {
    private int[][] grid;
    private boolean[][] visited;
    private int currX, currY;
    private int startX, startY;
    private int endX, endY;
    private List<Integer> currentPath;

    public MyShortestPathProblem(
            int[][] grid,
            int startX, int startY,
            int endX, int endY
    )
    {
        this.grid = grid;
        this.visited = new boolean[grid.length][grid[0].length];
        this.startX = startX;
        this.startY = startY;
        this.currX = startX;
        this.currY = startY;
        this.endX = endX;
        this.endY = endY;
        this.currentPath = new ArrayList<>();
        this.visited[startX][startY] = true;
    }

    @Override
    public boolean isSolution(){
        return currX == endX && currY == endY;
    }

    @Override
    public void applyMove(int move){
        //Moves: 0:Up - 1:Down - 2:Left - 3:Right
        currentPath.add(move);

        switch (move){
            case 0:
                currX--;
                break;
            case 1:
                currX++;
                break;
            case 2:
                currY--;
                break;
            case 3:
                currY++;
                break;
        }

        visited[currX][currY] = true;
    }

    @Override
    public void undoMove(int move){
        //Moves: 0:Up - 1:Down - 2:Left - 3:Right

        visited[currX][currY] = false;

        switch (move){
            case 0:
                currX++;
                break;
            case 1:
                currX--;
                break;
            case 2:
                currY++;
                break;
            case 3:
                currY --;
                break;
        }
    }

    public boolean isValidMove(int move){
       switch (move){
           case 0:
               return (currX - 1 >= 0);
           case 1:
               return (currX + 1 < grid.length);
           case 2:
               return (currY - 1 >= 0);
           case 3:
               return (currY + 1 < grid.length);
           default:
               return false;
       }
    }

    @Override
    public List<Integer> getPossibleMoves(){
        List<Integer> moves = new ArrayList<>();
        //Moves: 0:Up - 1:Down - 2:Left - 3:Right

        for (int i = 0; i < 4; i++){
            if (isValidMove(i)){
                moves.add(i);
            }
        }
        return moves;
    }

    @Override
    public List<Integer> getCurrentPath(){
        return new ArrayList<>(currentPath);
    }

    @Override
    public int getCurrentPathLength(){
        return currentPath.size();
    }

}
