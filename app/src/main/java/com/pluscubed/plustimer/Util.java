package com.pluscubed.plustimer;

import android.content.Context;

import com.pluscubed.plustimer.model.PuzzleType;
import com.pluscubed.plustimer.model.Solve;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Utitilies class
 */
public class Util {


    /**
     * Returns a String containing the date and time according the device's settings and locale from
     * the timestamp
     *
     * @param applicationContext the application context
     * @param timestamp          the timestamp to convert into a date & time String
     * @return the String converted from the timestamp
     * @see android.text.format.DateFormat
     * @see java.text.DateFormat
     */
    public static String timeDateStringFromTimestamp(Context applicationContext, long timestamp) {
        String timeDate;
        String androidDateTime = android.text.format.DateFormat.getDateFormat(applicationContext)
                .format(new Date(timestamp)) + " " +
                android.text.format.DateFormat.getTimeFormat(applicationContext)
                        .format(new Date(timestamp));
        String javaDateTime = DateFormat.getDateTimeInstance().format(new Date(timestamp));
        String AmPm = "";
        if (!Character.isDigit(androidDateTime.charAt(androidDateTime.length() - 1))) {
            if (androidDateTime.contains(
                    new SimpleDateFormat().getDateFormatSymbols().getAmPmStrings()[Calendar.AM])) {
                AmPm = " " + new SimpleDateFormat().getDateFormatSymbols()
                        .getAmPmStrings()[Calendar.AM];
            } else {
                AmPm = " " + new SimpleDateFormat().getDateFormatSymbols()
                        .getAmPmStrings()[Calendar.PM];
            }
            androidDateTime = androidDateTime.replace(AmPm, "");
        }
        if (!Character.isDigit(javaDateTime.charAt(javaDateTime.length() - 1))) {
            javaDateTime = javaDateTime.replace(" " + new SimpleDateFormat().getDateFormatSymbols()
                    .getAmPmStrings()[Calendar.AM], "");
            javaDateTime = javaDateTime.replace(" " + new SimpleDateFormat().getDateFormatSymbols()
                    .getAmPmStrings()[Calendar.PM], "");
        }
        javaDateTime = javaDateTime.substring(javaDateTime.length() - 3);
        timeDate = androidDateTime.concat(javaDateTime);
        return timeDate.concat(AmPm);
    }

    /**
     * Returns a String containing hours, minutes, and seconds (to the millisecond) from a duration
     * in nanoseconds.
     *
     * @param nanoseconds the duration to be converted
     * @return the String converted from the nanoseconds
     */
    //TODO: Localization of timeStringFromNs
    public static String timeStringFromNs(long nanoseconds, boolean enableMilliseconds) {
        String[] array = timeStringsFromNsSplitByDecimal(nanoseconds, enableMilliseconds);
        return array[0] + "." + array[1];
    }

    public static String timeStringSecondsFromNs(long nanoseconds, boolean enableMilliseconds) {
        double seconds;
        if (enableMilliseconds) {
            seconds = Math.round(nanoseconds / 1000000000.0 * 1000.0) / 1000.0;
        } else {
            seconds = Math.round(nanoseconds / 1000000000.0 * 100.0) / 100.0;
        }
        if (seconds == (long) seconds)
            return String.format("%d", (long) seconds);
        else
            return String.valueOf(seconds);
    }


    public static String[] timeStringsFromNsSplitByDecimal(long nanoseconds, boolean enableMilliseconds) {
        String[] array = new String[2];

        int hours = (int) ((nanoseconds / 1000000000L / 60 / 60) % 24);
        int minutes = (int) ((nanoseconds / 1000000000L / 60) % 60);
        int seconds = (int) ((nanoseconds / 1000000000L) % 60);

        // 0x is saying add zeroes for how many digits
        if (hours != 0) {
            array[0] = String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes != 0) {
            array[0] = String.format("%d:%02d", minutes, seconds);
        } else {
            array[0] = String.format("%d", seconds);
        }

        if (enableMilliseconds) {
            array[1] = String.format("%03d", (int) (((nanoseconds / 1000000.0) % 1000.0) + 0.5));
        } else {
            array[1] = String.format("%02d", (int) (((nanoseconds / 10000000.0) % 100.0) + 0.5));
        }

        return array;
    }

