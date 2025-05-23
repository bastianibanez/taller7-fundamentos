package app;

import app.impl.*;
import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        for (int i = 10; i < 100; i = i + 10) {
            RunAlgorithms run = new RunAlgorithms(i);
        }

        for (int i = 100; i < 500; i = i + 100) {
            RunAlgorithms run = new RunAlgorithms(i);
        }
    }
}