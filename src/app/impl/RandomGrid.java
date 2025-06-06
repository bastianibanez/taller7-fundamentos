package app.impl;

public class RandomGrid {
    private int[][] grid;
    private int dimension;

    public RandomGrid(int dimension) {
        this.dimension = dimension;
        this.grid = new int[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                this.grid[i][j] = (int) (Math.random() * 10);
            }
        }
    }

    public void showGrid() {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public int[][] get(){
        return grid;
    }
}
