package com.company;

import java.io.*;
import java.util.*;

import static com.company.HeruGolfUtil.*;


public class HeruGolfGenerator {

    int width;
    int height;
    float waterRatio = 0.2f;
    float fillRatio = 0.8f;
    float basePropagation = 0.9f;
    int maxAttempts = 50;
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

        generateWaterHazards();
        //boardState = readBoardStateFromFile("C:\\Users\\Jonat\\Dev\\IdeaProjects\\HeruGolf\\src\\ManualWater\\symmetricWater.txt");
        generateBallsAndHoles();

        removeSolution();
    }

    private void removeSolution() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (boardState[i][j] == TileState.HORIZONTAL.getValue() ||
                        boardState[i][j] == TileState.VERTICAL.getValue() ||
                        boardState[i][j] == TileState.ATTEMPT.getValue()) {
                    boardState[i][j] = TileState.EMPTY.getValue();
                } else if (boardState[i][j] == TileState.WATER_PLUS_HORIZONTAL.getValue() || boardState[i][j] == TileState.WATER_PLUS_VERTICAL.getValue()) {
                    boardState[i][j] = TileState.WATER.getValue();
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
        int attempts = 0;
        while (fullness < fillRatio) {
            int ballSize = generateNextBall();
            if (!placeLine(ballSize, null)) {
//                System.out.println("Breaking ball: " + ballSize);
                attempts++;
                //TODO Could break on trying to fit an unusually large line in
                if (attempts >= maxAttempts) {
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
        boolean startPosProvided = startPosition != null; //False the first time we go in here

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
                    boolean isLastPosition = lineLength - 1 == i;
                    if (isInsideBounds(nextPosition) && isAValidPath(nextPosition, isLastPosition)) {
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
                            if (boardState[position.getX()][position.getY()] == TileState.WATER.getValue()) { //TODO Seems kinda slow
                                boardState[position.getX()][position.getY()] = TileState.WATER_PLUS_VERTICAL.getValue();
                            } else {
                                boardState[position.getX()][position.getY()] = TileState.VERTICAL.getValue();
                            }
                        } else if (direction == Direction.RIGHT || direction == Direction.LEFT) {
                            if (boardState[position.getX()][position.getY()] == TileState.WATER.getValue()) {
                                boardState[position.getX()][position.getY()] = TileState.WATER_PLUS_HORIZONTAL.getValue();
                            } else {
                                boardState[position.getX()][position.getY()] = TileState.HORIZONTAL.getValue();
                            }
                        }
                    }
                    float fullness = getBoardFullness();
                    float propagationChance = basePropagation - (fullness/10);
                    Position lastPosition = positionHistory.get(positionHistory.size()-1);
                    if (rand.nextFloat() < propagationChance) { //Propagate
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

    private boolean isAValidPath(Position nextPosition, boolean isLastPosition) {
        boolean isEmpty = boardState[nextPosition.getX()][nextPosition.getY()] == TileState.EMPTY.getValue();
        boolean isWater = boardState[nextPosition.getX()][nextPosition.getY()] == TileState.WATER.getValue();
        return (isEmpty || (isWater && !isLastPosition));
    }

    private boolean isInsideBounds(Position position) {
        return position != null && position.getX() >= 0 && position.getX() < width && position.getY() >= 0 && position.getY() < height;
    }

    private Direction getRandomDirection() {
        Random rand = new Random();
        int val = rand.nextInt(4);

        return switch (val) {
            case 0 -> Direction.UP;
            case 1 -> Direction.RIGHT;
            case 2 -> Direction.DOWN;
            case 3 -> Direction.LEFT;
            default -> null;
        };
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
        double mean = Math.max((double) width, height)/2;
        double variance = mean/3;

        for (int i = 0; i < 1000; i++) {
            double res = getGaussian(mean, variance);
            int resInt = (int) res;

            if (resInt >= 0 && resInt < results.size()) {
                results.get(resInt).add(res);
            }

            int tilesUsed = estimateTilesUsed(results);
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

    public int generateNextBall() {
        int intResult = 0;
        int min = 1;
        int max = (int) (Math.max(width, height) * 0.8);
        double mean = Math.max((double) width, height)/2;
        double variance = mean/6;
        while (intResult < min || intResult > max) {
            double result = getGaussian(Math.max((double) width, height)/2, variance);
            intResult = (int) result - 1;
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
        for (int y = 0; y < height; y++) {
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

    private void generateWaterHazards() {
        int waterBlockCount = Math.round(width * height * waterRatio);

        Random rand = new Random();
        for (int i = 0; i < waterBlockCount; i++) {
            boardState[rand.nextInt(width)][rand.nextInt(height)] = TileState.WATER.getValue();
        }
    }

    private int[][] readBoardStateFromFile(String fileName) {
        ArrayList<String[]> data = new ArrayList<>(); //initializing a new ArrayList out of String[]'s
        try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
                data.add(lineItems); //adding the splitted line array to the ArrayList
            }
            int[][] matrix = new int[data.get(0).length][data.size()];
            for (int i = 0; i < matrix[0].length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    matrix[j][i] = Integer.parseInt(data.get(i)[j]);
                }
            }
            return matrix;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
