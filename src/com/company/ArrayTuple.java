package com.company;

import java.util.ArrayList;

public class ArrayTuple {

    private int[][] boardState;
    private int[][] solvedNumbers;

    ArrayTuple(int[][] boardState, int[][] solvedNumbers) {
        this.boardState = boardState;
        this.solvedNumbers = solvedNumbers;
    }

    public int[][] getBoardState() {

        return boardState;
    }

    public int[][] getSolvedNumbers() {
        return solvedNumbers;
    }
}
