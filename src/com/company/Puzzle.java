package com.company;

import java.util.Arrays;

public class Puzzle implements Comparable<Puzzle> {

    private int[][] boardState;
    private int[][] ballState;

    private int score;

    Puzzle(int[][] boardState, int[][] solvedNumbers, int score) {
        this.boardState = boardState;
        this.ballState = solvedNumbers;
        this.score = score;
    }

    public int[][] getBoardState() {

        return boardState;
    }

    public int[][] getBallState() {
        return ballState;
    }

    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof Puzzle)) return false;

        Puzzle that = (Puzzle)obj;
        boolean boardStateEqual = Arrays.deepEquals(this.boardState, that.boardState);
        boolean ballStateEqual = Arrays.deepEquals(this.ballState, that.ballState);
        return boardStateEqual && ballStateEqual;
    }

    @Override
    public int hashCode(){
        return (Arrays.deepToString(boardState) + Arrays.deepToString(ballState)).hashCode();
    }

    @Override
    public int compareTo(Puzzle that){
        if (this.score > that.score) {

            // if current object is greater,then return 1
            return 1;
        }
        else if (this.score < that.score) {

            // if current object is greater,then return -1
            return -1;
        }
        else {

            // if current object is equal to o,then return 0
            return 0;
        }
    }
}
