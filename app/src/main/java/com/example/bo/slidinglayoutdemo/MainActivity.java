package com.example.bo.slidinglayoutdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final Random mRnd = new Random();
    private final Handler mHandler = new Handler();
    private SlidingLayout mSlidingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSlidingLayout = (SlidingLayout) findViewById(R.id.sliding_text_layout);
        addTextView();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                addTextView();
                mHandler.postDelayed(this, 1000 * mRnd.nextInt(15));
            }
        });
    }

    private void addTextView() {
        TextView tv = new TextView(MainActivity.this);
        SpannableStringBuilder ssb = new SpannableStringBuilder("text" + mRnd.nextLong());
        ssb.setSpan(new ImageSpan(this, android.R.drawable.presence_audio_away), ssb.length() - 1, ssb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(10 + mRnd.nextInt(10));
        mSlidingLayout.addView(tv);
    }
}
