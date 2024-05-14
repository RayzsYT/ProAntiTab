package de.rayzs.pat.utils;

public class TimeConverter {

    private static final int MINUTES = 60, HOURS = MINUTES * 60, DAYS = HOURS * 24, WEEKS = DAYS * 7;

    public static String calcAndGetTime(long time) {
        long upTime = (System.currentTimeMillis() - time) / 1000;
        int weeks = 0, days = 0, hours = 0, minutes = 0, seconds = (int) upTime;

        while (seconds >= 60) {
            if (seconds >= WEEKS) {
                weeks++;
                seconds = seconds - WEEKS;
            } else if (seconds >= DAYS) {
                days++;
                seconds = seconds - DAYS;
            } else if (seconds >= HOURS) {
                hours++;
                seconds = seconds - HOURS;
            }
            minutes++;
            seconds = seconds - MINUTES;
        }

        StringBuilder uptimeText = new StringBuilder();
        if(weeks > 0) uptimeText.append(weeks + "w, ");
        else if(days > 0) uptimeText.append(days + "d, ");
        else if(hours > 0) uptimeText.append(hours + "h, ");
        else if(minutes > 0) uptimeText.append(minutes + "m, ");
        uptimeText.append(seconds + "s");
        return uptimeText.toString();
    }
}
