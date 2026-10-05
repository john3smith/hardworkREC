package com.local.hardworkrec;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class CountActivity extends Activity {
    private static final String LOG_TAG = "hardworkREC.Count";
    private int count;
    private TextView number;
    private Switch vibrationSwitch;
    private Switch soundSwitch;
    private ToneGenerator toneGenerator;
    private final DoubleTapReset resetGuard = new DoubleTapReset();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        count = state == null ? Math.max(0, getIntent().getIntExtra("count", 0)) : state.getInt("count", 0);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        LinearLayout root = Ui.column(this);
        root.setPadding(Ui.dp(this, 22), Ui.dp(this, 24), Ui.dp(this, 22), Ui.dp(this, 25));
        root.setBackgroundColor(Ui.BG);
        Ui.applySystemInsets(root, false);
        setContentView(root);
        TextView back = Ui.text(this, "←  메인 화면", 15, Ui.GREEN, true);
        back.setOnClickListener(v -> finishCount());
        root.addView(back, Ui.matchWrap(this, 28));
        root.addView(Ui.text(this, "수동 횟수 카운트", 30, Ui.INK, true), Ui.matchWrap(this, 6));
        root.addView(Ui.text(this, "가운데 버튼을 누를 때마다 1회씩 올라갑니다", 15, Ui.MUTED, false));

        LinearLayout middle = Ui.column(this);
        middle.setGravity(Gravity.CENTER);
        root.addView(middle, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout circle = Ui.column(this);
        circle.setGravity(Gravity.CENTER);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(Ui.GREEN);
        circle.setBackground(background);
        circle.setElevation(Ui.dp(this, 8));
        number = Ui.text(this, Integer.toString(count), 76, Ui.WHITE, true);
        number.setGravity(Gravity.CENTER);
        circle.addView(number);
        TextView hint = Ui.text(this, "탭하여 +1", 16, Ui.SOFT, true);
        hint.setGravity(Gravity.CENTER);
        circle.addView(hint);
        circle.setContentDescription("카운트 증가, 현재 " + count + "회");
        circle.setOnClickListener(v -> {
            if (count >= 999999999) return;
            count++;
            number.setText(String.format(Locale.US, "%d", count));
            circle.setContentDescription("카운트 증가, 현재 " + count + "회");
            playCountFeedback();
        });
        middle.addView(circle, new LinearLayout.LayoutParams(Ui.dp(this, 270), Ui.dp(this, 270)));

        LinearLayout feedback = Ui.row(this);
        vibrationSwitch = new Switch(this);
        vibrationSwitch.setText("진동");
        vibrationSwitch.setTextColor(Ui.INK);
        vibrationSwitch.setChecked(getPreferences(MODE_PRIVATE).getBoolean("vibration", true));
        vibrationSwitch.setOnCheckedChangeListener((button, enabled) ->
                getPreferences(MODE_PRIVATE).edit().putBoolean("vibration", enabled).apply());
        feedback.addView(vibrationSwitch, new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1));
        soundSwitch = new Switch(this);
        soundSwitch.setText("소리");
        soundSwitch.setTextColor(Ui.INK);
        soundSwitch.setChecked(getPreferences(MODE_PRIVATE).getBoolean("sound", false));
        soundSwitch.setOnCheckedChangeListener((button, enabled) ->
                getPreferences(MODE_PRIVATE).edit().putBoolean("sound", enabled).apply());
        feedback.addView(soundSwitch, new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1));
        root.addView(feedback, Ui.matchWrap(this, 10));

        TextView undo = Ui.action(this, "−  1회 취소", false);
        undo.setOnClickListener(v -> { if (count > 0) count--; number.setText(String.format(Locale.US, "%d", count)); circle.setContentDescription("카운트 증가, 현재 " + count + "회"); });
        root.addView(undo, Ui.matchWrap(this, 10));
        TextView reset = Ui.action(this, "횟수 리셋 · 빠르게 두 번 누르기", false);
        reset.setContentDescription("횟수 리셋, 빠르게 두 번 누르기");
        reset.setOnClickListener(v -> {
            if (resetGuard.tap(SystemClock.elapsedRealtime())) {
                count = 0;
                number.setText("0");
                circle.setContentDescription("카운트 증가, 현재 0회");
                Toast.makeText(this, "횟수를 0으로 리셋했습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "리셋하려면 빠르게 한 번 더 누르세요.", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(reset, Ui.matchWrap(this, 10));
        TextView done = Ui.action(this, "메인 화면으로", true);
        done.setOnClickListener(v -> finishCount());
        root.addView(done);
    }

    private void finishCount() {
        finish();
    }

    private void playCountFeedback() {
        if (vibrationSwitch.isChecked()) {
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= 31) {
                VibratorManager manager = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
                vibrator = manager == null ? null : manager.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                VibrationEffect effect = VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE);
                if (Build.VERSION.SDK_INT >= 33) {
                    vibrator.vibrate(effect, new VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_TOUCH).build());
                } else {
                    vibrator.vibrate(effect, new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build());
                }
                Log.i(LOG_TAG, "Count tap vibration requested");
            } else {
                Log.w(LOG_TAG, "Count tap vibration unavailable on this device");
            }
        }
        if (soundSwitch.isChecked()) {
            try {
                if (toneGenerator == null) toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
                boolean started = toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 200);
                Log.i(LOG_TAG, "Count tap sound started=" + started);
            } catch (RuntimeException error) {
                Log.e(LOG_TAG, "Count tap sound failed", error);
            }
        }
    }

    @Override protected void onDestroy() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        super.onDestroy();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("count", count);
        super.onSaveInstanceState(out);
    }
}
