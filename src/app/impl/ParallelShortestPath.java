package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ParallelShortestPath extends RecursiveTask<Void> {
    private final ShortestPathProblem problem;
    private final AtomicInteger sharedBestPathWeight;
    private final AtomicReference<List<Integer>> sharedBestPathRef;
    private final int currentDepth;

    private static final int SEQUENTIAL_THRESHOLD_DEPTH = 3;

    public ParallelShortestPath(ShortestPathProblem problemToCopy,
                                AtomicInteger initialSharedBestPathWeight,
                                AtomicReference<List<Integer>> bestPathRef) {
        this.problem = problemToCopy;
        this.sharedBestPathWeight = initialSharedBestPathWeight;
        this.sharedBestPathRef = bestPathRef;
        this.currentDepth = 0;
    }

    private ParallelShortestPath(ShortestPathProblem problemStateForThisTask,
                                 AtomicInteger sharedBestPathWeight,
                                 AtomicReference<List<Integer>> bestPathRef,
                                 int depth) {
        this.problem = problemStateForThisTask;
        this.sharedBestPathWeight = sharedBestPathWeight;
        this.sharedBestPathRef = bestPathRef;
        this.currentDepth = depth;
    }

    private void attemptUpdateGlobalBest(List<Integer> currentLocalPath, int currentLocalWeight) {
        int currentGlobalBestWeightValue;
        while (true) {
            currentGlobalBestWeightValue = sharedBestPathWeight.get();
            if (currentLocalWeight < currentGlobalBestWeightValue) {
                if (sharedBestPathWeight.compareAndSet(currentGlobalBestWeightValue, currentLocalWeight)) {
                    sharedBestPathRef.set(new ArrayList<>(currentLocalPath));
                    return;
                }
            } else {
                return;
            }
        }
    }

    @Override
    protected Void compute() {
        if (problem.getCurrentPathWeight() >= sharedBestPathWeight.get() && problem.getCurrentPath().size() > 1) {
            return null;
        }

        if (problem.isSolution()) {
            attemptUpdateGlobalBest(problem.getCurrentPath(), problem.getCurrentPathWeight());
            return null;
        }

        List<Integer> possibleMoves = problem.getPossibleMoves();

        if (currentDepth >= SEQUENTIAL_THRESHOLD_DEPTH) {
            for (int move : possibleMoves) {
                problem.applyMove(move);
                if (problem.getCurrentPathWeight() < sharedBestPathWeight.get()) {
                    compute();
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
                            this.sharedBestPathRef,
                            currentDepth + 1
                    );
                    tasks.add(childTask);
                    childTask.fork();
                }
            }
            for (ParallelShortestPath task : tasks) {
                task.join();
            }
        }
        return null;
    }
}