package com.company;

import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.company.HeruGolfUtil.getBoardStateCopy;
import static com.company.HeruGolfUtil.printSavableBoardToFile;

public class Main {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    static Logger log = Logger.getLogger("com.company.Main");
    public static void main(String[] args) {

        final long startTime = System.nanoTime();
        int lapMinutes = 60;
        int keepSize = 50;

        if (args.length == 2) {
            lapMinutes = Integer.parseInt(args[0]);
            keepSize = Integer.parseInt(args[1]);
        }
        int attempts = 600000 * lapMinutes; //600000 ~ 1 min
        int solvedPuzzles = 0;
        int lap = 0;

        while (true) {
            long lapStartTime = System.nanoTime();
            lap++;
            long runningTime = System.nanoTime() - startTime;
            String runningTimeString = String.format("%02d:%02d:%02d",
                    TimeUnit.NANOSECONDS.toHours(runningTime),
                    TimeUnit.NANOSECONDS.toMinutes(runningTime) - TimeUnit.HOURS.toMinutes(TimeUnit.NANOSECONDS.toHours(runningTime)),
                    TimeUnit.NANOSECONDS.toSeconds(runningTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(runningTime)));
            log.info("Starting lap " + lap + " at time: " + LocalDateTime.now() + " with runtime: " + runningTimeString);
            int unsolvable = 0;

            List<Puzzle> puzzleToplist = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                HeruGolfGenerator golfGenerator = new HeruGolfGenerator(10, 10);
                HeruGolfSolver golfSolver = new HeruGolfSolver(getBoardStateCopy(golfGenerator.getBoardState()), getBoardStateCopy(golfGenerator.getBallNumbers()));
                if (golfSolver.getSolved()) {
                    solvedPuzzles++;
                    if (golfSolver.getScore() > 1000) {
                        puzzleToplist.add(new Puzzle(golfGenerator.boardState, golfGenerator.ballNumbers, golfSolver.getScore()));
                    }
                } else {
                    unsolvable++;
//                System.out.println("Unsolvable:");
//                printSavableBoard(golfGenerator.getBoardState(), golfGenerator.getBallNumbers());
                }

                if ((i+1) % (attempts/100) == 0) {
                    int percentDone = (i+1) / (attempts/100);
                    long tempTime = System.nanoTime() - lapStartTime;
                    //hh:mm:ss
                    String output = String.format("%02d:%02d:%02d",
                            TimeUnit.NANOSECONDS.toHours(tempTime),
                            TimeUnit.NANOSECONDS.toMinutes(tempTime) - TimeUnit.HOURS.toMinutes(TimeUnit.NANOSECONDS.toHours(tempTime)),
                            TimeUnit.NANOSECONDS.toSeconds(tempTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(tempTime)));
                    long totalTimeNano = tempTime/percentDone * 100;
                    String totalTimeString = String.format("%02d:%02d:%02d",
                            TimeUnit.NANOSECONDS.toHours(totalTimeNano),
                            TimeUnit.NANOSECONDS.toMinutes(totalTimeNano) - TimeUnit.HOURS.toMinutes(TimeUnit.NANOSECONDS.toHours(totalTimeNano)),
                            TimeUnit.NANOSECONDS.toSeconds(totalTimeNano) - TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(totalTimeNano)));
                    log.info(percentDone + " - " + "Current: " + output + " - " + "Estimated total: " + totalTimeString);

                    if (puzzleToplist.size() > 1000) {
                        trimList(puzzleToplist, keepSize);
                    }
                }
            }

            if (puzzleToplist.size() > keepSize) {
                trimList(puzzleToplist, keepSize);
            }

//        printPlayableBoard(bestBoardState, bestBoardNumbers);
//        System.out.println();

            if (puzzleToplist.size() == 0) {
                log.info("No boards created this run.");
                //continue;
            }
            String timeStamp = LocalDateTime.now().format(formatter);
            for (int i = 0; i < puzzleToplist.size(); i++) {
                Puzzle puzzle = puzzleToplist.get(i);
                String puzzleName = "B" + puzzle.getScore() + "_" + (i+1) + "_" + timeStamp;
                printSavableBoardToFile(puzzleName, puzzle.getBoardState(), puzzle.getBallState());
                //log.info(puzzleName + " " + puzzle.getScore());
            }
            int highestScore = puzzleToplist.size() == 0 ? 0 : puzzleToplist.get(0).getScore();
            log.info("Solved: " + solvedPuzzles + ", Highest score: " + highestScore + " Unsolvable: " + unsolvable + "/" + attempts);
            log.info("Toplist size: " + puzzleToplist.size());

//        int[] values = new int[]{1, 2, 5, 7, 12, 15};
//        double[] ratios = new double[results.length];
//        for (int i = 0; i < results.length; i++) {
//            if (results[i] != 0) {
//                ratios[i] = Math.round((double) results[i] / total * values[i]); //35 = estimated average
//            }
//            System.out.print(ratios[i] + " ");
//        }
            final long nanos = System.nanoTime() - startTime;
            //hh:mm:ss
            String output = String.format("%02d:%02d:%02d",
                    TimeUnit.NANOSECONDS.toHours(nanos),
                    TimeUnit.NANOSECONDS.toMinutes(nanos) - TimeUnit.HOURS.toMinutes(TimeUnit.NANOSECONDS.toHours(nanos)),
                    TimeUnit.NANOSECONDS.toSeconds(nanos) - TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(nanos)));
            log.info("Total time: " + output);
        }
    }

    private static void trimList(List<?> list, int newLength) {
        Collections.sort(list, Collections.reverseOrder());
        list.subList(newLength, list.size()).clear();
    }

    public static void testDistribution() {
        ArrayList<Integer> test = new ArrayList<>();
        HeruGolfGenerator golfGenerator = new HeruGolfGenerator(10, 10);
        for (int i = 0; i < 10000; i++) {
            test.add(golfGenerator.generateNextBall());
        }
        HashMap<Integer,Integer> results = new HashMap<>();
        for (int integer : test) {
            if (results.get(integer) != null) {
                results.put(integer, results.get(integer) + 1);
            } else {
                results.put(integer, 1);
            }
        }

        for (int i = 1; i < 10; i++) {
            System.out.print(i + "\t");
        }
        System.out.println();
        for (int i = 1; i < 10; i++) {
            System.out.print(results.getOrDefault(i, 0) + "\t");
        }

        System.out.println();
    }

    private static void printGaussianAveragesEtc() {
        ArrayList<ArrayList<Double>> results = new ArrayList<>(8);
        for (int i = 0; i < 8; i++) {
            results.add(new ArrayList<>());
        }

        for (int i = 0; i < 1000; i++) {
            double res = getGaussian(2.5, 1);
            int resInt = (int) res;


            results.get(resInt).add(res);

        }

        OptionalDouble[] averages = new OptionalDouble[8];

        for (int i = 0; i < results.size(); i++) {
            averages[i] = results.get(i).stream().mapToDouble(a -> a).average();
        }

        for (ArrayList<Double> result : results) {
            System.out.print(result.size() + " ");
        }
        System.out.println();
        int total = 0;
        for (OptionalDouble result : averages) {
            result.ifPresent(System.out::println);
        }
    }

    private static double getGaussian(double aMean, double aVariance){
        Random fRandom = new Random();
        return aMean + fRandom.nextGaussian() * aVariance;
    }
}