    /**
     * Gets a list of times (calculated with +2s) from the list of {@code Solve}s, excluding DNFs.
     * If no times are found, an empty list is returned.
     *
     * @param list the list of solves to extract times from
     * @return the list of nanoseconds of times
     */
    public static List<Long> getListTimeTwoNoDnf(List<Solve> list) {
        ArrayList<Long> timeTwo = new ArrayList<Long>();
        for (Solve i : list) {
            if (!(i.getPenalty() == Solve.Penalty.DNF)) {
                timeTwo.add(i.getTimeTwo());
            }
        }
        return timeTwo;
    }

    /**
     * Gets the best {@code Solve} out of the list (lowest time).
     * <p/>
     * If the list contains no solves, null is returned. If the list contains only DNFs, the last
     * DNF solve is returned.
     *
     * @param list the list of solves, not empty
     * @return the solve with the lowest time
     */
    public static Solve getBestSolveOfList(List<Solve> list) {
        List<Solve> solveList = new ArrayList<Solve>(list);
        if (solveList.size() > 0) {
            Collections.reverse(solveList);
            List<Long> times = getListTimeTwoNoDnf(solveList);
            if (times.size() > 0) {
                long bestTimeTwo = Collections.min(times);
                for (Solve i : solveList) {
                    if (!(i.getPenalty() == Solve.Penalty.DNF) && i.getTimeTwo() == bestTimeTwo) {
                        return i;
                    }
                }

            }
            return solveList.get(0);
        }
        return null;
    }

    /**
     * Gets the worst {@code Solve} out of the list (highest time).
     * <p/>
     * If the list contains DNFs, the last DNF solve is returned.
     * If the list contains no solves, null is returned.
     *
     * @param list the list of solves, not empty
     * @return the solve with the highest time
     */
    public static Solve getWorstSolveOfList(List<Solve> list) {
        List<Solve> solveList = new ArrayList<Solve>(list);
        if (solveList.size() > 0) {
            Collections.reverse(solveList);
            for (Solve i : solveList) {
                if (i.getPenalty() == Solve.Penalty.DNF) {
                    return i;
                }
            }
            List<Long> times = getListTimeTwoNoDnf(solveList);
            if (times.size() > 0) {
                long worstTimeTwo = Collections.max(times);
                for (Solve i : solveList) {
                    if (i.getTimeTwo() == worstTimeTwo) {
                        return i;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Converts a sequence of moves in WCA notation to SiGN notation
     *
     * @param wca the sequence of moves in WCA notation
     * @return the converted sequence of moves in SiGN notation
     */
    public static String wcaToSignNotation(String wca, String puzzleTypeName) {
        if (Character.isDigit(PuzzleType.valueOf(puzzleTypeName).scramblerSpec.charAt(0))) {
            String[] moves = wca.split(" ");
            for (int i = 0; i < moves.length; i++) {
                if (moves[i].contains("w")) {
                    moves[i] = moves[i].replace("w", "");
                    moves[i] = moves[i].toLowerCase();
                }
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < moves.length; i++) {
                builder.append(moves[i]);
                if (i != moves.length - 1) builder.append(" ");
            }
            return builder.toString();
        } else {
            return wca;
        }
    }

    /**
     * Converts a sequence of moves in SiGN notation to WCA notation
     *
     * @param sign the sequence of moves in SiGN notation
     * @return the converted sequence of moves in WCA notation
     */
    public static String signToWcaNotation(String sign, String puzzleTypeName) {
        if (Character.isDigit(PuzzleType.valueOf(puzzleTypeName).scramblerSpec.charAt(0))) {
            String[] moves = sign.split(" ");
            for (int i = 0; i < moves.length; i++) {
                if (!moves[i].equals(moves[i].toUpperCase())) {
                    char[] possibleMoves = "udfrlb".toCharArray();
                    for (char move : possibleMoves) {
                        moves[i] = moves[i].replace(String.valueOf(move), Character.toUpperCase(move) + "w");
                    }
                }
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < moves.length; i++) {
                builder.append(moves[i]);
                if (i != moves.length - 1) builder.append(" ");
            }
            return builder.toString();
        } else {
            return sign;
        }
    }

}