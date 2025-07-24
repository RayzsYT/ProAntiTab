package de.rayzs.pat.utils;

public class TimeConverter {

    private static final int SECOND = 1000, MINUTE = SECOND * 60, HOUR = MINUTE * 60, DAY = HOUR * 24, WEEK = DAY * 7;

    public static String calcAndGetTime(long time) {
        long upTime = (System.currentTimeMillis() - time);
        int weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0, ms = (int) upTime;

        while (ms >= SECOND) {
            if (ms >= WEEK) {
                weeks++;
                ms -= WEEK;
            } else if (ms >= DAY) {
                days++;
                ms -= DAY;
            } else if (ms >= HOUR) {
                hours++;
                ms -= HOUR;
            } else if (ms >= MINUTE) {
                minutes++;
                ms -= MINUTE;
            } else {
                seconds++;

                if (seconds >= 60) {
                    seconds = 0;
                    minutes++;
                }

                ms = ms - SECOND;
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
}
