package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.company.HeruGolfUtil.*;

public class HeruGolfSolver {

    int[][] boardState;
    int[][] ballNumbers;
    int[][] solvedNumbers;
    int width;
    int height;
    int occurrencesOfMultiplePossibilites = 0;
    int score = 0;
    boolean solved = false;
    boolean tryGuessingFlag = false;
    ArrayList<Direction> directionList = new ArrayList<>(Arrays.asList(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT));

    /***
     *
     * @param boardState Boardstate
     * @param ballNumbers The values of the balls
     */
    HeruGolfSolver(int[][] boardState, int [][] ballNumbers) {
        this.boardState = boardState;
        this.ballNumbers = ballNumbers;

        width = boardState.length;
        height = boardState[0].length;

        solvedNumbers = new int[width][height];

        int attemps = 0;

        while (!isSolved()) {
            if (findProgressFromBall()) {
//                System.out.println("Found progress:");
//                printPlayableBoard(boardState);
            } else {
                //findProgressFromHole();
                //guessProgress(); //TODO Currently not allowing puzzles that require guesses
                return;
            }
        }

        solved = true;
//        System.out.println("Solved! Occ: " + occurrencesOfMultiplePossibilites);
//        printPlayableBoard(boardState);
    }

    private boolean findProgressFromHole() { //TODO Implement
        return false;
    }

    public int getOccurrencesOfMultiplePossibilites() {
        return occurrencesOfMultiplePossibilites;
    }

    public int getScore() {
        return score;
    }

    public boolean getSolved() {
        return solved;
    }

