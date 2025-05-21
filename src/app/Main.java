package app;

import app.impl.*;
import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        for (int i = 30; i < 100; i++) {
            RunAlgorithms run = new RunAlgorithms(i);
        }
    }
}