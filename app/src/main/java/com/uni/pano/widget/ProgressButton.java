package com.uni.pano.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.uni.pano.R;

/**
 * Created by ZachLi on 2016/6/29.
 */
public class ProgressButton extends ProgressBar {

    private String mText = "";
    private float mTextSize = 12;
    private int mTextColor = Color.BLACK;

    private TextPaint mPaint;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        initPaint();
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        initPaint();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs);
        initPaint();
    }


    private void initAttr(Context context, AttributeSet attrs) {
        if (attrs != null) {

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton);
            mText = a.getString(R.styleable.ProgressButton_text);
            mTextSize = a.getDimension(R.styleable.ProgressButton_textSize, 12);
            ColorStateList colorStateList = a.getColorStateList(R.styleable.ProgressButton_textColor);
            mTextColor = colorStateList == null ? Color.BLACK : colorStateList.getDefaultColor();
            a.recycle();
        }
    }


    private void initPaint() {
        // 创建画笔并开启抗锯齿
        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), rect);

        float x = getWidth() / 2 - rect.centerX();
        float y = getHeight() / 2 - rect.centerY();

        canvas.drawText(mText, x, y, mPaint);

    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

}
