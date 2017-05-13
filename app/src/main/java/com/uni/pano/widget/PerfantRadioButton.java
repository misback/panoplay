package com.uni.pano.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RadioButton;

import com.uni.pano.R;

public class PerfantRadioButton extends RadioButton {

    private String mTag;
    private Drawable buttonDrawable;
    private int buttonLeft, buttonWidth, verticalGravity, height, y;

    public PerfantRadioButton(Context context) {
        super(context);
    }

    public PerfantRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Object tag = getTag();
        if (tag instanceof String) {
            mTag = (String) tag;
        }
        if ("photo".equals(mTag)) {
            buttonDrawable = getResources().getDrawable(R.drawable.ic_mode_photo_selector);
        } else {
            buttonDrawable = getResources().getDrawable(R.drawable.ic_mode_video_selector);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (buttonDrawable != null) {
            buttonDrawable.setState(getDrawableState());
            verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
            height = buttonDrawable.getIntrinsicHeight();

            switch (verticalGravity) {
                case Gravity.BOTTOM:
                    y = getHeight() - height;
                    break;
                case Gravity.CENTER_VERTICAL:
                    y = (getHeight() - height) / 2;
                    break;
            }

            buttonWidth = buttonDrawable.getIntrinsicWidth();
            buttonLeft = (getWidth() - buttonWidth) / 2;
            buttonDrawable.setBounds(buttonLeft, y, buttonLeft + buttonWidth, y + height);
            buttonDrawable.draw(canvas);
        }
    }
}
