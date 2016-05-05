package com.tuyennc.weekview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private WeekView mWeekView;
    private Calendar mFirstDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView mYearTextView = (TextView) findViewById(R.id.text_year);
        Button mNextWeek = (Button) findViewById(R.id.button_previous);
        Button mPreviousWeek = (Button) findViewById(R.id.button_next);
        mWeekView = (WeekView) findViewById(R.id.week_view);

        mFirstDay = Calendar.getInstance();

        mNextWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirstDay.add(Calendar.DAY_OF_MONTH, 7);
                mWeekView.setFirstDay(mFirstDay);
            }
        });
        mPreviousWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirstDay.add(Calendar.DAY_OF_MONTH, -7);
                mWeekView.setFirstDay(mFirstDay);
            }
        });
        mYearTextView.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
    }
}
