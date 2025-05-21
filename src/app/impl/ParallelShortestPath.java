package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelShortestPath extends RecursiveTask<List<Integer>> {
    private final ShortestPathProblem problem;
    private final AtomicInteger sharedBestPathWeight;
    private final int currentDepth;

    private static final int SEQUENTIAL_THRESHOLD_DEPTH = 3;

    public ParallelShortestPath(ShortestPathProblem problemToCopy, AtomicInteger initialSharedBestPathWeight) {
        this.problem = problemToCopy;
        this.sharedBestPathWeight = initialSharedBestPathWeight;
        this.currentDepth = 0;
    }

    private ParallelShortestPath(ShortestPathProblem problemStateForThisTask, AtomicInteger sharedBestPathWeight, int depth) {
        this.problem = problemStateForThisTask;
        this.sharedBestPathWeight = sharedBestPathWeight;
        this.currentDepth = depth;
    }

    private void updateSharedBestPathWeight(int newWeight) {
        int oldWeight;
        do {
            oldWeight = sharedBestPathWeight.get();
            if (newWeight >= oldWeight) {
                return;
            }
        } while (!sharedBestPathWeight.compareAndSet(oldWeight, newWeight));
    }

    @Override
    protected List<Integer> compute() {
        int currentGlobalBestWeight = sharedBestPathWeight.get();

        if (problem.isSolution()) {
            List<Integer> currentPath = problem.getCurrentPath();
            updateSharedBestPathWeight(problem.getCurrentPathWeight());
            return new ArrayList<>(currentPath);
        }

        if (problem.getCurrentPathWeight() >= currentGlobalBestWeight && problem.getCurrentPath().size() > 1) {
            return null;
        }

        List<Integer> shortestPathFoundLocally = null;

        List<Integer> possibleMoves = problem.getPossibleMoves();

        if (currentDepth >= SEQUENTIAL_THRESHOLD_DEPTH) {
            for (int move : possibleMoves) {
                problem.applyMove(move);
                if (problem.getCurrentPathWeight() < sharedBestPathWeight.get()) {
                    List<Integer> path = compute();

                    if (path != null) {
                        if (shortestPathFoundLocally == null) {
                            shortestPathFoundLocally = path;
                        }
                    }
                }
                problem.undoMove(move);
            }
        } else {
            List<ParallelShortestPath> tasks = new ArrayList<>();
            for (int move : possibleMoves) {
                ShortestPathProblem problemForChild = this.problem.copy();
                problemForChild.applyMove(move);

                if (problemForChild.getCurrentPathWeight() < sharedBestPathWeight.get()) {
                    ParallelShortestPath childTask = new ParallelShortestPath(
                            problemForChild,
                            this.sharedBestPathWeight,
                            currentDepth + 1
                    );
                    tasks.add(childTask);
                    childTask.fork();
                }
            }

            for (ParallelShortestPath task : tasks) {
                List<Integer> pathFromChild = task.join();
                if (pathFromChild != null) {
                    if (shortestPathFoundLocally == null) {
                        shortestPathFoundLocally = pathFromChild;
                    }
                }
            }
        }
        return shortestPathFoundLocally;
    }
}