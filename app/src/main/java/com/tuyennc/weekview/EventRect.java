package com.tuyennc.weekview;

import android.graphics.Rect;

import java.util.Calendar;

/**
 * Created by Nguyen Cong Tuyen on 4/25/2016.
 */

public class EventRect {
    public int mLeft;
    public int mTop;
    public int mRight;
    public int mBottom;
    public int mDayOfWeekIndex;
    public int mTimeOfDayIndex;
    private boolean mIsAllDayEvent;
    private boolean mIsDurationEvent;

    public boolean isAllDayEvent() {
        return mIsAllDayEvent;
    }

    public void setAllDayEvent(boolean pAllDayEvent) {
        mIsAllDayEvent = pAllDayEvent;
        if (mIsAllDayEvent)
            mIsDurationEvent = false;
    }

    public boolean isDurationEvent() {
        return mIsDurationEvent;
    }

    public void setDurationEvent(boolean pDurationEvent) {
        mIsDurationEvent = pDurationEvent;
        if (mIsDurationEvent)
            mIsAllDayEvent = false;
    }

    public void setEventTimeIndex(int pDayOfWeekIndex, int pTimeOfDayIndex) {
        mDayOfWeekIndex = pDayOfWeekIndex;
        mTimeOfDayIndex = pTimeOfDayIndex;
        setDurationEvent(true);
    }

    public void copyCoordinateFromRect(Rect pRect) {
        mLeft = pRect.left;
        mTop = pRect.top;
        mRight = pRect.right;
        mBottom = pRect.bottom;
    }

    public boolean hitTest(float x, float y) {
        if (x >= mLeft && x <= mRight && y >= mTop && y <= mBottom) {
            return true;
        }
        return false;
    }

    public Calendar getEventTime() {
        Calendar out = TimeUtils.today();
        out.add(Calendar.DAY_OF_MONTH, mDayOfWeekIndex);
        out.add(Calendar.HOUR_OF_DAY, WeekView.START_TIME_OF_DAY + mTimeOfDayIndex / 2);
        out.add(Calendar.MINUTE, mTimeOfDayIndex % 2 == 0 ? 0 : 30);
        return out;
    }
}
