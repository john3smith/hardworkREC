package com.local.hardworkrec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final int CREATE_BACKUP = 11;
    private static final int OPEN_BACKUP = 12;
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN);
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN);
    private WorkoutStore store;
    private LinearLayout content;
    private LinearLayout bottomBar;
    private int tab = 0;
    private YearMonth month = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();
    private String backupStatus = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store = new WorkoutStore(this);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.WHITE);
        if (state != null) {
            tab = state.getInt("tab", 0);
            month = YearMonth.parse(state.getString("month", YearMonth.now().toString()));
            selectedDate = LocalDate.parse(state.getString("date", LocalDate.now().toString()));
        }
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(Ui.BG);
        Ui.applySystemInsets(root, false);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        content = Ui.column(this);
        content.setPadding(Ui.dp(this, 22), Ui.dp(this, 27), Ui.dp(this, 22), Ui.dp(this, 28));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        bottomBar = Ui.row(this);
        bottomBar.setBackgroundColor(Ui.WHITE);
        bottomBar.setPadding(Ui.dp(this, 14), Ui.dp(this, 7), Ui.dp(this, 14), Ui.dp(this, 7));
        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, Ui.dp(this, 66)));
        setContentView(root);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if (content != null) render();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("tab", tab);
        out.putString("month", month.toString());
        out.putString("date", selectedDate.toString());
        super.onSaveInstanceState(out);
    }

    @Override protected void onDestroy() {
        store.close();
        super.onDestroy();
    }

    private void render() {
        content.removeAllViews();
        bottomBar.removeAllViews();
        if (tab == 0) renderHome();
        else if (tab == 1) renderCalendar();
        else renderBackup();
        addTab("오늘", 0);
        addTab("달력", 1);
        addTab("백업", 2);
    }

    private void addTab(String label, int index) {
        TextView view = Ui.text(this, label, 15, tab == index ? Ui.GREEN : Ui.MUTED, tab == index);
        view.setGravity(Gravity.CENTER);
        view.setBackground(tab == index ? Ui.shape(Ui.SOFT, 13, this) : Ui.shape(Ui.WHITE, 13, this));
        view.setContentDescription(label + " 탭");
        view.setOnClickListener(v -> { tab = index; render(); });
        bottomBar.addView(view, new LinearLayout.LayoutParams(0, -1, 1));
    }

    private void renderHome() {
        LinearLayout tools = Ui.row(this);
        TextView count = Ui.action(this, "카운트", false);
        count.setOnClickListener(v -> startActivity(new Intent(this, CountActivity.class)));
        tools.addView(count, new LinearLayout.LayoutParams(0, -2, 1));
        TextView timer = Ui.action(this, "타이머", false);
        timer.setOnClickListener(v -> startActivity(new Intent(this, CountdownActivity.class)));
        LinearLayout.LayoutParams timerLayout = new LinearLayout.LayoutParams(0, -2, 1);
        timerLayout.leftMargin = Ui.dp(this, 10);
        tools.addView(timer, timerLayout);
        content.addView(tools, Ui.matchWrap(this, 18));

        LinearLayout header = Ui.row(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(Ui.text(this, "HARDWORK / REC", 13, Ui.GREEN, true),
                new LinearLayout.LayoutParams(0, -2, 1));
        TextView add = Ui.text(this, "+ 운동명 추가", 13, Ui.GREEN, true);
        add.setGravity(Gravity.CENTER);
        add.setPadding(Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8));
        add.setBackground(Ui.shape(Ui.SOFT, 12, this));
        add.setContentDescription("운동명 추가");
        add.setOnClickListener(v -> showAddExercise());
        header.addView(add);
        content.addView(header, Ui.matchWrap(this, 16));
        content.addView(Ui.text(this, "오늘도 한 걸음", 29, Ui.INK, true), Ui.matchWrap(this, 4));
        content.addView(Ui.text(this, LocalDate.now().format(DAY_LABEL), 15, Ui.MUTED, false), Ui.matchWrap(this, 24));

        TextView record = Ui.action(this, "＋  운동 기록하기", true);
        record.setOnClickListener(v -> showWorkoutPopup());
        content.addView(record, Ui.matchWrap(this, 25));

        List<WorkoutStore.Entry> today = store.entriesForDate(LocalDate.now());
        sectionTitle("오늘의 기록", today.size() + "개");
        if (today.isEmpty()) {
            content.addView(Ui.text(this, "아직 기록이 없습니다.", 14, Ui.MUTED, false));
        } else {
            for (int i = 0; i < today.size(); i++) addHomeEntryCard(today.get(i), i + 1);
        }
    }

    private void showWorkoutPopup() {
        final AlertDialog[] popup = new AlertDialog[1];
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 20), Ui.dp(this, 8), Ui.dp(this, 20), Ui.dp(this, 8));
        List<WorkoutStore.Exercise> exercises = store.exercises();
        body.addView(Ui.text(this, "운동 목록", 18, Ui.INK, true), Ui.matchWrap(this, 12));
        if (exercises.isEmpty()) {
            body.addView(Ui.text(this, "등록된 운동이 없습니다. 오른쪽 위에서 운동명을 추가해 주세요.",
                    14, Ui.MUTED, false), Ui.matchWrap(this, 12));
        } else {
            for (WorkoutStore.Exercise exercise : exercises) {
                TextView item = Ui.action(this, exercise.name + "  →", false);
                body.addView(item, Ui.matchWrap(this, 8));
                item.setOnClickListener(v -> {
                    popup[0].dismiss();
                    Intent intent = new Intent(this, RecordActivity.class);
                    intent.putExtra("exerciseId", exercise.id);
                    startActivity(intent);
                });
            }
        }
        ScrollView scroll = new ScrollView(this);
        scroll.addView(body);
        popup[0] = new AlertDialog.Builder(this).setTitle("운동 기록하기")
                .setView(scroll).setNegativeButton("닫기", null).create();
        popup[0].show();
    }

    private void showAddExercise() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("예: 스쿼트, 달리기");
        input.setPadding(Ui.dp(this, 20), Ui.dp(this, 12), Ui.dp(this, 20), Ui.dp(this, 12));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("운동명 추가")
                .setView(input)
                .setNegativeButton("취소", null)
                .setPositiveButton("추가", null)
                .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button -> {
            try {
                long id = store.addExercise(input.getText().toString());
                if (id < 0) { input.setError("이미 등록한 운동입니다."); return; }
                dialog.dismiss();
                render();
            } catch (IllegalArgumentException ex) { input.setError(ex.getMessage()); }
        }));
        dialog.show();
    }

    private void sectionTitle(String label, String count) {
        LinearLayout row = Ui.row(this);
        row.addView(Ui.text(this, label, 20, Ui.INK, true), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(Ui.text(this, count, 14, Ui.MUTED, false));
        content.addView(row, Ui.matchWrap(this, 12));
    }

    private void addEntryCard(WorkoutStore.Entry entry) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, entry.exerciseName, 17, Ui.INK, true), Ui.matchWrap(this, 5));
        String details = entry.reps + (entry.durationSeconds > 0 ? "  ·  " + TimeFormat.readable(entry.durationSeconds) : "");
        card.addView(Ui.text(this, details, 14, Ui.MUTED, false));
        content.addView(card, Ui.matchWrap(this, 9));
    }

    private void addHomeEntryCard(WorkoutStore.Entry entry, int number) {
        LinearLayout card = Ui.card(this);
        LinearLayout row = Ui.row(this);
        row.addView(Ui.text(this, number + ".", 17, Ui.INK, true));
        EditText nameInput = new EditText(this);
        nameInput.setSingleLine(true);
        nameInput.setText(entry.exerciseName);
        nameInput.setTextSize(17);
        nameInput.setTextColor(Ui.INK);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
        nameInput.setPadding(Ui.dp(this, 10), Ui.dp(this, 5), Ui.dp(this, 10), Ui.dp(this, 5));
        nameInput.setBackground(Ui.stroke(Ui.WHITE, Ui.BORDER, 9, this));
        nameInput.setContentDescription(number + "번째 운동명 수정");
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 45), 1);
        nameParams.leftMargin = Ui.dp(this, 8);
        nameParams.rightMargin = Ui.dp(this, 8);
        row.addView(nameInput, nameParams);
        nameInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) return false;
            saveExerciseName(entry, nameInput);
            return true;
        });
        card.addView(row, Ui.matchWrap(this, 6));
        EditText repsInput = new EditText(this);
        repsInput.setText(entry.reps);
        repsInput.setHint("횟수 입력");
        repsInput.setTextSize(15);
        repsInput.setTextColor(Ui.INK);
        repsInput.setHintTextColor(Ui.MUTED);
        repsInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        repsInput.setMinLines(1);
        repsInput.setMaxLines(5);
        repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        repsInput.setPadding(Ui.dp(this, 10), Ui.dp(this, 5), Ui.dp(this, 10), Ui.dp(this, 5));
        repsInput.setBackground(Ui.stroke(Ui.WHITE, Ui.BORDER, 9, this));
        repsInput.setContentDescription(number + "번째 횟수 바로 수정, 자동 저장");
        repsInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable value) {
                try {
                    store.updateEntryReps(entry.uuid, value.toString());
                    repsInput.setError(null);
                } catch (IllegalArgumentException ex) {
                    repsInput.setError(ex.getMessage());
                }
            }
        });
        card.addView(repsInput, Ui.matchWrap(this, entry.durationSeconds > 0 ? 5 : 0));
        if (entry.durationSeconds > 0)
            card.addView(Ui.text(this, TimeFormat.readable(entry.durationSeconds), 14, Ui.MUTED, false));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordActivity.class);
            intent.putExtra("entryUuid", entry.uuid);
            startActivity(intent);
        });
        content.addView(card, Ui.matchWrap(this, 9));
    }

    private void saveExerciseName(WorkoutStore.Entry entry, EditText input) {
        String nextName = input.getText().toString().trim();
        if (nextName.equals(entry.exerciseName)) { input.clearFocus(); return; }
        try {
            store.renameExercise(entry.exerciseId, nextName);
            input.clearFocus();
            render();
        } catch (IllegalArgumentException ex) {
            input.setError(ex.getMessage());
        }
    }

    private void renderCalendar() {
        content.addView(Ui.text(this, "나의 운동 달력", 28, Ui.INK, true), Ui.matchWrap(this, 6));
        content.addView(Ui.text(this, "운동한 날을 한눈에 확인하세요", 14, Ui.MUTED, false), Ui.matchWrap(this, 25));
        LinearLayout card = Ui.card(this);
        LinearLayout header = Ui.row(this);
        TextView previous = Ui.text(this, "‹", 32, Ui.GREEN, true);
        previous.setGravity(Gravity.CENTER);
        previous.setContentDescription("이전 달");
        previous.setOnClickListener(v -> { month = month.minusMonths(1); selectedDate = month.atDay(1); render(); });
        header.addView(previous, new LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 48)));
        TextView title = Ui.text(this, month.format(MONTH_LABEL), 19, Ui.INK, true);
        title.setGravity(Gravity.CENTER);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView next = Ui.text(this, "›", 32, Ui.GREEN, true);
        next.setGravity(Gravity.CENTER);
        next.setContentDescription("다음 달");
        next.setOnClickListener(v -> { month = month.plusMonths(1); selectedDate = month.atDay(1); render(); });
        header.addView(next, new LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 48)));
        card.addView(header, Ui.matchWrap(this, 14));

        LinearLayout weekdays = Ui.row(this);
        for (String day : new String[]{"월", "화", "수", "목", "금", "토", "일"}) {
            TextView label = Ui.text(this, day, 13, Ui.MUTED, true);
            label.setGravity(Gravity.CENTER);
            weekdays.addView(label, new LinearLayout.LayoutParams(0, Ui.dp(this, 28), 1));
        }
        card.addView(weekdays);
        Set<LocalDate> activeDays = new HashSet<>();
        for (WorkoutStore.Entry entry : store.entriesForMonth(month)) {
            activeDays.add(java.time.Instant.ofEpochMilli(entry.recordedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        int offset = month.atDay(1).getDayOfWeek().getValue() - 1;
        int slots = ((offset + month.lengthOfMonth() + 6) / 7) * 7;
        for (int start = 0; start < slots; start += 7) {
            LinearLayout week = Ui.row(this);
            for (int column = 0; column < 7; column++) {
                int day = start + column - offset + 1;
                LinearLayout cell = Ui.column(this);
                cell.setGravity(Gravity.CENTER);
                week.addView(cell, new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1));
                if (day < 1 || day > month.lengthOfMonth()) continue;
                LocalDate date = month.atDay(day);
                boolean selected = date.equals(selectedDate);
                cell.setBackground(Ui.shape(selected ? Ui.SOFT : Ui.WHITE, 12, this));
                TextView number = Ui.text(this, Integer.toString(day), 16, selected ? Ui.GREEN : Ui.INK, selected);
                number.setGravity(Gravity.CENTER);
                cell.addView(number);
                View dot = new View(this);
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(activeDays.contains(date) ? Ui.GREEN : Ui.WHITE);
                dot.setBackground(circle);
                LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(Ui.dp(this, 6), Ui.dp(this, 6));
                dotParams.topMargin = Ui.dp(this, 4);
                cell.addView(dot, dotParams);
                cell.setContentDescription(date + (activeDays.contains(date) ? " 운동 기록 있음" : " 운동 기록 없음"));
                cell.setOnClickListener(v -> { selectedDate = date; render(); });
            }
            card.addView(week);
        }
        content.addView(card, Ui.matchWrap(this, 23));
        sectionTitle(selectedDate.format(DAY_LABEL), "");
        List<WorkoutStore.Entry> entries = store.entriesForDate(selectedDate);
        if (entries.isEmpty()) content.addView(Ui.text(this, "이날의 운동 기록이 없습니다.", 14, Ui.MUTED, false));
        else for (WorkoutStore.Entry entry : entries) addEntryCard(entry);
    }

    private void renderBackup() {
        content.addView(Ui.text(this, "기록 백업", 28, Ui.INK, true), Ui.matchWrap(this, 6));
        content.addView(Ui.text(this, "소중한 운동 기록을 파일로 보관하세요", 14, Ui.MUTED, false), Ui.matchWrap(this, 25));
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Google Drive에 보관하기", 20, Ui.INK, true), Ui.matchWrap(this, 9));
        card.addView(Ui.text(this, "아래 버튼을 누른 뒤 저장 위치에서 Google Drive를 선택하세요. 운동명과 기록을 JSON 파일로 저장합니다.", 15, Ui.MUTED, false), Ui.matchWrap(this, 21));
        TextView backup = Ui.action(this, "백업 파일 저장", true);
        backup.setOnClickListener(v -> launchCreateBackup());
        card.addView(backup, Ui.matchWrap(this, 10));
        TextView restore = Ui.action(this, "백업 파일 가져오기", false);
        restore.setOnClickListener(v -> launchOpenBackup());
        card.addView(restore);
        content.addView(card, Ui.matchWrap(this, 16));
        LinearLayout notice = Ui.card(this);
        notice.setBackground(Ui.shape(Ui.SOFT, 20, this));
        notice.addView(Ui.text(this, "알아두세요", 16, Ui.GREEN, true), Ui.matchWrap(this, 7));
        notice.addView(Ui.text(this, "자동 동기화가 아닌 수동 백업입니다. 가져오기는 기존 기록을 지우지 않고 중복되지 않는 기록만 추가합니다. Google Drive가 목록에 없다면 기기에 Drive 앱과 계정 설정이 필요합니다.", 14, Ui.INK, false));
        content.addView(notice, Ui.matchWrap(this, 12));
        if (!backupStatus.isEmpty()) content.addView(Ui.text(this, backupStatus, 14, Ui.GREEN, true));
    }

    private void launchCreateBackup() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "hardworkREC-backup-" + LocalDate.now() + ".json");
        startActivityForResult(intent, CREATE_BACKUP);
    }

    private void launchOpenBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_BACKUP);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == CREATE_BACKUP) {
            try (OutputStream stream = getContentResolver().openOutputStream(uri, "w")) {
                if (stream == null) throw new IllegalStateException("저장 위치를 열 수 없습니다.");
                stream.write(store.exportJson().getBytes(StandardCharsets.UTF_8));
                stream.flush();
                backupStatus = "선택한 위치에 백업 파일을 저장했습니다.";
                Toast.makeText(this, backupStatus, Toast.LENGTH_LONG).show();
                render();
            } catch (Exception ex) { showError("백업 실패", ex); }
        } else if (requestCode == OPEN_BACKUP) {
            new AlertDialog.Builder(this)
                    .setTitle("백업 가져오기")
                    .setMessage("기존 기록은 그대로 두고, 중복되지 않는 운동명과 기록을 추가합니다.")
                    .setNegativeButton("취소", null)
                    .setPositiveButton("가져오기", (dialog, which) -> restoreFrom(uri))
                    .show();
        }
    }

    private void restoreFrom(Uri uri) {
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            if (stream == null) throw new IllegalStateException("백업 파일을 열 수 없습니다.");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                if (bytes.size() + read > 10_000_000) throw new IllegalArgumentException("백업 파일이 너무 큽니다.");
                bytes.write(buffer, 0, read);
            }
            int inserted = store.importJson(bytes.toString(StandardCharsets.UTF_8.name()));
            backupStatus = "가져오기 완료 · 새 기록 " + inserted + "개";
            Toast.makeText(this, backupStatus, Toast.LENGTH_LONG).show();
            render();
        } catch (Exception ex) { showError("가져오기 실패", ex); }
    }

    private void showError(String title, Exception ex) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(ex.getMessage()).setPositiveButton("확인", null).show();
    }
}
