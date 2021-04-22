package com.company;

import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        HeruGolfGenerator golfGenerator = new HeruGolfGenerator(5, 5);

//        ArrayList<ArrayList<Double>> results = new ArrayList<>(6);
//        for (int i = 0; i < 6; i++) {
//            results.add(new ArrayList<Double>());
//        }
//
//        for (int i = 0; i < 1000; i++) {
//            double res = getGaussian(3, 1);
//            int resInt = (int) res;
//
//
//            switch (resInt) {
//                case 0:
//                    results.get(0).add(res);
//                    break;
//                case 1:
//                    results.get(1).add(res);
//                    break;
//                case 2:
//                    results.get(2).add(res);
//                    break;
//                case 3:
//                    results.get(3).add(res);
//                    break;
//                case 4:
//                    results.get(4).add(res);
//                    break;
//                case 5:
//                    results.get(5).add(res);
//                    break;
//            }
//
//        }
//
//        OptionalDouble[] averages = new OptionalDouble[6];
//
//        for (int i = 0; i < results.size(); i++) {
//            averages[i] = results.get(i).stream().mapToDouble(a -> a).average();
//        }
//
//        for (ArrayList<Double> result : results) {
//            System.out.print(result.size() + " ");
//        }
//        System.out.println();
//        int total = 0;
//        for (OptionalDouble result : averages) {
//            result.ifPresent(System.out::println);
//        }

//        int[] values = new int[]{1, 2, 5, 7, 12, 15};
//        double[] ratios = new double[results.length];
//        for (int i = 0; i < results.length; i++) {
//            if (results[i] != 0) {
//                ratios[i] = Math.round((double) results[i] / total * values[i]); //35 = estimated average
//            }
//            System.out.print(ratios[i] + " ");
//        }

    }

    private static double getGaussian(double aMean, double aVariance){
        Random fRandom = new Random();
        return aMean + fRandom.nextGaussian() * aVariance;
    }
}
