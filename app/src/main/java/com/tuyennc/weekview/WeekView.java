package com.tuyennc.weekview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nguyen Cong Tuyen on 4/15/2016.
 */
public class WeekView extends View {

    public static final int START_TIME_OF_DAY = 8;
    public static final int END_TIME_OF_DAY = 23;
    private int mInvisibleRow;
    private int mVisibleRowPerPage;
    private int mMaxScrollY;

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private OverScroller mScroller;
    private GestureDetectorCompat mGestureDetector;
    private Direction mCurrentScrollDirection = Direction.NONE;
    private Direction mCurrentFlingDirection = Direction.NONE;
    private boolean mHorizontalFlingEnabled = true;
    private boolean mVerticalFlingEnabled = true;
    private float mXScrollingSpeed = 1f;
    private PointF mCurrentOrigin = new PointF(0f, 0f);

    private final int TEXT_PADDING = 36;
    private TextPaint mTextPaint;
    private Paint mBackgroundPaint;

    private float mTextHeight;
    private float mTextWidth;

    private float mHeaderTextSize = 16;
    private int mRowDividerHeight = 6;
    private int mVerticalTextPadding = 6;
    private int mRowSize;

    private Rect mTopLeftRect;
    private Rect mTextBoundRect;

    private Rect mMonthHeaderRect;
    private Rect mDayRowHeaderRect;

    private int mInactiveColor = Color.parseColor("#e3e3e3");
    private int mMonthBgColor = Color.parseColor("#b8b8b8");
    private int mSundayBgColor = Color.parseColor("#f9d6cf");
    private int mSaturdayBgColor = Color.parseColor("#d5e1f7");
    private Calendar mFirstVisibleDay;
    private int mStartYFromHeader = 0;

    private int mStartTime = START_TIME_OF_DAY;
    private int mEndTime = END_TIME_OF_DAY;
    private int mEndY;

    private List<WeekEvent> mWeekEvents;
    private boolean[] mAllDayEventIndex;
    private boolean[][] mEventIndex;
    private String[] mAllDayEventString;

    private String[] mDayNumberString;
    private String[] mDayNameString;
    private String[] mTimesString;
    private String mMonthString;

