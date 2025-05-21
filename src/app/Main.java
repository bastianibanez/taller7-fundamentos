package app;

import app.impl.*;
import app.service.ShortestPathProblem;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        for (int i = 5; i < 200; i = i+5) {
            RunAlgorithms run = new RunAlgorithms(i);
        }

//         for (int i = 100; i < 2000; i = i+50) {
//             RunAlgorithms run = new RunAlgorithms(i);
//         }
    }
}