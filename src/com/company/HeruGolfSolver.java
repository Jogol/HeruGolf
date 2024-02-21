package com.company;

import java.util.*;

import static com.company.HeruGolfUtil.*;

public class HeruGolfSolver {

    int[][] boardState;
    int[][] ballNumbers;
    int[][] solvedNumbers;
    HashMap<Position, Position> possibleHoleBallPairs;
    HashMap<Position, Position> ballSource; //Key: lastPosition, value: currentPosition
    HashMap<Position, Integer> ballsForHoleCounter;
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
        ballSource = new HashMap<>();

        boolean gotHoleProgress = false;
        while (!isSolved()) {
            possibleHoleBallPairs = new HashMap<>();
            ballsForHoleCounter = new HashMap<>();
//            System.out.println("----------------------");
//            printPlayableBoard(boardState);
//            System.out.println("----------------------");
            if (findProgressFromBall()) {
                //System.out.println("Ball progress");
//                printPlayableBoard(boardState);
            } else if (findProgressFromHole()) { //TODO Add difficulty scoring
                //Found progress
                //System.out.println("Hole progress");
                //gotHoleProgress = true;
            } else {
                //guessProgress(); //TODO Currently not allowing puzzles that require guesses
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

    public int getScore() {
        return score;
    }

    public boolean getSolved() {
        return solved;
    }

    private boolean findProgressFromBall() { //TODO Find progress from a hole (eg only one ball can go to that hole)
        score++; //Just starting another lap speaks for the complexity

        //Iterate the board, and for each ball that isn't solved do
        for (int i = 0; i < boardState.length; i++) {
            for (int j = 0; j < boardState[0].length; j++) {
                if (boardState[i][j] == TileState.BALL.getValue() && solvedNumbers[i][j] == 0) {
                    int lineLength = ballNumbers[i][j];

                    Position currentPosition = new Position(i, j);
                    //Which directions around this position have a valid move with current length?
                    HashMap<Direction, ArrayList<Position>> traversableLines = getTraversableLines(currentPosition, lineLength);
                    //System.out.println("Num of trav: " + traversableLines.size());
                    HashMap<Direction, ArrayList<Position>> endingInHoleLines;
                    if (traversableLines.keySet().size() == 1) {
                        endingInHoleLines = traversableLines;
                    } else {
                        //Which of these directions actually end in a hole?
                        endingInHoleLines = keepLinesEndingInAHole(currentPosition, traversableLines, lineLength);
                    }
                    //System.out.println("Num of ending: " + endingInHoleLines.size());

                    //If only one direction ends in a hole, that must be the correct path
                    if (endingInHoleLines.keySet().size() == 1) {
                        //This means we only have 1 direction to go that ends in a hole
                        score -= 5; //Only having 1 option is "bad" but necessary eventually
                        Direction solutionDirection = endingInHoleLines.keySet().iterator().next();
                        ArrayList<Position> solutionLine = endingInHoleLines.get(solutionDirection);

                        //We only do moves that are guaranteed, so here we draw that move into the board (only 1 move)
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
                            //If we aren't done yet, we pretend this is the starting point for a new ball
                            boardState[lastPosition.getX()][lastPosition.getY()] = TileState.BALL.getValue();
                            ballNumbers[lastPosition.getX()][lastPosition.getY()] = lineLength - 1;
                            ballSource.put(lastPosition, currentPosition);
                        } else {
                            //If lastPosition is a hole, mark it as solved, unsure why
                            solvedNumbers[lastPosition.getX()][lastPosition.getY()] = 1;
                            score-= 10; //If a final solution is too quick to end it won't generate many points above and thus becoming net negative here.
                        }
                        //Since we succeeded in making a move, mark the ball that went here as solved
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

    /***
     * Looks in all directions around start position and return all path that allow for 1 step
     * @param startPosition the position of the ball before moving
     * @param lineLength the number on the ball going here
     * @return
     */
    private HashMap<Direction, ArrayList<Position>> getTraversableLines(Position startPosition, int lineLength) {
        HashMap<Direction, ArrayList<Position>> possibleLines = new HashMap<>();

        for (Direction direction : directionList) {
            ArrayList<Position> positionHistory = new ArrayList<>();
            Position currentPosition = startPosition;
            for (int k = 0; k < lineLength; k++) {
                Position nextPosition = nextPositionInDirection(currentPosition, direction);
                if (isInsideBounds(nextPosition)) {
                    boolean isLastPosition = lineLength - 1 == k;
                    if (isValidPositionForPath(nextPosition, isLastPosition) || (isLastPosition && positionIsUnusedHole(nextPosition))) {
                        positionHistory.add(nextPosition);
                        currentPosition = nextPosition;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (positionHistory.size() == lineLength) {
                possibleLines.put(direction, positionHistory);
            }
        }
        return possibleLines;
    }

    /***
     * Among the lines that allow for 1 step, return the ones that actually end in a hole eventually
     * This is checked recursively
     * @param traversableLines
     * @param lineLength
     * @return
     */
    private HashMap<Direction, ArrayList<Position>> keepLinesEndingInAHole(Position originalPos, HashMap<Direction, ArrayList<Position>> traversableLines, int lineLength) {
        HashMap<Direction, ArrayList<Position>> filteredLines = new HashMap<>();
        for (Direction solutionDirection : traversableLines.keySet()) {
            ArrayList<Position> solutionLine = traversableLines.get(solutionDirection);
            Position lastPosition = solutionLine.get(solutionLine.size() - 1);

            int[][] tempMoves = new int[width][height];
            for (int k = 0; k < solutionLine.size() - 1; k++) {
                Position position = solutionLine.get(k);
                tempMoves[position.getX()][position.getY()] = 1;
            }

            if (isSolveableBranch(originalPos, lastPosition, tempMoves, lineLength - 1)) {
                filteredLines.put(solutionDirection, traversableLines.get(solutionDirection));
                score += 5; //The more simultaneously possible routes the better
            }
        }
        return filteredLines;
    }

    /***
     *
     * @param startPosition Position on board from which to solve
     * @param tempMoves To keep track of done moves so that we don't loop back on an already used space. Only tracks used (1) or not used(0)
     * @param lineLength How long the line we are trying to fit is
     * @return true if we were able to add the line
     */
    private boolean isSolveableBranch(Position originalPosition, Position startPosition, int[][] tempMoves, int lineLength) {
        //TODO BUG: we check if there is any hole, we don't check all holes
        //This means we don't add all holes to ballForHoleCounter

        //If we are on top of a hole already it must be a valid and ending move
        if (boardState[startPosition.getX()][startPosition.getY()] == TileState.HOLE.getValue()) {
            possibleHoleBallPairs.put(startPosition, originalPosition); //TODO Detta blir sista steget innan hålet, men är inte garanterat att vi ritat ut hela vägen dit
            //TODO Make the value an object that holds count and ball origin to speed things up?
            //Allegedly makes value 1 if none existed, otherwise adds existing + 1
            //System.out.println("Orig: " + originalPosition + " Start: " + startPosition + " Counter: " + ballsForHoleCounter.get(startPosition) + 1);
            //printTempMoves(tempMoves);
            ballsForHoleCounter.merge(startPosition, 1, Integer::sum);
            return true;
        } else if (lineLength == 0) { //If we are out of moves, this was a false path
            return false;
        }
        //printTempMoves(tempMoves);

        //Checking number of possible lines
        HashMap<Direction, ArrayList<Position>> possibleLines = new HashMap<>();
        for (Direction direction : directionList) {
            ArrayList<Position> positionHistory = new ArrayList<>();
            Position currentPosition = startPosition;
            for (int k = 0; k < lineLength; k++) {
                Position nextPosition = nextPositionInDirection(currentPosition, direction);
                if (isInsideBounds(nextPosition) && tempMoves[nextPosition.getX()][nextPosition.getY()] != 1) {
                    boolean isLastPosition = lineLength - 1 == k;
                    if (isValidPositionForPath(nextPosition, isLastPosition) || (isLastPosition && positionIsUnusedHole(nextPosition))) {
                        positionHistory.add(nextPosition);
                        currentPosition = nextPosition;
                    } else {
                        break;
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

                int[][] tempCopy = Arrays.stream(tempMoves).map(int[]::clone).toArray(int[][]::new);
                if (isSolveableBranch(originalPosition, lastPosition, tempCopy, lineLength - 1)) {
                    isSolveable = true;
                    //break;
                }
            }

            return isSolveable;
        }
    }

    private boolean findProgressFromHole() { //TODO Implement
        score += 100;
//        Collection<Position> values = possibleBallHolePairs.values();
//        List<Position> uniqueHoleList = values.stream().filter(i -> Collections.frequency(values, i) == 1).toList();
        for (Position hole : ballsForHoleCounter.keySet()) {
            if (ballsForHoleCounter.get(hole) == 1) {
                //System.out.println("Hole: " + hole);
                Position ballPos = possibleHoleBallPairs.get(hole);
                do {
                    if (boardState[ballPos.getX()][ballPos.getY()] == TileState.BALL.getValue()) {
                        if (makeProgress(ballPos, hole)) {
                            return true;
                        }
                    } else {
                        Position nextPos = ballSource.get(ballPos);
                        if (nextPos == null) {
                            if (makeProgress(ballPos, hole)) {
                                return true;
                            }
                        } else {
                            ballPos = nextPos;
                        }
                    }
                } while (true);

            }
        }
        return false;
    }

    private boolean makeProgress(Position startPos, Position goalHole) {
        int lineLength = ballNumbers[startPos.getX()][startPos.getY()];
        int[][] tempMoves = new int[width][height];
        if (isCorrectBranch(startPos, goalHole, lineLength, tempMoves)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCorrectBranch(Position currentPos, Position goalHole, int lineLength, int[][] tempMoves) {
        if (currentPos.equals(goalHole)) {
            return true;
        } else if (lineLength == 0 || boardState[currentPos.getX()][currentPos.getY()] == TileState.HOLE.getValue()) {
            return false;
        }

        HashMap<Direction, ArrayList<Position>> traversableLines = getTraversableLines(currentPos, lineLength);
        for (Direction direction : traversableLines.keySet()) {
            ArrayList<Position> solutionLine = traversableLines.get(direction);
            Position firstStep = solutionLine.get(0);
            if (tempMoves[firstStep.getX()][firstStep.getY()] == 1) {
                continue;//TODO Olika riktningar delar samma tempMoves
            }
            Position lastPos = solutionLine.get(solutionLine.size() - 1);

            for (int k = 0; k < solutionLine.size() - 1; k++) {
                Position stepPos = solutionLine.get(k);
                tempMoves[stepPos.getX()][stepPos.getY()] = 1;
            }

            int[][] tempCopy = Arrays.stream(tempMoves).map(int[]::clone).toArray(int[][]::new);
            if (isCorrectBranch(lastPos, goalHole, lineLength - 1, tempCopy)) {

                //We only do moves that are guaranteed, so here we draw that move into the board
                for (int k = 0; k < solutionLine.size() - 1; k++) {
                    Position pos = solutionLine.get(k);
                    if (direction == Direction.UP || direction == Direction.DOWN) {
                        boardState[pos.getX()][pos.getY()] = TileState.VERTICAL.getValue();
                    } else if (direction == Direction.RIGHT || direction == Direction.LEFT) {
                        boardState[pos.getX()][pos.getY()] = TileState.HORIZONTAL.getValue();
                    }
                }

                //TODO Fix below, set currentPos to ball and solved
                if (!(boardState[lastPos.getX()][lastPos.getY()] == TileState.HOLE.getValue())) {
                    //If we aren't done yet, we pretend this is the starting point for a new ball
                    boardState[lastPos.getX()][lastPos.getY()] = TileState.BALL.getValue();
                    ballNumbers[lastPos.getX()][lastPos.getY()] = lineLength - 1;
                    ballSource.put(lastPos, currentPos);
                } else {
                    //If lastPosition is a hole, mark it as solved, unsure why
                    solvedNumbers[lastPos.getX()][lastPos.getY()] = 1;
                    score-= 10; //If a final solution is too quick to end it won't generate many points above and thus becoming net negative here.
                }
                //Since we succeeded in making a move, mark the ball that went here as solved
                solvedNumbers[currentPos.getX()][currentPos.getY()] = 1;
                return true;
            }
        }
        return false;
    }

    private boolean isValidPositionForPath(Position nextPosition, boolean isLastPosition) {
        return (positionIsEmpty(nextPosition) || (!isLastPosition && positionIsWater(nextPosition)));
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
                    case 0 :
                        substString = " ";
                        break;
                    case 1 :
                        substString = "-";
                        break;
                    case 2 :
                        substString = "|";
                        break;
                    case 3 :
                        substString = "X";
                        break;
                    case 4 :
                        substString = ballNumbers[x][y] + "";
                        break;
                    case 5 :
                        if (solvedNumbers[x][y] == 1) {
                            substString = "S";
                        } else {
                            substString = "H";
                        }
                        break;
                    default :
                        substString = "ERROR";
                }
                System.out.print( substString + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void printTempMoves(int[][] board) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tile = board[x][y];
                String substString;
                switch (tile) {
                    case 0 :
                        substString = "O";
                        break;
                    case 1 :
                        substString = "X";
                        break;
                    default :
                        substString = "ERROR";
                }
                System.out.print( substString + " ");
            }
            System.out.println();
        }
        System.out.println();
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
