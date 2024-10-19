package de.rayzs.pat.utils;

public class TimeConverter {

    public static String calcAndGetTime(long time) {
        long upTime = (System.currentTimeMillis() - time);
        int weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0, ms = (int) upTime;

        while (ms >= SECONDS) {
            if (ms >= WEEKS) {
                weeks++;
                ms = ms - WEEKS;
            } else if (ms >= DAYS) {
                days++;
                ms = ms - DAYS;
            } else if (ms >= HOURS) {
                hours++;
                ms = ms - HOURS;
            } else if (ms >= MINUTES) {
                minutes++;
                ms = ms - MINUTES;
            } else {
                seconds++;
                ms = ms - SECONDS;
            }
        }

        StringBuilder uptimeText = new StringBuilder();
        if (weeks > 0) uptimeText.append(weeks + "w, ");
        else if (days > 0) uptimeText.append(days + "d, ");
        else if (hours > 0) uptimeText.append(hours + "h, ");
        else if (minutes > 0) uptimeText.append(minutes + "m, ");
        else if (seconds > 0) uptimeText.append(seconds + "s");
        else uptimeText.append(ms + "ms");
        return uptimeText.toString();
    }

    private static final int SECONDS = 1000, MINUTES = SECONDS * 60, HOURS = MINUTES * 60, DAYS = HOURS * 24, WEEKS = DAYS * 7;


}
