package app;

import app.impl.*;
import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        for (int i = 2; i < 20; i++) {
            RunAlgorithms run = new RunAlgorithms(i);
        }
    }
}