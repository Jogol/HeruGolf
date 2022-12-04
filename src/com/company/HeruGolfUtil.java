package com.company;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HeruGolfUtil {

    public static Position nextPositionInDirection(Position currentPosition, Direction direction) {

        Position newPosition;
        switch (direction) {
            case UP -> newPosition = new Position(currentPosition.getX(), currentPosition.getY() - 1);
            case RIGHT -> newPosition = new Position(currentPosition.getX() + 1, currentPosition.getY());
            case DOWN -> newPosition = new Position(currentPosition.getX(), currentPosition.getY() + 1);
            case LEFT -> newPosition = new Position(currentPosition.getX() - 1, currentPosition.getY());
            default -> { return null; }
        }

        return newPosition;
    }

    public static void printBoardState(int[][] board) {
        for (int y = 0; y < board[0].length; y++) {
            for (int x = 0; x < board.length; x++) {
                System.out.print(board[x][y] + " ");
            }
            System.out.println();
        }
    }

    public static void printSavableBoard(int[][] boardState, int[][] ballNumbers) {
        for (int y = 0; y < boardState[0].length; y++) {
            StringBuilder lineString = new StringBuilder();
            for (int x = 0; x < boardState.length; x++) {
                int tile = boardState[x][y];
                String substString;
                if (tile == 4) {
                    substString = ballNumbers[x][y] + 10 + "";
                } else {
                    substString = tile + "";
                }
                lineString.append(substString).append("\t");
            }
            System.out.println(lineString.substring(0, lineString.length() - 1));
        }
    }

    public static void printSavableBoardToFile(String fileName, int[][] boardState, int[][] ballNumbers) { //TODO Flip?
        File file = new File("C:\\Users\\Jonat\\Dev\\IdeaProjects\\HeruGolf\\src\\GeneratedBoards\\" + fileName + ".txt");
        BufferedWriter writer = null;
        StringBuilder str = new StringBuilder();
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (int x = 0; x < boardState[0].length; x++) {
                StringBuilder lineString = new StringBuilder();
                for (int y = 0; y < boardState.length; y++) {
                    int tile = boardState[x][y];
                    String substString;
                    if (tile == 4) {
                        substString = ballNumbers[x][y] + 10 + "";
                    } else {
                        substString = tile + "";
                    }
                    lineString.append(substString).append("\t");
                }
                str.append(lineString.substring(0, lineString.length() - 1)).append("\n");
            }
            str.setLength(str.length() - 1);
            writer.write(str.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Print fucky");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Close fucky");
                }
            }
        }

    }

    public static void printPlayableBoard(int[][] boardState, int[][] ballNumbers) {
        for (int y = 0; y < boardState[0].length; y++) { //TODO use board height, not constant
            for (int x = 0; x < boardState.length; x++) {
                int tile = boardState[x][y];
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

    public static int[][] getBoardStateCopy(int[][] boardState) {
        int [][] copy = new int[boardState.length][];
        for(int i = 0; i < boardState.length; i++) {
            copy[i] = boardState[i].clone();
        }
        return copy;
    }

    enum TileState {
        EMPTY(0),
        HORIZONTAL(1),
        VERTICAL(2),
        WATER(3),
        BALL(4),
        HOLE(5),
        ATTEMPT(6),
        SOLVED_HOLE(7),
        WATER_PLUS_HORIZONTAL(31),
        WATER_PLUS_VERTICAL(32);

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
