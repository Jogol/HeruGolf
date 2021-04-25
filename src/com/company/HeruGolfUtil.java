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
