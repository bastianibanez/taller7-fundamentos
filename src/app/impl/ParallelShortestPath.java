package app.impl;

import app.service.ShortestPathProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class ParallelShortestPath extends RecursiveTask<List<Integer>> {
    private final ShortestPathProblem problem; // This will be the unique state for this task
    private int bestPathLength; // This task's view of the best path length
    private final int currentDepth;

    // Threshold after which tasks are no longer forked but solved sequentially within the current task.
    // This value can be tuned (e.g., 3, 4, or 5).
    private static final int SEQUENTIAL_THRESHOLD_DEPTH = 1;

    // Constructor for the initial call (e.g., from RunAlgorithms)
    public ParallelShortestPath(ShortestPathProblem problemToCopy, int initialBestPathLength) {
        // The root task also works on its own copy of the problem.
        this.problem = problemToCopy; // Assuming problemToCopy is already a copy from RunAlgorithms
        this.bestPathLength = initialBestPathLength;
        this.currentDepth = 0;
    }

    // Private constructor for recursive (forked) calls
    private ParallelShortestPath(ShortestPathProblem problemStateForThisTask, int bestPathLength, int depth) {
        // problemStateForThisTask is already the unique, copied, and (potentially) move-applied state for this child.
        this.problem = problemStateForThisTask;
        this.bestPathLength = bestPathLength;
        this.currentDepth = depth;
    }

    @Override
    protected List<Integer> compute() {
        if (problem.isSolution()) {
            return new ArrayList<>(problem.getCurrentPath()); // Return a copy
        }

        // Pruning: if current path is already not better than the best known, stop.
        // (problem.getCurrentPathLength() > 0 is to avoid pruning the initial empty path immediately)
        if (problem.getCurrentPathLength() > 0 && problem.getCurrentPathLength() >= bestPathLength) {
            return null;
        }

        List<Integer> shortestPathFoundInThisBranch = null;
        List<Integer> possibleMoves = problem.getPossibleMoves();

        if (currentDepth >= SEQUENTIAL_THRESHOLD_DEPTH) {
            // --- Switch to sequential exploration within this task ---
            for (int move : possibleMoves) {
                problem.applyMove(move); // Apply move to this task's problem state

                // Check if this path is still promising before deeper recursion
                if (problem.getCurrentPathLength() < bestPathLength) {
                    List<Integer> path = compute(); // Recursive call on the *same* task's logic (now sequential)

                    if (path != null) {
                        if (shortestPathFoundInThisBranch == null || path.size() < shortestPathFoundInThisBranch.size()) {
                            shortestPathFoundInThisBranch = path; // path is already a distinct list
                            this.bestPathLength = shortestPathFoundInThisBranch.size(); // Update for subsequent moves in this sequential block
                        }
                    }
                }
                problem.undoMove(move); // Backtrack
            }
        } else {
            // --- Continue with parallel forking ---
            List<ParallelShortestPath> tasks = new ArrayList<>();
            for (int move : possibleMoves) {
                // 1. Create a fresh copy of the current (parent) task's problem state for the child
                ShortestPathProblem problemForChild = this.problem.copy();
                // 2. Apply the move to this new independent copy
                problemForChild.applyMove(move);

                // 3. Check if this path is still worth exploring based on parent's bestPathLength
                if (problemForChild.getCurrentPathLength() < this.bestPathLength) {
                    // 4. Create child task with the new state and incremented depth
                    ParallelShortestPath childTask = new ParallelShortestPath(
                            problemForChild,      // Pass the already copied and move-applied state
                            this.bestPathLength,  // Pass down the current best known length
                            currentDepth + 1
                    );
                    tasks.add(childTask);
                    childTask.fork();
                }
                // this.problem (parent's problem state) remains unchanged
            }

            // Collect results from forked tasks
            for (ParallelShortestPath task : tasks) {
                List<Integer> pathFromChild = task.join();
                if (pathFromChild != null) {
                    if (shortestPathFoundInThisBranch == null || pathFromChild.size() < shortestPathFoundInThisBranch.size()) {
                        shortestPathFoundInThisBranch = pathFromChild; // pathFromChild is a result, should be safe
                        // Update this task's bestPathLength. This helps if this task itself
                        // were to do more work after joins or if used in more complex scenarios.
                        // For sibling tasks already forked, this update won't help them directly.
                        this.bestPathLength = shortestPathFoundInThisBranch.size();
                    }
                }
            }
        }
        return shortestPathFoundInThisBranch;
    }
}