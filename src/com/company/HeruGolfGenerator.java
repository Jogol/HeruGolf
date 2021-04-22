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
            results.add(new ArrayList<Double>());
        }

        for (int i = 0; i < 1000; i++) {
            double res = getGaussian(2.5, 1);
            int resInt = (int) res;


            switch (resInt) {
                case 0:
                    results.get(0).add(res);
                    break;
                case 1:
                    results.get(1).add(res);
                    break;
                case 2:
                    results.get(2).add(res);
                    break;
                case 3:
                    results.get(3).add(res);
                    break;
                case 4:
                    results.get(4).add(res);
                    break;
                case 5:
                    results.get(5).add(res);
                    break;
                case 6:
                    results.get(6).add(res);
                    break;
                case 7:
                    results.get(7).add(res);
                    break;
                case 8:
                    results.get(8).add(res);
                    break;
                case 9:
                    results.get(9).add(res);
                    break;
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
        private TileState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
