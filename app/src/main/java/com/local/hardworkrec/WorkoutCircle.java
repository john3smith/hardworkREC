package com.local.hardworkrec;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

/** Large workout control that stays circular within the available screen width. */
final class WorkoutCircle extends LinearLayout {
    WorkoutCircle(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        int padding = Ui.dp(context, 24);
        setPadding(padding, padding, padding, padding);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int diameter = Ui.dp(getContext(), 360);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            diameter = Math.min(diameter, MeasureSpec.getSize(widthMeasureSpec));
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            diameter = Math.min(diameter, MeasureSpec.getSize(heightMeasureSpec));
        }
        int square = MeasureSpec.makeMeasureSpec(diameter, MeasureSpec.EXACTLY);
        super.onMeasure(square, square);
    }
}
