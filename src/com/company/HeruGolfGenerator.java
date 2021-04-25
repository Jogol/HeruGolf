package com.company;

import java.util.*;

public class HeruGolfGenerator {

    int width;
    int height;
    float hazardRatio = 0.1f;
    float fillRatio = 0.7f;
    float basePropagation = 0.9f;
    int[][] boardState;
    int[] values = new int[]{1, 2, 3, 7, 9, 11, 13};
    double mean = 2.7;
    double variance = 1;
    Random rand;

    HeruGolfGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        rand = new Random();
        boardState = new int[width][height];

        generateHazards();
//        int[] ballAmounts = generateBallsAmount();
        generateBallsAndHoles();
        printBoardState();
        System.out.println();
    }

    private void generateBallsAndHoles() {

        float fullness = getBoardFullness();
        float propagationChance = basePropagation;
        while (fullness < fillRatio) {
            placeLine(generateNextBall(), null);
            fullness = getBoardFullness();
        }

    }

    private boolean placeLine(int lineLength, Position startPosition) {
        if (lineLength == 0) {
            return false;
        }
        int [][] attemptedTiles = getBoardStateCopy();
        boolean done = false;
        while (!done) {
            if (startPosition != null) {
                done = true;
            } else {
                startPosition = getRandomOpenPosition(attemptedTiles);
            }

            if (startPosition == null) {
                System.out.println("No empty spots on board.");
                return false;
            }
            ArrayList<Direction> directionList = new ArrayList<>(Arrays.asList(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT));
            Collections.shuffle(directionList);

            Position currentPosition = startPosition;
            for (Direction direction : directionList) {
                ArrayList<Position> positionHistory = new ArrayList<>();
                for (int i = 0; i < lineLength; i++) {
                    Position nextPosition = positionInDirection(currentPosition, direction);
                    if (nextPosition != null && boardState[nextPosition.getX()][nextPosition.getY()] == TileState.EMPTY.getValue()) {
                        positionHistory.add(nextPosition);
                        currentPosition = nextPosition;
                    } else {
                        break;
                    }
                }
                if (positionHistory.size() == lineLength) {
                    boardState[startPosition.getX()][startPosition.getY()] = TileState.BALL.getValue();
                    for (Position position : positionHistory) {
                        if (direction == Direction.UP || direction == Direction.DOWN) {
                            boardState[position.getX()][position.getY()] = TileState.VERTICAL.getValue();
                        } else if (direction == Direction.RIGHT || direction == Direction.LEFT) {
                            boardState[position.getX()][position.getY()] = TileState.HORIZONTAL.getValue();
                        }
                    }
                    float fullness = getBoardFullness();
                    float propagationChance = basePropagation - (fullness/10);
                    Position lastPosition = positionHistory.get(positionHistory.size()-1);
                    if (rand.nextFloat() < propagationChance) {
                        if (placeLine(lineLength-1, lastPosition)) {
                            boardState[lastPosition.getX()][lastPosition.getY()] = TileState.HOLE.getValue();
                        }
                    } else {
                        boardState[lastPosition.getX()][lastPosition.getY()] = TileState.HOLE.getValue();
                        return true;
                    }
                }

            }

            attemptedTiles[startPosition.getX()][startPosition.getY()] = TileState.ATTEMPT.getValue();
        }

        return false;
    }

    private Position positionInDirection(Position currentPosition, Direction direction) {

        Position newPosition;
        switch (direction) {
            case UP -> newPosition = new Position(currentPosition.getX(), currentPosition.getY() - 1);
            case RIGHT -> newPosition = new Position(currentPosition.getX() + 1, currentPosition.getY());
            case DOWN -> newPosition = new Position(currentPosition.getX(), currentPosition.getY() + 1);
            case LEFT -> newPosition = new Position(currentPosition.getX() - 1, currentPosition.getY());
            default -> { return null; }
        }

        if (isInsideBounds(newPosition)) {
            return newPosition;
        } else {
            return null;
        }
    }

    private boolean isInsideBounds(Position position) {
        if (position.getX() >= 0 && position.getX() < width && position.getY() >= 0 && position.getY() < height) {
            return true;
        } else {
            return false;
        }
    }

    private Direction getRandomDirection() {
        Random rand = new Random();
        int val = rand.nextInt(4);

        switch (val) {
            case 0 : return Direction.UP;
            case 1 : return Direction.RIGHT;
            case 2 : return Direction.DOWN;
            case 3 : return Direction.LEFT;
        }
        return null;
    }
    private int[][] getBoardStateCopy() {
        int [][] copy = new int[boardState.length][];
        for(int i = 0; i < boardState.length; i++) {
            copy[i] = boardState[i].clone();
        }
        return copy;
    }

    private Position getRandomOpenPosition(int[][] attemptedTiles) {
        if (!boardHasEmptyTile(attemptedTiles)) {
            return null;
        }
        Random rand = new Random();
        boolean done = false;
        int x = 0;
        int y = 0;
        while (!done) {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            if (attemptedTiles[x][y] == TileState.EMPTY.getValue()) {
                done = true;
            }
        }
        return new Position(x, y);
    }

    private boolean boardHasEmptyTile(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == TileState.EMPTY.getValue()) {
                    return true;
                }
            }
        }
        return false;
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

    private int generateNextBall() {
        int intResult = 0;
        while (intResult == 0) {
            double result = getGaussian(mean, variance);
            intResult = (int) result;
        }
        return intResult;
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
        HOLE(5),
        ATTEMPT(6);

        private final int value;
        TileState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    enum Direction {
        UP(0),
        RIGHT(1),
        DOWN(2),
        LEFT(3);

        private final int value;
        Direction(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

}
