package com.local.hardworkrec;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Insets;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    public static final int BG = 0xfff3f5f7;
    public static final int WHITE = Color.WHITE;
    public static final int INK = 0xff17243a;
    public static final int MUTED = 0xff647084;
    public static final int GREEN = 0xff235b57;
    public static final int SOFT = 0xffe8f0ed;
    public static final int BORDER = 0xffe2e7ee;
    public static final int LIME = 0xffd5f577;

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
        view.setIncludeFontPadding(false);
        view.setLineSpacing(dp(c, 3), 1f);
        view.setTypeface(Typeface.create(bold ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        return view;
    }

    public static TextView action(Context c, String label, boolean filled) {
        TextView view = text(c, label, 15, INK, true);
        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(c, 54));
        view.setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10));
        view.setBackground(touch(filled ? LIME : WHITE, 18, c));
        view.setClickable(true);
        view.setFocusable(true);
        view.setContentDescription(label);
        return view;
    }

    public static LinearLayout card(Context c) {
        LinearLayout view = column(c);
        view.setPadding(dp(c, 20), dp(c, 20), dp(c, 20), dp(c, 20));
        view.setBackground(stroke(WHITE, BORDER, 24, c));
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

    public static Drawable touch(int color,int radius,Context c) {
        return new RippleDrawable(ColorStateList.valueOf(0x180f2238),shape(color,radius,c),shape(WHITE,radius,c));
    }
    public static GradientDrawable dark(Context c,int radius) {
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{INK,0xff294c57});
        d.setCornerRadius(dp(c,radius));return d;
    }
    public static void icon(TextView view,String name,int color,int size) {
        Drawable d=new Symbol(name,color);int px=dp(view.getContext(),size);d.setBounds(0,0,px,px);
        view.setCompoundDrawables(d,null,null,null);view.setCompoundDrawablePadding(dp(view.getContext(),10));
    }
    public static TextView back(Context c,String label) {
        TextView v=text(c,label,14,INK,true);v.setMinHeight(dp(c,48));v.setGravity(Gravity.CENTER_VERTICAL);icon(v,"back",INK,20);return v;
    }
    public static LinearLayout tool(Context c,String symbol,String title,String subtitle) {
        LinearLayout v=card(c);v.setPadding(dp(c,16),dp(c,16),dp(c,16),dp(c,16));v.setBackground(touch(WHITE,22,c));
        TextView mark=text(c,"",18,GREEN,true);icon(mark,symbol,GREEN,24);v.addView(mark,matchWrap(c,14));
        v.addView(text(c,title,17,INK,true),matchWrap(c,4));v.addView(text(c,subtitle,12,MUTED,false));
        v.setFocusable(true);v.setContentDescription(title);return v;
    }
    public static void empty(Context c,LinearLayout parent,String title,String caption) {
        LinearLayout box=card(c);box.setGravity(Gravity.CENTER);box.setPadding(dp(c,20),dp(c,30),dp(c,20),dp(c,30));
        TextView mark=text(c,"",20,GREEN,true);Drawable icon=new Symbol("record",GREEN);int size=dp(c,30);icon.setBounds(0,0,size,size);mark.setCompoundDrawables(null,icon,null,null);mark.setGravity(Gravity.CENTER);box.addView(mark,matchWrap(c,16));
        TextView t=text(c,title,16,INK,true);t.setGravity(Gravity.CENTER);box.addView(t,matchWrap(c,8));
        TextView sub=text(c,caption,13,MUTED,false);sub.setGravity(Gravity.CENTER);box.addView(sub);parent.addView(box,matchWrap(c,12));
    }
    /** Original geometric icons; no external artwork or font asset. */
    public static final class Symbol extends Drawable {
        private final String name;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Symbol(String name,int color){this.name=name;p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
        @Override public void draw(Canvas c){c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/24f,getBounds().height()/24f);
            switch(name){
                case "home": {Path s=new Path();s.moveTo(3,11);s.lineTo(12,3);s.lineTo(21,11);s.moveTo(5,10);s.lineTo(5,21);s.lineTo(10,21);s.lineTo(10,15);s.lineTo(14,15);s.lineTo(14,21);s.lineTo(19,21);s.lineTo(19,10);c.drawPath(s,p);break;}
                case "calendar":c.drawRoundRect(3,5,21,21,3,3,p);c.drawLine(3,10,21,10,p);c.drawLine(8,3,8,7,p);c.drawLine(16,3,16,7,p);c.drawLine(8,14,10,14,p);c.drawLine(14,14,16,14,p);c.drawLine(8,18,10,18,p);break;
                case "backup":c.drawRoundRect(4,4,20,20,3,3,p);c.drawLine(12,7,12,16,p);c.drawLine(8,12,12,16,p);c.drawLine(16,12,12,16,p);break;
                case "timer":c.drawCircle(12,14,8,p);c.drawLine(9,2,15,2,p);c.drawLine(12,2,12,5,p);c.drawLine(12,14,12,9,p);c.drawLine(18,6,20,4,p);break;
                case "count":c.drawLine(5,5,3,19,p);c.drawLine(13,5,11,19,p);c.drawLine(19,5,17,19,p);c.drawLine(2,9,22,9,p);c.drawLine(1,15,21,15,p);break;
                case "back":c.drawLine(20,12,4,12,p);c.drawLine(10,5,3,12,p);c.drawLine(3,12,10,19,p);break;
                case "info":c.drawCircle(12,12,9,p);c.drawLine(12,11,12,17,p);c.drawPoint(12,7,p);break;
                default:c.drawRoundRect(4,3,20,21,3,3,p);c.drawLine(8,8,16,8,p);c.drawLine(8,12,16,12,p);c.drawLine(8,16,13,16,p);break;
            }c.restore();}
        @Override public void setAlpha(int a){p.setAlpha(a);invalidateSelf();}
        @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
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
