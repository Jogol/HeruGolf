package com.company;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        HeruGolfGenerator golfGenerator = new HeruGolfGenerator(5, 5);

        HeruGolfSolver golfSolver = new HeruGolfSolver(golfGenerator.getBoardState(), golfGenerator.getBallNumbers());

//        int[] values = new int[]{1, 2, 5, 7, 12, 15};
//        double[] ratios = new double[results.length];
//        for (int i = 0; i < results.length; i++) {
//            if (results[i] != 0) {
//                ratios[i] = Math.round((double) results[i] / total * values[i]); //35 = estimated average
//            }
//            System.out.print(ratios[i] + " ");
//        }

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
