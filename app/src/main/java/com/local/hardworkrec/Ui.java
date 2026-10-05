package com.local.hardworkrec;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Insets;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    public static final int BG = Color.rgb(247, 249, 248);
    public static final int WHITE = Color.WHITE;
    public static final int INK = Color.rgb(21, 38, 34);
    public static final int MUTED = Color.rgb(98, 113, 108);
    public static final int GREEN = Color.rgb(24, 111, 98);
    public static final int SOFT = Color.rgb(225, 245, 238);
    public static final int BORDER = Color.rgb(224, 233, 229);

    private Ui() {}

    public static int dp(Context c, float value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable shape(int color, int radiusDp, Context c) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(c, radiusDp));
        return drawable;
    }

    public static GradientDrawable stroke(int color, int border, int radiusDp, Context c) {
        GradientDrawable drawable = shape(color, radiusDp, c);
        drawable.setStroke(dp(c, 1), border);
        return drawable;
    }

    public static LinearLayout column(Context c) {
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public static LinearLayout row(Context c) {
        LinearLayout layout = new LinearLayout(c);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    public static TextView text(Context c, String value, int sp, int color, boolean bold) {
        TextView view = new TextView(c);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setFontFeatureSettings("kern");
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    public static TextView action(Context c, String label, boolean filled) {
        TextView view = text(c, label, 16, filled ? WHITE : GREEN, true);
        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(c, 54));
        view.setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10));
        view.setBackground(filled ? shape(GREEN, 16, c) : stroke(WHITE, BORDER, 16, c));
        view.setClickable(true);
        view.setFocusable(true);
        view.setContentDescription(label);
        return view;
    }

    public static LinearLayout card(Context c) {
        LinearLayout view = column(c);
        view.setPadding(dp(c, 18), dp(c, 18), dp(c, 18), dp(c, 18));
        view.setBackground(shape(WHITE, 20, c));
        view.setElevation(dp(c, 1));
        return view;
    }

    public static LinearLayout.LayoutParams matchWrap(Context c, int bottomDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(c, bottomDp);
        return params;
    }

    public static View gap(Context c, int heightDp) {
        View view = new View(c);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(c, heightDp)));
        return view;
    }

    /** Keep interactive content outside status/navigation bars in edge-to-edge mode. */
    public static void applySystemInsets(View root, boolean includeKeyboard) {
        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();
        root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            int left, top, right, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets bars = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                left = bars.left; top = bars.top; right = bars.right; bottom = bars.bottom;
                if (includeKeyboard) bottom = Math.max(bottom, windowInsets.getInsets(WindowInsets.Type.ime()).bottom);
            } else {
                left = windowInsets.getSystemWindowInsetLeft();
                top = windowInsets.getSystemWindowInsetTop();
                right = windowInsets.getSystemWindowInsetRight();
                bottom = windowInsets.getSystemWindowInsetBottom();
            }
            view.setPadding(baseLeft + left, baseTop + top, baseRight + right, baseBottom + bottom);
            return windowInsets;
        });
    }
}
