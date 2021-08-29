package com.company;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.company.HeruGolfUtil.*;

public class Main {

    public static void main(String[] args) {

        final long startTime = System.nanoTime();
        int attempts = 100000;
        int solvedPuzzles = 0;
        int highestOccurence = 0;
        int unsolveable = 0;
        int[][] bestBoardState = null;
        int[][] bestBoardNumbers = null;
        for (int i = 0; i < attempts; i++) {
            HeruGolfGenerator golfGenerator = new HeruGolfGenerator(10, 10);
            HeruGolfSolver golfSolver = new HeruGolfSolver(getBoardStateCopy(golfGenerator.getBoardState()), getBoardStateCopy(golfGenerator.getBallNumbers()));
            if (golfSolver.getSolved()) {
                solvedPuzzles++;
                if (golfSolver.getOccurrencesOfMultiplePossibilites() > highestOccurence) {
                    highestOccurence = golfSolver.getOccurrencesOfMultiplePossibilites();
                    bestBoardState = golfGenerator.getBoardState();
                    bestBoardNumbers = golfGenerator.getBallNumbers();
                }
            } else {
                unsolveable++;
//                System.out.println("Unsolveable:");
//                printSaveableBoard(golfGenerator.getBoardState(), golfGenerator.getBallNumbers());
            }

            if (i % (attempts/100) == 0) {
                System.out.println(i / (attempts/100));
            }
        }

        printPlayableBoard(bestBoardState, bestBoardNumbers);
        System.out.println();
        printSaveableBoard(bestBoardState, bestBoardNumbers);
        System.out.println("Solved: " + solvedPuzzles + ", Highest occs: " + highestOccurence + " Unsolveable: " + unsolveable + "/" + attempts);

//        int[] values = new int[]{1, 2, 5, 7, 12, 15};
//        double[] ratios = new double[results.length];
//        for (int i = 0; i < results.length; i++) {
//            if (results[i] != 0) {
//                ratios[i] = Math.round((double) results[i] / total * values[i]); //35 = estimated average
//            }
//            System.out.print(ratios[i] + " ");
//        }
        final long nanos = System.nanoTime() - startTime;
        //hh:mm:ss
        String output = String.format("%02d:%02d:%02d",
                TimeUnit.NANOSECONDS.toHours(nanos),
                TimeUnit.NANOSECONDS.toMinutes(nanos) - TimeUnit.HOURS.toMinutes(TimeUnit.NANOSECONDS.toHours(nanos)),
                TimeUnit.NANOSECONDS.toSeconds(nanos) - TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(nanos)));
        System.out.println(output);


    }

    private static void printGaussianAveragesEtc() {
        ArrayList<ArrayList<Double>> results = new ArrayList<>(8);
        for (int i = 0; i < 8; i++) {
            results.add(new ArrayList<Double>());
        }

        for (int i = 0; i < 1000; i++) {
            double res = getGaussian(2.5, 1);
            int resInt = (int) res;


            results.get(resInt).add(res);

        }

        OptionalDouble[] averages = new OptionalDouble[8];

        for (int i = 0; i < results.size(); i++) {
            averages[i] = results.get(i).stream().mapToDouble(a -> a).average();
        }

        for (ArrayList<Double> result : results) {
            System.out.print(result.size() + " ");
        }
        System.out.println();
        int total = 0;
        for (OptionalDouble result : averages) {
            result.ifPresent(System.out::println);
        }
    }

    private static double getGaussian(double aMean, double aVariance){
        Random fRandom = new Random();
        return aMean + fRandom.nextGaussian() * aVariance;
    }
}
