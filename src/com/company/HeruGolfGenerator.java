package com.company;

import java.util.Random;

public class HeruGolfGenerator {

    int width;
    int height;
    float hazardRatio = 0.1f;
    float fillRatio = 0.7f;
    int[][] boardState;

    HeruGolfGenerator(int width, int height) {
        this.width = width;
        this.height = height;

        boardState = new int[width][height];

        generateHazards();
        printBoardState();
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

    public enum TileState {
        HORIZONTAL(0),
        VERTICAL(1),
        HAZARD(2),
        BALL(3),
        HOLE(4);

        private final int value;
        private TileState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
