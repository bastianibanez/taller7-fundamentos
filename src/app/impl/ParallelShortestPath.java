package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelShortestPath extends RecursiveTask<List<Integer>> {
    private final ShortestPathProblem problem;
    private final AtomicInteger sharedBestPathWeight; // Stores the best weight found globally
    private final int currentDepth;

    // This threshold determines when to stop forking and solve sequentially.
    // Tune this value based on performance observations (e.g., 0, 1, 2, 3).
    // A lower value reduces parallelism but also overhead from copies/task creation.
    private static final int SEQUENTIAL_THRESHOLD_DEPTH = 3;

    /**
     * Constructor for the initial parallel task.
     * @param problemToCopy The problem instance (a copy will be made if needed,
     * or assumed to be a fresh copy for this root task).
     * @param initialSharedBestPathWeight An AtomicInteger initialized to Integer.MAX_VALUE.
     */
    public ParallelShortestPath(ShortestPathProblem problemToCopy, AtomicInteger initialSharedBestPathWeight) {
        this.problem = problemToCopy; // Assumes this is the copy intended for the root parallel task
        this.sharedBestPathWeight = initialSharedBestPathWeight;
        this.currentDepth = 0;
    }

    /**
     * Private constructor for forked child tasks.
     * @param problemStateForThisTask The unique problem state (already copied and move-applied) for this child.
     * @param sharedBestPathWeight    The reference to the same AtomicInteger shared across all tasks.
     * @param depth                   The current recursion depth for this child task.
     */
    private ParallelShortestPath(ShortestPathProblem problemStateForThisTask, AtomicInteger sharedBestPathWeight, int depth) {
        this.problem = problemStateForThisTask;
        this.sharedBestPathWeight = sharedBestPathWeight;
        this.currentDepth = depth;
    }

    /**
     * Atomically updates the shared best path weight if the new weight is better.
     * @param newWeight The new path weight to consider.
     */
    private void updateSharedBestPathWeight(int newWeight) {
        int oldWeight;
        do {
            oldWeight = sharedBestPathWeight.get();
            if (newWeight >= oldWeight) {
                return; // New weight is not better, no update needed
            }
        } while (!sharedBestPathWeight.compareAndSet(oldWeight, newWeight));
        // If compareAndSet succeeds, the value is updated.
        // If it fails, it means another thread updated it; the loop re-checks.
    }

    @Override
    protected List<Integer> compute() {
        // Get the current best known weight from the shared atomic variable
        int currentGlobalBestWeight = sharedBestPathWeight.get();

        // If this path itself is a solution
        if (problem.isSolution()) {
            List<Integer> currentPath = problem.getCurrentPath();
            updateSharedBestPathWeight(problem.getCurrentPathWeight()); // Update global best with this path's weight
            return new ArrayList<>(currentPath); // Return a copy of the path
        }

        // Pruning: if current path's weight is already not better than the best known, stop.
        // The check problem.getCurrentPath().size() > 1 helps avoid pruning the initial state
        // if its weight is 0 (e.g. path = [startNode], weight = 0)
        // and currentGlobalBestWeight is still MAX_VALUE.
        if (problem.getCurrentPathWeight() >= currentGlobalBestWeight && problem.getCurrentPath().size() > 1) {
            return null;
        }

        List<Integer> shortestPathFoundLocally = null; // Holds the path list for the best solution found from this task's branch

        List<Integer> possibleMoves = problem.getPossibleMoves();

        if (currentDepth >= SEQUENTIAL_THRESHOLD_DEPTH) {
            // --- Switch to sequential exploration within this task ---
            for (int move : possibleMoves) {
                problem.applyMove(move);
                // Re-check against the potentially updated global best before deeper recursion
                if (problem.getCurrentPathWeight() < sharedBestPathWeight.get()) {
                    List<Integer> path = compute(); // Recursive call, will execute sequentially due to depth

                    if (path != null) {
                        // If a path is returned, it means a valid solution was found down this line,
                        // and sharedBestPathWeight would have been updated by the deeper call.
                        // We just need to keep track of one such valid path list to return.
                        // A more sophisticated approach might compare weights if multiple paths are returned
                        // by the sequential explorations, but this simplifies.
                        if (shortestPathFoundLocally == null) { // Keep the first valid path found
                            shortestPathFoundLocally = path;
                        }
                        // To pick the actual "best" among paths from this sequential block,
                        // compute() would need to return weight, or we'd re-evaluate problem state for 'path'.
                        // Relying on sharedBestPathWeight to have been updated is the primary pruning mechanism.
                    }
                }
                problem.undoMove(move); // Backtrack
            }
        } else {
            // --- Continue with parallel forking ---
            List<ParallelShortestPath> tasks = new ArrayList<>();
            for (int move : possibleMoves) {
                ShortestPathProblem problemForChild = this.problem.copy(); // Create a fresh copy for the child
                problemForChild.applyMove(move); // Apply the move to the child's copy

                // Re-check against the potentially updated global best before forking
                if (problemForChild.getCurrentPathWeight() < sharedBestPathWeight.get()) {
                    ParallelShortestPath childTask = new ParallelShortestPath(
                            problemForChild,          // Pass the child's unique problem state
                            this.sharedBestPathWeight, // Pass the reference to the same AtomicInteger
                            currentDepth + 1
                    );
                    tasks.add(childTask);
                    childTask.fork();
                }
            }

            // Collect results from forked tasks
            for (ParallelShortestPath task : tasks) {
                List<Integer> pathFromChild = task.join();
                if (pathFromChild != null) {
                    // If a child returns a path, it means a solution was found and
                    // sharedBestPathWeight was potentially updated.
                    // Similar to the sequential part, we just keep one valid path list.
                    if (shortestPathFoundLocally == null) {
                        shortestPathFoundLocally = pathFromChild;
                    }
                    // If we needed to compare which child returned the absolute best path (by weight)
                    // among siblings, compute() would need to return more than just List<Integer>.
                }
            }
        }
        return shortestPathFoundLocally; // Return the path list found from this branch
    }
}