    private boolean findProgressFromBall() { //TODO Find progress from a hole (eg only one ball can go to that hole)
        score++; //Just starting another lap speaks for the complexity
        for (int i = 0; i < boardState.length; i++) {
            for (int j = 0; j < boardState[0].length; j++) {
                if (boardState[i][j] == TileState.BALL.getValue() && solvedNumbers[i][j] == 0) {
                    int lineLength = ballNumbers[i][j];

                    HashMap<Direction, ArrayList<Position>> possibleLines = new HashMap<>();

                    Position startPosition = new Position(i, j);
                    for (Direction direction : directionList) {
                        ArrayList<Position> positionHistory = new ArrayList<>();
                        Position currentPosition = startPosition;
                        for (int k = 0; k < lineLength; k++) {
                            Position nextPosition = nextPositionInDirection(currentPosition, direction);
                            if (isInsideBounds(nextPosition)) {
                                if (lineLength == 1) {
                                    if (positionIsUnusedHole(nextPosition)) {
                                        positionHistory.add(nextPosition);
                                        currentPosition = nextPosition;
                                    } else {
                                        break;
                                    }
                                } else if (lineLength != 1) {
                                    boolean isLastPosition = lineLength - 1 == k;
                                    if (isValidPositionForPath(nextPosition, isLastPosition) || (isLastPosition && positionIsUnusedHole(nextPosition))) {//TODO New rule: can jump over water
                                        positionHistory.add(nextPosition);
                                        currentPosition = nextPosition;
                                    } else {
                                        break;
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                        if (positionHistory.size() == lineLength) {
                            possibleLines.put(direction, positionHistory);
                        }
                    }

                    HashMap<Direction, ArrayList<Position>> filteredLines = new HashMap<>();
                    for (Direction solutionDirection : possibleLines.keySet()) {
                        ArrayList<Position> solutionLine = possibleLines.get(solutionDirection);
                        Position lastPosition = solutionLine.get(solutionLine.size() - 1);

                        int[][] tempMoves = new int[width][height];
                        for (int k = 0; k < solutionLine.size() - 1; k++) {
                            Position position = solutionLine.get(k);
                            tempMoves[position.getX()][position.getY()] = 1;
                        }

                        if (isSolveableBranch(lastPosition, tempMoves, lineLength - 1)) {
                            filteredLines.put(solutionDirection, possibleLines.get(solutionDirection));
                            score += 5; //The more simultaneously possible routes the better
                        }
                    }

                    if (filteredLines.keySet().size() == 1) {
                        score -= 5; //Only having 1 option is "bad" but necessary eventually
                        Direction solutionDirection = filteredLines.keySet().iterator().next();
                        ArrayList<Position> solutionLine = filteredLines.get(solutionDirection);
                        for (int k = 0; k < solutionLine.size() - 1; k++) {
                            Position position = solutionLine.get(k);
                            if (solutionDirection == Direction.UP || solutionDirection == Direction.DOWN) {
                                boardState[position.getX()][position.getY()] = TileState.VERTICAL.getValue();
                            } else if (solutionDirection == Direction.RIGHT || solutionDirection == Direction.LEFT) {
                                boardState[position.getX()][position.getY()] = TileState.HORIZONTAL.getValue();
                            }
                        }

                        Position lastPosition = solutionLine.get(solutionLine.size() - 1);
                        if (!(boardState[lastPosition.getX()][lastPosition.getY()] == TileState.HOLE.getValue())) {
                            boardState[lastPosition.getX()][lastPosition.getY()] = TileState.BALL.getValue();
                            ballNumbers[lastPosition.getX()][lastPosition.getY()] = lineLength - 1;
                        } else {
                            solvedNumbers[lastPosition.getX()][lastPosition.getY()] = 1;
                            score-= 10; //If a final solution is too quick to end it won't generate many points above and thus becoming net negative here.
                        }
                        solvedNumbers[i][j] = 1;
                        return true;
                    } else {
                        occurrencesOfMultiplePossibilites++;
                        if (tryGuessingFlag) {

                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isValidPositionForPath(Position nextPosition, boolean isLastPosition) {
        return (positionIsEmpty(nextPosition) || (!isLastPosition && positionIsWater(nextPosition)));
    }

    /***
     *
     * @param startPosition Position on board from which to solve
     * @param tempMoves To keep track of done moves so that we don't loop back on an already used space. Only tracks used (1) or not used(0)
     * @param lineLength How long the line we are trying to fit is
     * @return true if we were able to add the line
     */
    private boolean isSolveableBranch(Position startPosition, int[][] tempMoves, int lineLength) {

        //If we are on top of a hole already it must be a valid and ending move
        if (boardState[startPosition.getX()][startPosition.getY()] == TileState.HOLE.getValue()) {
            return true;
        } else if (lineLength == 0) { //If we are out of moves, this was a false path
            return false;
        }

        //Checking number of possible lines
        HashMap<Direction, ArrayList<Position>> possibleLines = new HashMap<>();
        for (Direction direction : directionList) {
            ArrayList<Position> positionHistory = new ArrayList<>();
            Position currentPosition = startPosition;
            for (int k = 0; k < lineLength; k++) {
                Position nextPosition = nextPositionInDirection(currentPosition, direction);
                if (isInsideBounds(nextPosition) && tempMoves[nextPosition.getX()][nextPosition.getY()] != 1) {
                    if (lineLength == 1) {
                        if (positionIsUnusedHole(nextPosition)) {
                            positionHistory.add(nextPosition);
                            currentPosition = nextPosition;
                        } else {
                            break;
                        }
                    } else if (lineLength != 1) {
                        boolean isLastPosition = lineLength - 1 == k;
                        if (isValidPositionForPath(nextPosition, isLastPosition) || (isLastPosition && positionIsUnusedHole(nextPosition))) {//TODO New rule: can jump over water
                            positionHistory.add(nextPosition);
                            currentPosition = nextPosition;
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }
            }
            if (positionHistory.size() == lineLength) {
                possibleLines.put(direction, positionHistory);
            }
        }

        //Solving lines
        if (possibleLines.keySet().size() == 0) {
            return false;
        } else {
            boolean isSolveable = false;
            for (Direction solutionDirection : possibleLines.keySet()) {
                ArrayList<Position> solutionLine = possibleLines.get(solutionDirection);
                Position lastPosition = solutionLine.get(solutionLine.size() - 1);

                for (int k = 0; k < solutionLine.size() - 1; k++) {
                    Position position = solutionLine.get(k);
                    tempMoves[position.getX()][position.getY()] = 1;
                }

                if (isSolveableBranch(lastPosition, tempMoves, lineLength - 1)) {
                    isSolveable = true;
                }
            }

            return isSolveable;
        }
    }

    private boolean positionIsEmpty(Position position) {
        return boardState[position.getX()][position.getY()] == TileState.EMPTY.getValue();
    }

    private boolean positionIsWater(Position position) {
        return boardState[position.getX()][position.getY()] == TileState.WATER.getValue();
    }

    private boolean positionIsUnusedHole(Position position) {
        return (boardState[position.getX()][position.getY()] == TileState.HOLE.getValue() && solvedNumbers[position.getX()][position.getY()] == 0);
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

    private boolean isSolved() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (boardState[i][j] == TileState.BALL.getValue()) {
                    if (solvedNumbers[i][j] == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //TODO Put in Util?
    private boolean isInsideBounds(Position position) {
        if (position.getX() >= 0 && position.getX() < width && position.getY() >= 0 && position.getY() < height) {
            return true;
        } else {
            return false;
        }
    }
}
