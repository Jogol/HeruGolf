package com.company;

import java.util.Random;

public class Main {

    public static void main(String[] args) {

        //HeruGolfGenerator golfGenerator = new HeruGolfGenerator(5, 7);

        int[] results = new int[7];

        for (int i = 0; i < 1000; i++) {
            double res = getGaussian(3, 1);
            if (res > 0 && res < 1) {
                results[0]++;
            } else if (res > 1 && res < 2) {
                results[1]++;
            } else if (res > 2 && res < 3) {
                results[2]++;
            } else if (res > 3 && res < 4) {
                results[3]++;
            } else if (res > 4 && res < 5) {
                results[4]++;
            } else if (res > 5 && res < 6) {
                results[5]++;
            }
        }

        for (int result : results) {
            System.out.print((double) result/100 + " ");
        }

    }

    private static double getGaussian(double aMean, double aVariance){
        Random fRandom = new Random();
        return aMean + fRandom.nextGaussian() * aVariance;
    }
}
