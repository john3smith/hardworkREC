package com.local.hardworkrec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RecordActivity extends Activity {
    private WorkoutStore store;
    private List<WorkoutStore.Exercise> exercises;
    private Spinner exerciseSpinner;
    private EditText repsInput;
    private EditText timeInput;
    private boolean formattingTime;
    private String entryUuid;
    private WorkoutStore.Entry editingEntry;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store = new WorkoutStore(this);
        exercises = store.exercises();
        if (exercises.isEmpty()) { finish(); return; }
        entryUuid = getIntent().getStringExtra("entryUuid");
        if (entryUuid != null) {
            editingEntry = store.entryByUuid(entryUuid);
            if (editingEntry == null) {
                Toast.makeText(this, "수정할 기록을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui.BG);
        Ui.applySystemInsets(scroll, true);
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 22), Ui.dp(this, 24), Ui.dp(this, 22), Ui.dp(this, 28));
        scroll.addView(body);
        setContentView(scroll);

        TextView back = Ui.text(this, "←  돌아가기", 15, Ui.GREEN, true);
        back.setOnClickListener(v -> finish());
        body.addView(back, Ui.matchWrap(this, 25));
        body.addView(Ui.text(this, editingEntry == null ? "운동 기록하기" : "운동 기록 수정", 29, Ui.INK, true), Ui.matchWrap(this, 7));
        body.addView(Ui.text(this, editingEntry == null ? "오늘의 운동을 남겨보세요" : "저장한 기록을 고쳐보세요", 15, Ui.MUTED, false), Ui.matchWrap(this, 25));

        LinearLayout form = Ui.card(this);
        form.addView(Ui.text(this, "운동명", 15, Ui.INK, true), Ui.matchWrap(this, 10));
        exerciseSpinner = new Spinner(this);
        ArrayList<String> names = new ArrayList<>();
        for (WorkoutStore.Exercise exercise : exercises) names.add(exercise.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        exerciseSpinner.setAdapter(adapter);
        exerciseSpinner.setBackground(Ui.stroke(Ui.WHITE, Ui.BORDER, 13, this));
        exerciseSpinner.setPadding(Ui.dp(this, 13), 0, Ui.dp(this, 13), 0);
        form.addView(exerciseSpinner, new LinearLayout.LayoutParams(-1, Ui.dp(this, 54)));
        long selectedId = editingEntry == null ? getIntent().getLongExtra("exerciseId", -1) : editingEntry.exerciseId;
        for (int i = 0; i < exercises.size(); i++) if (exercises.get(i).id == selectedId) exerciseSpinner.setSelection(i);

        form.addView(Ui.gap(this, 22));
        form.addView(Ui.text(this, "횟수", 15, Ui.INK, true), Ui.matchWrap(this, 9));
        repsInput = new EditText(this);
        repsInput.setHint("예: 10회, 12회, 8회");
        repsInput.setTextSize(18);
        repsInput.setTextColor(Ui.INK);
        repsInput.setHintTextColor(Ui.MUTED);
        repsInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        repsInput.setMinLines(3);
        repsInput.setGravity(Gravity.TOP);
        repsInput.setPadding(Ui.dp(this, 15), Ui.dp(this, 12), Ui.dp(this, 15), Ui.dp(this, 12));
        repsInput.setBackground(Ui.stroke(Ui.WHITE, Ui.BORDER, 13, this));
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        form.addView(repsInput, new LinearLayout.LayoutParams(-1, -2));
        form.addView(Ui.gap(this, 24));
        form.addView(Ui.text(this, "운동 시간", 15, Ui.INK, true), Ui.matchWrap(this, 9));
        timeInput = numberInput("숫자만 연속 입력 · 예: 1234 → 12:34");
        timeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        timeInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (formattingTime) return;
                String formatted = TimeFormat.displayDigits(s.toString());
                if (!formatted.equals(s.toString())) {
                    formattingTime = true;
                    timeInput.setText(formatted);
                    timeInput.setSelection(formatted.length());
                    formattingTime = false;
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        form.addView(timeInput, new LinearLayout.LayoutParams(-1, Ui.dp(this, 55)));
        form.addView(Ui.gap(this, 10));
        form.addView(Ui.text(this, "오른쪽 두 자리는 초입니다. 125 → 1분 25초", 13, Ui.MUTED, false));
        body.addView(form, Ui.matchWrap(this, 22));

        TextView save = Ui.action(this, editingEntry == null ? "기록 저장" : "수정 저장", true);
        save.setOnClickListener(v -> saveRecord());
        body.addView(save, Ui.matchWrap(this, 10));
        body.addView(Ui.text(this, "횟수나 시간 중 하나만 입력해도 저장할 수 있습니다.", 13, Ui.MUTED, false));

        if (state != null) {
            repsInput.setText(state.getString("reps", ""));
            timeInput.setText(state.getString("time", ""));
            exerciseSpinner.setSelection(state.getInt("exerciseIndex", 0));
        } else if (editingEntry != null) {
            repsInput.setText(editingEntry.reps);
            if (editingEntry.durationSeconds > 0) {
                timeInput.setText(String.format(Locale.US, "%d:%02d",
                        editingEntry.durationSeconds / 60, editingEntry.durationSeconds % 60));
            }
        }
    }

    private EditText numberInput(String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(18);
        input.setTextColor(Ui.INK);
        input.setHintTextColor(Ui.MUTED);
        input.setHint(hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setPadding(Ui.dp(this, 15), 0, Ui.dp(this, 15), 0);
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setBackground(Ui.stroke(Ui.WHITE, Ui.BORDER, 13, this));
        return input;
    }

    private void saveRecord() {
        String reps = repsInput.getText().toString().trim();
        String rawTime = timeInput.getText().toString();
        int seconds = rawTime.isEmpty() ? 0 : TimeFormat.parseSeconds(rawTime);
        if (seconds < 0) { timeInput.setError("초는 00~59로 입력해 주세요."); return; }
        if (reps.isEmpty() && seconds == 0) {
            new AlertDialog.Builder(this).setMessage("횟수나 시간 중 하나를 입력해 주세요.").setPositiveButton("확인", null).show();
            return;
        }
        try {
            long exerciseId = exercises.get(exerciseSpinner.getSelectedItemPosition()).id;
            if (editingEntry == null) store.addEntry(exerciseId, reps, seconds);
            else store.updateEntry(entryUuid, exerciseId, reps, seconds);
            setResult(RESULT_OK);
            finish();
        } catch (Exception ex) {
            new AlertDialog.Builder(this).setTitle("저장 실패").setMessage(ex.getMessage()).setPositiveButton("확인", null).show();
        }
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putString("reps", repsInput.getText().toString());
        out.putString("time", timeInput.getText().toString());
        out.putInt("exerciseIndex", exerciseSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(out);
    }

    @Override protected void onDestroy() {
        store.close();
        super.onDestroy();
    }
}
