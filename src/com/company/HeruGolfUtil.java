package com.company;

public class HeruGolfUtil {

    public static Position positionInDirection(Position currentPosition, Direction direction) {

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
        for (int y = 0; y < board[0].length; y++) { //TODO use board height, not constant
            for (int x = 0; x < board.length; x++) {
                System.out.print(board[x][y] + " ");
            }
            System.out.println();
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