    private EventRect mSelectedEventRect;
    private OnTimeSlotClickedListener mOnTimeSlotClickedListener;
    private List<EventRect> mEventRects;

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            if (!mScroller.isFinished()) { // is flinging
                mScroller.forceFinished(true); // to stop flinging on touch
            }
            mCurrentScrollDirection = Direction.NONE;
            mCurrentFlingDirection = Direction.NONE;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (distanceX > 0) {
                    mCurrentScrollDirection = Direction.LEFT;
                }
                else {
                    mCurrentScrollDirection = Direction.RIGHT;
                }
            }
            else {
                mCurrentScrollDirection = Direction.VERTICAL;
            }

            // Calculate the new origin after scroll.
            switch (mCurrentScrollDirection) {
                case LEFT:
                case RIGHT:
                    mCurrentOrigin.x -= distanceX * mXScrollingSpeed;
                    ViewCompat.postInvalidateOnAnimation(WeekView.this);
                    break;
                case VERTICAL:
                    mMaxScrollY = -(((mEndTime - mStartTime + 1) * 2 - mVisibleRowPerPage) * (mRowSize + mRowDividerHeight));
                    mCurrentOrigin.y -= distanceY;
                    if (mCurrentOrigin.y < mMaxScrollY) {
                        mCurrentOrigin.y = mMaxScrollY;
                    }
                    if (mCurrentOrigin.y > 0) {
                        mCurrentOrigin.y = 0;
                    }
                    ViewCompat.postInvalidateOnAnimation(WeekView.this);
                    break;
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if ((mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled) ||
                    (mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled) ||
                    (mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled)) {
                return true;
            }
            mScroller.forceFinished(true);

            mCurrentFlingDirection = mCurrentScrollDirection;
            switch (mCurrentFlingDirection) {
                case LEFT:
                case RIGHT:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, (int) (velocityX * mXScrollingSpeed), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                    break;
                case VERTICAL:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, 0, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, mMaxScrollY, 0);
                    break;
            }

            ViewCompat.postInvalidateOnAnimation(WeekView.this);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            AppLogger.logInfo("X:" + e.getX() + "Y:" + e.getY());
            for (EventRect rect :
                    mEventRects) {
                if (rect.hitTest(e.getX(), e.getY())) {
                    mSelectedEventRect = rect;
                    if (rect.isDurationEvent())
                        AppLogger.logInfo("busy time: " + rect.getEventTime());
                    else {
                        AppLogger.logInfo("available time: " + rect.getEventTime());
                    }
                    if (mOnTimeSlotClickedListener != null) {
                        mOnTimeSlotClickedListener.onTimeSlotSelected(rect);
                    }
                    invalidate();
                    break;
                }
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    };

    public WeekView(Context context) {
        super(context);
        init(context);
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.WeekView);
        try {
            mHeaderTextSize = typedArray.getDimensionPixelSize(R.styleable.WeekView_header_text_size, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mHeaderTextSize, context.getResources().getDisplayMetrics()));
        } catch (Exception ex) {
        } finally {
            typedArray.recycle();
        }

        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mScroller = new OverScroller(context, new FastOutLinearInInterpolator());
        // int paint
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mMonthBgColor);
        mBackgroundPaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(mHeaderTextSize);

        //measure text w and h
        mTextWidth = mTextPaint.measureText("12月");
        mTextBoundRect = new Rect();
        mTextPaint.getTextBounds("12月", 0, "12月".length(), mTextBoundRect);
        mTextHeight = mTextBoundRect.height();

        mTopLeftRect = new Rect();
        mMonthHeaderRect = new Rect();

        mDayRowHeaderRect = new Rect();

        //init event
        mAllDayEventIndex = new boolean[7];
        mAllDayEventString = new String[7];
        int timeRow = ((mEndTime - mStartTime) * 2 + 1);
        mEventIndex = new boolean[7][timeRow];
        mWeekEvents = new ArrayList<>();

        mDayNumberString = new String[7];
        mDayNameString = new String[7];
        mDayNameString = new String[7];
        mTimesString = new String[timeRow];

        setFirstDay(TimeUtils.today());

        mEventRects = new ArrayList<>();


        List<WeekEvent> mTmpEvent = new ArrayList<>();
        WeekEvent time = new WeekEvent();
        time.isAllDay = true;
        time.mEventDateTime = TimeUtils.today();
        mTmpEvent.add(time);

        time = new WeekEvent();
        time.isAllDay = false;
        time.mEventDateTime = TimeUtils.today();
        time.mEventDateTime.add(Calendar.DAY_OF_MONTH, 1);
        time.mEventDateTime.add(Calendar.HOUR_OF_DAY, 8);
        time.mEventDateTime.add(Calendar.MINUTE, 0);
        mTmpEvent.add(time);

        time = new WeekEvent();
        time.isAllDay = false;
        time.mEventDateTime = TimeUtils.today();
        time.mEventDateTime.add(Calendar.DAY_OF_MONTH, 6);
        time.mEventDateTime.add(Calendar.HOUR_OF_DAY, 23);
        time.mEventDateTime.add(Calendar.MINUTE, 0);
        mTmpEvent.add(time);

        setWeekEvents(mTmpEvent);
    }

    public void setFirstDay(Calendar pFirstDay) {
        mFirstVisibleDay = (Calendar) pFirstDay.clone();
        mMonthString = (mFirstVisibleDay.get(Calendar.MONTH) + 1) + "月";

        for (int i = 0; i < 7; i++) {
            mDayNumberString[i] = String.valueOf(pFirstDay.get(Calendar.DAY_OF_MONTH) + 1);
            mDayNameString[i] = TimeUtils.getDayName(pFirstDay.get(Calendar.DAY_OF_WEEK) - 1);
            pFirstDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (int i = 0; i < (mEndTime - mStartTime) * 2 + 1; i++) {
            mTimesString[i] = String.format(Locale.ENGLISH, "%02d:%02d", (i / 2 + mStartTime), (i + mStartTime) % 2 == 0 ? 0 : 30);
        }
        invalidate();
    }

    public void setWeekEvents(List<WeekEvent> pWeekEvents) {
        resetBookedTimeSlot();
        mWeekEvents.addAll(pWeekEvents);
        int dayIndex = -1;
        int timeIndex = -1;
        for (int i = 0; i < mWeekEvents.size(); i++) {
            WeekEvent weekEvent = mWeekEvents.get(i);
            if (weekEvent != null && weekEvent.mEventDateTime != null) {
                Calendar firstDay = (Calendar) mFirstVisibleDay.clone();
                for (int j = 0; j < 7; j++) {
                    if (weekEvent.isAllDay) {
                        if (TimeUtils.isSameDay(weekEvent.mEventDateTime, firstDay)) {
                            mAllDayEventIndex[j] = true;
                            mAllDayEventString[i] = "メッセージを検索";
                        }
                    }
                    else {
                        dayIndex = TimeUtils.isSameDay(weekEvent.mEventDateTime, firstDay) ? j : -1;
                        timeIndex = (weekEvent.mEventDateTime.get(Calendar.HOUR_OF_DAY) - mStartTime) * 2 + (weekEvent.mEventDateTime.get(Calendar.MINUTE) >= 30 ? 1 : 0);
                        if (dayIndex > -1 && timeIndex > -1) {
                            mEventIndex[dayIndex][timeIndex] = true;
                        }
                    }
                    firstDay.add(Calendar.DATE, 1);
                }
            }
        }
        invalidate();
    }

    private void resetBookedTimeSlot() {
        mWeekEvents.clear();
        mAllDayEventIndex = new boolean[7];
        mAllDayEventString = new String[7];
        mEventIndex = new boolean[7][(mEndTime - mStartTime) * 2 + 1];
    }

    private void setOnTimeSlotClickedListener(OnTimeSlotClickedListener pOnTimeSlotClickedListener) {
        mOnTimeSlotClickedListener = pOnTimeSlotClickedListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int topLeftRectW = ((w - 8 * mRowDividerHeight) / 17 * 2) * 3 / 2;
        mRowSize = topLeftRectW / 3 * 2;

        mTopLeftRect.left = mRowDividerHeight;
        mTopLeftRect.top = mRowDividerHeight;
        mTopLeftRect.right = topLeftRectW;

        mMonthHeaderRect.left = mTopLeftRect.right + mRowDividerHeight;
        mMonthHeaderRect.top = 0;
        mMonthHeaderRect.right = 0;
        mMonthHeaderRect.bottom = (int) (mTextHeight + TEXT_PADDING);

        mTopLeftRect.bottom = (int) (mTextHeight * 2 + TEXT_PADDING + mMonthHeaderRect.bottom + mRowDividerHeight + mVerticalTextPadding);
        mStartYFromHeader = mTopLeftRect.bottom + mRowDividerHeight;
        mVisibleRowPerPage = (getHeight() - mTopLeftRect.bottom) / (mRowSize + mRowDividerHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
        canvas.save();
        drawTimes(canvas);
        canvas.restore();
        drawHeader(canvas);
    }

    private void drawHeader(Canvas canvas) {
        //draw top left header
        mBackgroundPaint.setColor(mMonthBgColor);
        mBackgroundPaint.setStrokeWidth(mRowDividerHeight / 2);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(mTopLeftRect, mBackgroundPaint);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mTextPaint.setColor(Color.WHITE);
        float topLeftTextY = TEXT_PADDING / 2 - mTextBoundRect.top;

        Calendar mTempDay = (Calendar) mFirstVisibleDay.clone();
        //draw month header row
        mMonthHeaderRect.right = canvas.getWidth();
        canvas.drawRect(mMonthHeaderRect, mBackgroundPaint);
        canvas.drawText(mMonthString, (mMonthHeaderRect.right - mMonthHeaderRect.left - mTextWidth) / 2 + mMonthHeaderRect.left, topLeftTextY, mTextPaint);

        mDayRowHeaderRect.top = (mMonthHeaderRect.bottom + mRowDividerHeight);
        mDayRowHeaderRect.bottom = mTopLeftRect.bottom;
        mBackgroundPaint.setColor(mInactiveColor);

        for (int i = 0; i < 7; i++) {
            //draw day rect
            mDayRowHeaderRect.left = (mTopLeftRect.right + mRowDividerHeight + (mRowSize + mRowDividerHeight) * i);
            mDayRowHeaderRect.right = (mDayRowHeaderRect.left + (mRowSize));
            //draw day text
            //set bg for sunday or saturday
            if (mTempDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                mTextPaint.setColor(Color.RED);
                mBackgroundPaint.setColor(mSundayBgColor);
            }
            else if (mTempDay.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                mTextPaint.setColor(Color.BLUE);
                mBackgroundPaint.setColor(mSaturdayBgColor);
            }
            else {
                mTextPaint.setColor(mMonthBgColor);
                mBackgroundPaint.setColor(mInactiveColor);
            }
            canvas.drawRect(mDayRowHeaderRect, mBackgroundPaint);
            //draw date and day of week
            mTextWidth = mTextPaint.measureText(mDayNumberString[i]);
            canvas.drawText(mDayNumberString[i], (mDayRowHeaderRect.right - mDayRowHeaderRect.left - mTextWidth) / 2 + mDayRowHeaderRect.left, mDayRowHeaderRect.top + topLeftTextY, mTextPaint);
            mTextWidth = mTextPaint.measureText(mDayNameString[i]);
            canvas.drawText(mDayNameString[i], (mDayRowHeaderRect.right - mDayRowHeaderRect.left - mTextWidth) / 2 + mDayRowHeaderRect.left, mDayRowHeaderRect.top + topLeftTextY + mVerticalTextPadding + mTextHeight, mTextPaint);

            mBackgroundPaint.setColor(mMonthBgColor);
            if (mAllDayEventIndex[i]) {
                canvas.drawRect(mDayRowHeaderRect.left, mStartYFromHeader, mDayRowHeaderRect.left + mRowSize, mEndY, mBackgroundPaint);
                if (mAllDayEventString[i] != null) {
                    for (int k = 0; k < mAllDayEventString[i].length(); k++) {
                        mTextPaint.setColor(Color.BLACK);
                        mTextWidth = mTextPaint.measureText(String.valueOf(mAllDayEventString[i].charAt(k)));
                        canvas.drawText(String.valueOf(mAllDayEventString[i].charAt(k)), (mDayRowHeaderRect.right - mDayRowHeaderRect.left - mTextWidth) / 2 + mDayRowHeaderRect.left, (getHeight() - mStartYFromHeader - mTextHeight) / 2 - mTextBoundRect.top + k * (mTextHeight + mVerticalTextPadding), mTextPaint);
                    }
                }
            }
            //got o next day
            mTempDay.add(Calendar.DATE, 1);
        }
    }

    private void drawTimes(Canvas canvas) {
        mEventRects.clear();
        canvas.clipRect(0, mStartYFromHeader, canvas.getWidth(), canvas.getHeight());
        mTextWidth = mTextPaint.measureText(mTimesString[0]);
        mInvisibleRow = (int) (Math.abs(mCurrentOrigin.y) / (mRowSize + mRowDividerHeight));
        //set paint for drawing
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mRowDividerHeight / 2);
        mTextPaint.setColor(mMonthBgColor);

        for (int i = 0; i <= (mEndTime - mStartTime) * 2; i++) {
            //calculate top and bottom
            mDayRowHeaderRect.top = (mStartYFromHeader + (mRowSize + mRowDividerHeight) * i) + mRowDividerHeight * i;
            mDayRowHeaderRect.bottom = (mDayRowHeaderRect.top + (mRowSize));
            if (mInvisibleRow > 0) {
                mDayRowHeaderRect.top = (mStartYFromHeader + (mRowSize + mRowDividerHeight) * (i - mInvisibleRow)) + mRowDividerHeight * (i - mInvisibleRow) + (int) (mCurrentOrigin.y % (mRowSize + mRowDividerHeight));
                mDayRowHeaderRect.bottom = (mDayRowHeaderRect.top + (mRowSize));
            }
            else {
                mDayRowHeaderRect.top = (int) (mDayRowHeaderRect.top + mCurrentOrigin.y - mInvisibleRow * (mRowSize));
                mDayRowHeaderRect.bottom = (mDayRowHeaderRect.top + (mRowSize));
            }
            //draw time text
            mBackgroundPaint.setColor(mMonthBgColor);
            canvas.drawText(mTimesString[i], (mTopLeftRect.right - mTopLeftRect.left - mTextWidth) / 2 + mTopLeftRect.left, mDayRowHeaderRect.top + (mDayRowHeaderRect.height() - mTextHeight) / 2 - mTextBoundRect.top, mTextPaint);
            //draw time row
            for (int j = 0; j < 7; j++) {
                EventRect tmpEventRect = new EventRect();
                if (mAllDayEventIndex[j]) continue;

                mBackgroundPaint.setColor(mMonthBgColor);
                mDayRowHeaderRect.left = (mTopLeftRect.right + mRowDividerHeight + (mRowSize + mRowDividerHeight) * j);
                mDayRowHeaderRect.right = (mDayRowHeaderRect.left + (mRowSize));
                tmpEventRect.copyCoordinateFromRect(mDayRowHeaderRect);
                tmpEventRect.setAllDayEvent(false);
                tmpEventRect.setEventTimeIndex(j, i);
                //draw selected time slot background
                if (mSelectedEventRect != null) {
                    if (mSelectedEventRect.mDayOfWeekIndex == j && mSelectedEventRect.mTimeOfDayIndex == i) {
                        mBackgroundPaint.setStyle(Paint.Style.FILL);
                        mBackgroundPaint.setColor(mInactiveColor);
                        canvas.drawRect(mDayRowHeaderRect, mBackgroundPaint);
                    }
                }
                //draw time slot
                mBackgroundPaint.setStyle(Paint.Style.STROKE);
                mBackgroundPaint.setColor(mMonthBgColor);
                if (mEventIndex[j][i]) {
                    tmpEventRect.setDurationEvent(true);
                    canvas.drawLine(mDayRowHeaderRect.centerX() - mDayRowHeaderRect.width() / 4, mDayRowHeaderRect.centerY() - mDayRowHeaderRect.height() / 4, mDayRowHeaderRect.centerX() + mDayRowHeaderRect.width() / 4, mDayRowHeaderRect.centerY() + mDayRowHeaderRect.height() / 4, mBackgroundPaint);
                    canvas.drawLine(mDayRowHeaderRect.centerX() - mDayRowHeaderRect.width() / 4, mDayRowHeaderRect.centerY() + mDayRowHeaderRect.height() / 4, mDayRowHeaderRect.centerX() + mDayRowHeaderRect.width() / 4, mDayRowHeaderRect.centerY() - mDayRowHeaderRect.height() / 4, mBackgroundPaint);
                }
                else {
                    tmpEventRect.setDurationEvent(false);
                    canvas.drawRect(mDayRowHeaderRect, mBackgroundPaint);
                    mBackgroundPaint.setColor(Color.RED);
                    canvas.drawCircle(mDayRowHeaderRect.centerX(), mDayRowHeaderRect.centerY(), mDayRowHeaderRect.width() / 4, mBackgroundPaint);
                }
                mEventRects.add(tmpEventRect);
            }
            //draw row divider line
            mBackgroundPaint.setColor(mMonthBgColor);
            canvas.drawLine(0, mDayRowHeaderRect.bottom + mRowDividerHeight, canvas.getWidth(), mDayRowHeaderRect.bottom + mRowDividerHeight, mBackgroundPaint);
            mEndY = mDayRowHeaderRect.bottom + mRowDividerHeight;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller.getCurrY();
                mCurrentOrigin.x = mScroller.getCurrX();
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    public interface OnTimeSlotClickedListener {
        void onTimeSlotSelected(EventRect pEventRect);
    }
}
