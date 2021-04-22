package com.company;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class HeruGolfGenerator {

    int width;
    int height;
    float hazardRatio = 0.1f;
    float fillRatio = 0.7f;
    int[][] boardState;
    int[] values = new int[]{1, 2, 3, 7, 9, 11, 13};
    double mean = 2.5;
    double variance = 1;

    HeruGolfGenerator(int width, int height) {
        this.width = width;
        this.height = height;

        boardState = new int[width][height];

        generateHazards();
        int[] ballAmounts = generateBallsAmount();
        generateBallsAndHoles(ballAmounts);
//        printBoardState();
    }

    private void generateBallsAndHoles(int[] ballAmounts) {
        ArrayList<Integer> ballList = new ArrayList<>();
        for (int i = 0; i < ballAmounts.length; i++) {
            for (int j = 0; j < ballAmounts[i]; j++) {
                ballList.add(i + 1);
            }
        }
        Collections.shuffle(ballList);
        for (Integer i : ballList) {
            placeLine(i + 1);
        }

    }

    private void placeLine(int lineLength) {
        int [][] attemptedTiles = new int[boardState.length][];
        for(int i = 0; i < boardState.length; i++) {
            attemptedTiles[i] = boardState[i].clone();
        }

    }

    private float getBoardFullness() {
        float numOfFilledTiles = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (boardState[i][j] != 0) {
                    numOfFilledTiles++;
                }
            }
        }
        return numOfFilledTiles / (width * height);
    }

    private int[] generateBallsAmount() {
        int maxBallSize = 7;
        ArrayList<ArrayList<Double>> results = new ArrayList<>(maxBallSize);
        for (int i = 0; i < maxBallSize; i++) {
            results.add(new ArrayList<>());
        }

        for (int i = 0; i < 1000; i++) {
            double res = getGaussian(mean, variance);
            int resInt = (int) res;

            if (resInt >= 0 && resInt < results.size()) {
                results.get(resInt).add(res);
            }

            float tilesUsed = estimateTilesUsed(results);
            if (tilesUsed > (width * height * fillRatio)) {
                int[] ballsAmounts = new int[maxBallSize];
                for (int j = 0; j < results.size(); j++) {
                    ballsAmounts[j] = results.get(j).size();
                }
                return ballsAmounts;
            }

        }


        return null;
    }

    private int estimateTilesUsed(ArrayList<ArrayList<Double>> results) {
        int tilesUsed = 0;
        for (int i = 0; i < results.size(); i++) {
            tilesUsed += results.get(i).size() * values[i];
        }
        return tilesUsed;
    }

    private void printBoardState() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(boardState[x][y] + " ");
            }
            System.out.println();
        }
    }
    private void generateHazards() {
        int hazardNumber = Math.round(width * height * hazardRatio);

        Random rand = new Random();
        for (int i = 0; i < hazardNumber; i++) {
            boardState[rand.nextInt(width)][rand.nextInt(height)] = TileState.HAZARD.getValue();
        }
    }

    private double getGaussian(double aMean, double aVariance){
        Random fRandom = new Random();
        return aMean + fRandom.nextGaussian() * aVariance;
    }

    enum TileState {
        EMPTY(0),
        HORIZONTAL(1),
        VERTICAL(2),
        HAZARD(3),
        BALL(4),
        HOLE(5);

        private final int value;
        TileState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
