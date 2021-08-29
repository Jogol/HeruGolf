package com.company;

import java.util.*;

import static com.company.HeruGolfUtil.*;


public class HeruGolfGenerator {

    int width;
    int height;
    float hazardRatio = 0.1f;
    float fillRatio = 0.8f;
    float basePropagation = 0.9f;
    int[][] boardState;
    int[][] ballNumbers;
    int[] values = new int[]{1, 2, 3, 7, 9, 11, 13};
//    double mean = 2.7;
//    double variance = 1;
    Random rand;

    HeruGolfGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        rand = new Random();
        boardState = new int[width][height];
        ballNumbers = new int[width][height];

        generateHazards();
        generateBallsAndHoles();
//        System.out.println();
//        printPlayableBoard(boardState);
//        System.out.println();
//        printBoardState(boardState);
//        System.out.println();
//        printBoardState(ballNumbers);
//        System.out.println();
//        System.out.println("Board fullness: " + getBoardFullness());
//        countBallsAndHoles();

        removeSolution();
//        printPlayableBoard(boardState);
    }

    private void removeSolution() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (boardState[i][j] == TileState.HORIZONTAL.getValue() ||
                        boardState[i][j] == TileState.VERTICAL.getValue() ||
                        boardState[i][j] == TileState.ATTEMPT.getValue()) {
                    boardState[i][j] = TileState.EMPTY.getValue();
                }
            }
        }
    }

    private void countBallsAndHoles() {
        int balls = 0;
        int holes = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (boardState[i][j] == TileState.BALL.getValue()) {
                    balls++;
                } else if (boardState[i][j] == TileState.HOLE.getValue()) {
                    holes++;
                }
            }
        }
        System.out.println("Balls: " + balls + " Holes: " + holes);
    }

    private void generateBallsAndHoles() {

        float fullness = getBoardFullness();
        float propagationChance = basePropagation;
        int attempts = 0;
        while (fullness < fillRatio) {
            int ballSize = generateNextBall();
            if (!placeLine(ballSize, null)) {
//                System.out.println("Breaking ball: " + ballSize);
                attempts++;
                //TODO Could break on trying to fit an unusually large line in
                if (attempts >= 10) {
//                    System.out.println("Attempts failed");
                    break;
                }
            }
            int[][] newBoardState = getBoardStateCopy(boardState);
            fullness = getBoardFullness();
        }

    }

    private boolean placeLine(int lineLength, Position startPosition) { //TODO Void?
        if (lineLength == 0) {
            boardState[startPosition.getX()][startPosition.getY()] = TileState.HOLE.getValue();
            return true;
        }
        int [][] attemptedTiles = getBoardStateCopy(boardState);
        boolean done = false;
        boolean startPosProvided = (startPosition != null) ? true : false;

        while (!done) {
            if (startPosProvided) {
                done = true;
            } else {
                startPosition = getRandomOpenPosition(attemptedTiles);
                if (startPosition == null) {
//                    System.out.println("No empty spots on board.");
                    return false;
                }
                attemptedTiles[startPosition.getX()][startPosition.getY()] = TileState.ATTEMPT.getValue();
            }


            ArrayList<Direction> directionList = new ArrayList<>(Arrays.asList(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT));
            Collections.shuffle(directionList);


            for (Direction direction : directionList) {
                Position currentPosition = startPosition;
                ArrayList<Position> positionHistory = new ArrayList<>();
                Position lineStartPosition = currentPosition;
                for (int i = 0; i < lineLength; i++) {
                    Position nextPosition = nextPositionInDirection(currentPosition, direction);
                    if (isInsideBounds(nextPosition) && boardState[nextPosition.getX()][nextPosition.getY()] == TileState.EMPTY.getValue()) {
                        positionHistory.add(nextPosition);
                        currentPosition = nextPosition;
                    } else {
                        break;
                    }
                }
                if (positionHistory.size() == lineLength) {
                    if (!startPosProvided) {
                        boardState[lineStartPosition.getX()][lineStartPosition.getY()] = TileState.BALL.getValue();
                        ballNumbers[lineStartPosition.getX()][lineStartPosition.getY()] = lineLength;
                    }
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
                        if (!placeLine(lineLength - 1, lastPosition)) {
                            boardState[lastPosition.getX()][lastPosition.getY()] = TileState.HOLE.getValue();
                        }
                    } else {
                        boardState[lastPosition.getX()][lastPosition.getY()] = TileState.HOLE.getValue();
                    }
                    return true;
                }
            }
        }

        return false;
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
        double mean = Math.max(width, height)/2;
        double variance = mean/3;

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
        int min = 1;
        int max = (int) (Math.max(width, height) * 0.8);
        double mean = Math.max(width, height)/2;
        double variance = mean/3;
        while (intResult <= min || intResult > max) {
            double result = getGaussian(Math.max(width, height)/2, variance);
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

    private void printPlayableBoard(int[][] board) {
        for (int y = 0; y < height; y++) { //TODO use board height, not constant
            for (int x = 0; x < width; x++) {
                int tile = board[x][y];
                String substString;
                switch (tile) {
                    case 0 -> substString = " ";
                    case 1 -> substString = "-";
                    case 2 -> substString = "|";
                    case 3 -> substString = "X";
                    case 4 -> substString = ballNumbers[x][y] + "";
                    case 5 -> substString = "H";
                    default -> substString = "ERROR";
                }
                System.out.print( substString + " ");
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

    public int[][] getBoardState() {
        return boardState;
    }

    public int[][] getBallNumbers() {
        return ballNumbers;
    }
}
