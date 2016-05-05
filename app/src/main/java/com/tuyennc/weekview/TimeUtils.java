package com.tuyennc.weekview;

import java.util.Calendar;

/**
 * Created by Nguyen Cong Tuyen on 4/20/2016.
 */
public class TimeUtils {
    /**
     * Returns a calendar instance at the start of this day
     *
     * @return the calendar instance
     */
    private static String[] sDayNames = new String[]{"(日)", "(月)", "(火)", "(水)", "(木)", "(金)", "(土)"};

    public static Calendar today() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    public static boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        if(dayOne == null || dayTwo == null)
            return false;
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

    public static int dateDiff(Calendar dayOne, Calendar dayTwo){
        if(dayOne == null || dayTwo == null)
            return 0;
        return (int) ((dayTwo.getTimeInMillis() - dayOne.getTimeInMillis()) /(1000 * 60 * 60 * 24));
    }

    public static String getDayName(int day) {
        return sDayNames[day];
    }
}
