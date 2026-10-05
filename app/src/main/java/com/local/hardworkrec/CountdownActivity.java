package com.local.hardworkrec;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.VibrationAttributes;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public final class CountdownActivity extends Activity {
    private static final String LOG_TAG = "hardworkREC.Timer";
    private static final int DEFAULT_SECONDS = 60;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            updateDisplay();
            if (running) handler.postDelayed(this, 100);
        }
    };
    private EditText timeInput;
    private TextView display;
    private TextView circleHint;
    private LinearLayout circle;
    private Switch vibrationSwitch;
    private Switch soundSwitch;
    private int durationSeconds = DEFAULT_SECONDS;
    private long remainingMs = DEFAULT_SECONDS * 1000L;
    private long deadlineMs;
    private boolean running;
    private boolean hasStarted;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            durationSeconds = state.getInt("duration", DEFAULT_SECONDS);
            remainingMs = state.getLong("remaining", durationSeconds * 1000L);
            running = state.getBoolean("running", false) && remainingMs > 0;
            hasStarted = state.getBoolean("hasStarted", false);
            if (running) deadlineMs = SystemClock.elapsedRealtime() + remainingMs;
        }
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        LinearLayout root = Ui.column(this);
        root.setPadding(Ui.dp(this, 22), Ui.dp(this, 24), Ui.dp(this, 22), Ui.dp(this, 25));
        root.setBackgroundColor(Ui.BG);
        Ui.applySystemInsets(root, true);
        setContentView(root);

        TextView back = Ui.text(this, "←  메인 화면", 15, Ui.GREEN, true);
        back.setOnClickListener(v -> finish());
        root.addView(back, Ui.matchWrap(this, 28));
        root.addView(Ui.text(this, "운동 카운트다운", 30, Ui.INK, true), Ui.matchWrap(this, 8));
        root.addView(Ui.text(this, "기본 60초 · 원형 버튼을 눌러 시작하거나 일시정지하세요", 15, Ui.MUTED, false), Ui.matchWrap(this, 24));

        LinearLayout settings = Ui.card(this);
        settings.addView(Ui.text(this, "설정 시간 (초)", 15, Ui.INK, true), Ui.matchWrap(this, 8));
        timeInput = new EditText(this);
        timeInput.setSingleLine(true);
        timeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        timeInput.setTextSize(20);
        timeInput.setTextColor(Ui.INK);
        timeInput.setHint("예: 90");
        timeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        timeInput.setBackground(Ui.stroke(Ui.WHITE, Ui.BORDER, 13, this));
        timeInput.setPadding(Ui.dp(this, 15), 0, Ui.dp(this, 15), 0);
        timeInput.setText(state == null ? Integer.toString(durationSeconds) : state.getString("timeInput", Integer.toString(durationSeconds)));
        settings.addView(timeInput, new LinearLayout.LayoutParams(-1, Ui.dp(this, 54)));
        root.addView(settings, Ui.matchWrap(this, 18));

        LinearLayout middle = Ui.column(this);
        middle.setGravity(Gravity.CENTER);
        root.addView(middle, new LinearLayout.LayoutParams(-1, 0, 1));
        circle = Ui.column(this);
        circle.setGravity(Gravity.CENTER);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(Ui.GREEN);
        circle.setBackground(background);
        circle.setElevation(Ui.dp(this, 8));
        display = Ui.text(this, format((int) Math.ceil(remainingMs / 1000.0)), 68, Ui.WHITE, true);
        display.setGravity(Gravity.CENTER);
        circle.addView(display);
        circleHint = Ui.text(this, "탭하여 시작", 16, Ui.SOFT, true);
        circleHint.setGravity(Gravity.CENTER);
        circle.addView(circleHint);
        circle.setOnClickListener(v -> toggle());
        middle.addView(circle, new LinearLayout.LayoutParams(Ui.dp(this, 270), Ui.dp(this, 270)));

        LinearLayout alerts = Ui.row(this);
        vibrationSwitch = new Switch(this);
        vibrationSwitch.setText("진동");
        vibrationSwitch.setTextColor(Ui.INK);
        vibrationSwitch.setChecked(getPreferences(MODE_PRIVATE).getBoolean("vibration", true));
        vibrationSwitch.setOnCheckedChangeListener((button, enabled) -> getPreferences(MODE_PRIVATE).edit().putBoolean("vibration", enabled).apply());
        alerts.addView(vibrationSwitch, new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1));
        soundSwitch = new Switch(this);
        soundSwitch.setText("소리");
        soundSwitch.setTextColor(Ui.INK);
        soundSwitch.setChecked(getPreferences(MODE_PRIVATE).getBoolean("sound", false));
        soundSwitch.setOnCheckedChangeListener((button, enabled) -> getPreferences(MODE_PRIVATE).edit().putBoolean("sound", enabled).apply());
        alerts.addView(soundSwitch, new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1));
        root.addView(alerts, Ui.matchWrap(this, 10));

        TextView reset = Ui.action(this, "설정 시간으로 리셋", false);
        reset.setOnClickListener(v -> resetTimer());
        root.addView(reset, Ui.matchWrap(this, 10));
        TextView done = Ui.action(this, "메인 화면으로", true);
        done.setOnClickListener(v -> finish());
        root.addView(done);
        updateDisplay();
    }

    private boolean applyTimeOnStart() {
        int seconds = TimerSeconds.parse(timeInput.getText().toString());
        if (seconds < 1) {
            timeInput.setError("1초부터 5999초까지 숫자로 입력해 주세요.");
            return false;
        }
        durationSeconds = seconds;
        remainingMs = seconds * 1000L;
        timeInput.clearFocus();
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(timeInput.getWindowToken(), 0);
        return true;
    }

    private void toggle() {
        if (running) {
            remainingMs = Math.max(0, deadlineMs - SystemClock.elapsedRealtime());
            running = false;
            handler.removeCallbacks(tick);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            if (!hasStarted) {
                if (!applyTimeOnStart()) return;
                hasStarted = true;
            }
            deadlineMs = SystemClock.elapsedRealtime() + remainingMs;
            running = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            handler.removeCallbacks(tick);
            handler.post(tick);
        }
        updateDisplay();
    }

    private void resetTimer() {
        int seconds = TimerSeconds.parse(timeInput.getText().toString());
        if (seconds < 1) {
            timeInput.setError("1초부터 5999초까지 숫자로 입력해 주세요.");
            return;
        }
        running = false;
        hasStarted = false;
        handler.removeCallbacks(tick);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        durationSeconds = seconds;
        remainingMs = durationSeconds * 1000L;
        updateDisplay();
    }

    private void updateDisplay() {
        if (running) {
            remainingMs = Math.max(0, deadlineMs - SystemClock.elapsedRealtime());
            if (remainingMs == 0) {
                running = false;
                hasStarted = false;
                handler.removeCallbacks(tick);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                alertCompletion();
                Toast.makeText(this, "카운트다운이 완료되었습니다.", Toast.LENGTH_LONG).show();
            }
        }
        int seconds = (int) ((remainingMs + 999) / 1000);
        display.setText(format(seconds));
        String action = running ? "일시정지" : (hasStarted ? "이어 시작" : "시작");
        circleHint.setText("탭하여 " + action);
        circle.setContentDescription("남은 시간 " + display.getText() + ", 탭하여 " + action);
    }

    private static String format(int seconds) {
        return seconds + "초";
    }

    private void alertCompletion() {
        if (vibrationSwitch.isChecked()) {
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= 31) {
                VibratorManager manager = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
                vibrator = manager == null ? null : manager.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                VibrationEffect effect = VibrationEffect.createWaveform(
                        new long[]{0, 700, 250, 700}, -1);
                if (Build.VERSION.SDK_INT >= 33) {
                    VibrationAttributes attributes = new VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_ALARM).build();
                    vibrator.vibrate(effect, attributes);
                } else {
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM).build();
                    vibrator.vibrate(effect, attributes);
                }
                Log.i(LOG_TAG, "Timer completion vibration requested");
            } else {
                Log.w(LOG_TAG, "Timer completion vibration unavailable on this device");
            }
        }
        if (soundSwitch.isChecked()) {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 500);
            handler.postDelayed(tone::release, 650);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (running) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            handler.removeCallbacks(tick);
            handler.post(tick);
        }
    }

    @Override protected void onPause() {
        // Keep the deadline check alive when the screen is briefly backgrounded.
        super.onPause();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(tick);
        super.onDestroy();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("duration", durationSeconds);
        out.putLong("remaining", running ? Math.max(0, deadlineMs - SystemClock.elapsedRealtime()) : remainingMs);
        out.putBoolean("running", running);
        out.putBoolean("hasStarted", hasStarted);
        out.putString("timeInput", timeInput.getText().toString());
        super.onSaveInstanceState(out);
    }
}
