package com.company;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.company.HeruGolfUtil.*;

import static com.company.HeruGolfUtil.positionInDirection;
import static com.company.HeruGolfUtil.printBoardState;

public class HeruGolfSolver {

    int[][] boardState;
    int[][] ballNumbers;
    int[][] solvedNumbers;
    int width;
    int height;
    int occurrencesOfMultiplePossibilites = 0;
    boolean solved = false;

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
            if (findProgress()) {
//                System.out.println("Found progress:");
//                printPlayableBoard(boardState);
            } else {
                guessProgress();
                return;
            }
        }

        solved = true;
//        System.out.println("Solved! Occ: " + occurrencesOfMultiplePossibilites);
//        printPlayableBoard(boardState);
    }

    public int getOccurrencesOfMultiplePossibilites() {
        return occurrencesOfMultiplePossibilites;
    }

    public boolean getSolved() {
        return solved;
    }

    private boolean findProgress() {
        ArrayList<Direction> directionList = new ArrayList<>(Arrays.asList(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT));
        for (int i = 0; i < boardState.length; i++) {
            for (int j = 0; j < boardState[0].length; j++) {
                if (boardState[i][j] == TileState.BALL.getValue() && solvedNumbers[i][j] == 0) {
                    int lineLength = ballNumbers[i][j];
//                    ArrayList<ArrayList<Position>> possibleLines = new ArrayList<>(4);
                    HashMap<Direction, ArrayList<Position>> possibleLines = new HashMap<>();

                    Position startPosition = new Position(i, j);
                    for (Direction direction : directionList) {
                        //TODO If lineLength = 1, check if there is only 1 adjacaent hole, mark holes as used
                        ArrayList<Position> positionHistory = new ArrayList<>();
                        Position currentPosition = startPosition;
                        for (int k = 0; k < lineLength; k++) {
                            Position nextPosition = positionInDirection(currentPosition, direction);
//                            if (isInsideBounds(nextPosition) && lineLength == 1 && positionIsUnusedHole(nextPosition)) { //If linelength is 1, only allow holes
//                                positionHistory.add(nextPosition);
//                                currentPosition = nextPosition;
//                            } else if (isInsideBounds(nextPosition) && (positionIsEmpty(nextPosition) || (k == lineLength - 1 && positionIsUnusedHole(nextPosition)))) {
//                                positionHistory.add(nextPosition);
//                                currentPosition = nextPosition;
//                            } else {
//                                break;
//                            }
                            if (isInsideBounds(nextPosition)) {
                                if (lineLength == 1) {
                                    if (positionIsUnusedHole(nextPosition)) {
                                        positionHistory.add(nextPosition);
                                        currentPosition = nextPosition;
                                    } else {
                                        break;
                                    }
                                } else if (lineLength != 1) {
                                    if (positionIsEmpty(nextPosition) || (k == lineLength - 1 && positionIsUnusedHole(nextPosition))) {
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


                    if (possibleLines.keySet().size() == 1) {

                        Direction solutionDirection = possibleLines.keySet().iterator().next();
                        ArrayList<Position> solutionLine = possibleLines.get(solutionDirection);
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
                        }
                        solvedNumbers[i][j] = 1;
                        return true;
                    } else {
                        occurrencesOfMultiplePossibilites++;
                    }
                }
            }
        }

        return false;
    }

    private boolean positionIsEmpty(Position position) {
        return boardState[position.getX()][position.getY()] == TileState.EMPTY.getValue();
    }

    private boolean positionIsUnusedHole(Position position) {
        return (boardState[position.getX()][position.getY()] == TileState.HOLE.getValue() && solvedNumbers[position.getX()][position.getY()] == 0);
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

    private void guessProgress() {
        //Find a spot with multiple solutions, generate boards for each possibility, try to solve them
    }
}
