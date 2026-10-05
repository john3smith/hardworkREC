package com.local.hardworkrec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class WorkoutStore extends SQLiteOpenHelper {
    public static final class Exercise {
        public final long id;
        public final String name;
        Exercise(long id, String name) { this.id = id; this.name = name; }
    }

    public static final class Entry {
        public final String uuid;
        public final long exerciseId;
        public final String exerciseName;
        public final String reps;
        public final int durationSeconds;
        public final long recordedAt;
        Entry(String uuid, String exerciseName, String reps, int durationSeconds, long recordedAt) {
            this(uuid, -1, exerciseName, reps, durationSeconds, recordedAt);
        }
        Entry(String uuid, long exerciseId, String exerciseName, String reps, int durationSeconds, long recordedAt) {
            this.uuid = uuid;
            this.exerciseId = exerciseId;
            this.exerciseName = exerciseName;
            this.reps = reps;
            this.durationSeconds = durationSeconds;
            this.recordedAt = recordedAt;
        }
    }

    private static final String DB_NAME = "hardwork.db";

    public WorkoutStore(Context context) { super(context, DB_NAME, null, 2); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE exercises (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL COLLATE NOCASE UNIQUE, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE entries (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, exercise_id INTEGER NOT NULL REFERENCES exercises(id), reps INTEGER NOT NULL DEFAULT 0, reps_text TEXT NOT NULL DEFAULT '', duration_seconds INTEGER NOT NULL, recorded_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX entries_date_idx ON entries(recorded_at)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE entries ADD COLUMN reps_text TEXT NOT NULL DEFAULT ''");
            db.execSQL("UPDATE entries SET reps_text=CAST(reps AS TEXT) WHERE reps>0");
        }
    }

    public long addExercise(String rawName) {
        String name = validExerciseName(rawName);
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insertWithOnConflict("exercises", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void renameExercise(long exerciseId, String rawName) {
        String name = validExerciseName(rawName);
        if (exerciseId <= 0) throw new IllegalArgumentException("수정할 운동을 찾을 수 없습니다.");
        ContentValues values = new ContentValues();
        values.put("name", name);
        int updated = getWritableDatabase().updateWithOnConflict(
                "exercises", values, "id=?", new String[]{Long.toString(exerciseId)}, SQLiteDatabase.CONFLICT_IGNORE);
        if (updated != 1) throw new IllegalArgumentException("이미 등록한 운동명이거나 수정할 운동을 찾을 수 없습니다.");
    }

    private static String validExerciseName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty() || name.length() > 60) throw new IllegalArgumentException("운동명은 1~60자로 입력해 주세요.");
        return name;
    }

    public List<Exercise> exercises() {
        List<Exercise> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT id, name FROM exercises ORDER BY name COLLATE NOCASE", null)) {
            while (cursor.moveToNext()) result.add(new Exercise(cursor.getLong(0), cursor.getString(1)));
        }
        return result;
    }

    public void addEntry(long exerciseId, String reps, int seconds) {
        reps = validReps(reps, seconds);
        ContentValues values = new ContentValues();
        values.put("uuid", UUID.randomUUID().toString());
        values.put("exercise_id", exerciseId);
        values.put("reps", 0);
        values.put("reps_text", reps);
        values.put("duration_seconds", seconds);
        values.put("recorded_at", System.currentTimeMillis());
        getWritableDatabase().insertOrThrow("entries", null, values);
    }

    public void updateEntry(String uuid, long exerciseId, String reps, int seconds) {
        if (uuid == null || uuid.isEmpty()) throw new IllegalArgumentException("수정할 기록을 찾을 수 없습니다.");
        reps = validReps(reps, seconds);
        ContentValues values = new ContentValues();
        values.put("exercise_id", exerciseId);
        values.put("reps", 0);
        values.put("reps_text", reps);
        values.put("duration_seconds", seconds);
        if (getWritableDatabase().update("entries", values, "uuid=?", new String[]{uuid}) != 1)
            throw new IllegalArgumentException("수정할 기록을 찾을 수 없습니다.");
    }

    public void updateEntryReps(String uuid, String reps) {
        Entry existing = entryByUuid(uuid);
        if (existing == null) throw new IllegalArgumentException("수정할 기록을 찾을 수 없습니다.");
        reps = validReps(reps, existing.durationSeconds);
        ContentValues values = new ContentValues();
        values.put("reps", 0);
        values.put("reps_text", reps);
        if (getWritableDatabase().update("entries", values, "uuid=?", new String[]{uuid}) != 1)
            throw new IllegalArgumentException("수정할 기록을 찾을 수 없습니다.");
    }

    public Entry entryByUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        String sql = "SELECT e.uuid, e.exercise_id, x.name, e.reps_text, e.duration_seconds, e.recorded_at "
                + "FROM entries e JOIN exercises x ON x.id=e.exercise_id WHERE e.uuid=? LIMIT 1";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{uuid})) {
            return cursor.moveToFirst() ? readEntry(cursor) : null;
        }
    }

    private static long startOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public List<Entry> entriesForDate(LocalDate date) {
        return entriesBetween(startOfDay(date), startOfDay(date.plusDays(1)));
    }

    public List<Entry> entriesForMonth(YearMonth month) {
        return entriesBetween(startOfDay(month.atDay(1)), startOfDay(month.plusMonths(1).atDay(1)));
    }

    public List<Entry> allEntries() {
        return entriesBetween(0, Long.MAX_VALUE);
    }

    private List<Entry> entriesBetween(long from, long to) {
        List<Entry> result = new ArrayList<>();
        String sql = "SELECT e.uuid, e.exercise_id, x.name, e.reps_text, e.duration_seconds, e.recorded_at "
                + "FROM entries e JOIN exercises x ON x.id=e.exercise_id "
                + "WHERE e.recorded_at>=? AND e.recorded_at<? ORDER BY e.recorded_at ASC, e.id ASC";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{Long.toString(from), Long.toString(to)})) {
            while (cursor.moveToNext()) result.add(readEntry(cursor));
        }
        return result;
    }

    private static Entry readEntry(Cursor cursor) {
        return new Entry(cursor.getString(0), cursor.getLong(1), cursor.getString(2),
                cursor.getString(3), cursor.getInt(4), cursor.getLong(5));
    }

    public String exportJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", 2);
        root.put("exportedAt", System.currentTimeMillis());
        JSONArray exercisesArray = new JSONArray();
        for (Exercise exercise : exercises()) exercisesArray.put(exercise.name);
        root.put("exercises", exercisesArray);
        JSONArray entriesArray = new JSONArray();
        for (Entry entry : allEntries()) {
            JSONObject item = new JSONObject();
            item.put("uuid", entry.uuid);
            item.put("exerciseName", entry.exerciseName);
            item.put("reps", entry.reps);
            item.put("durationSeconds", entry.durationSeconds);
            item.put("recordedAt", entry.recordedAt);
            entriesArray.put(item);
        }
        root.put("entries", entriesArray);
        return root.toString(2);
    }

    /** Merges a backup; repeated imports do not duplicate entries. No existing data is deleted. */
    public int importJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        int schemaVersion = root.getInt("schemaVersion");
        if (schemaVersion != 1 && schemaVersion != 2) throw new IllegalArgumentException("지원하지 않는 백업 버전입니다.");
        JSONArray exerciseArray = root.getJSONArray("exercises");
        JSONArray entryArray = root.getJSONArray("entries");
        if (exerciseArray.length() > 50000 || entryArray.length() > 50000) throw new IllegalArgumentException("백업 항목이 너무 많습니다.");
        Set<String> names = new HashSet<>();
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < exerciseArray.length(); i++) names.add(validName(exerciseArray.getString(i)));
        for (int i = 0; i < entryArray.length(); i++) {
            JSONObject item = entryArray.getJSONObject(i);
            String uuid = UUID.fromString(item.getString("uuid")).toString();
            String name = validName(item.getString("exerciseName"));
            String reps = schemaVersion == 1 ? (item.getInt("reps") == 0 ? "" : item.getString("reps")) : item.getString("reps");
            int seconds = item.getInt("durationSeconds");
            long at = item.getLong("recordedAt");
            if (at <= 0 || at > System.currentTimeMillis() + 86400000L) {
                throw new IllegalArgumentException("백업 기록에 잘못된 값이 있습니다.");
            }
            reps = validReps(reps, seconds);
            names.add(name);
            entries.add(new Entry(uuid, name, reps, seconds, at));
        }

        SQLiteDatabase db = getWritableDatabase();
        int inserted = 0;
        db.beginTransaction();
        try {
            for (String name : names) {
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("created_at", System.currentTimeMillis());
                db.insertWithOnConflict("exercises", null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            for (Entry entry : entries) {
                long exerciseId = exerciseId(db, entry.exerciseName);
                if (exerciseId < 0) throw new IllegalArgumentException("백업 운동명을 찾을 수 없습니다.");
                ContentValues values = new ContentValues();
                values.put("uuid", entry.uuid);
                values.put("exercise_id", exerciseId);
                values.put("reps", 0);
                values.put("reps_text", entry.reps);
                values.put("duration_seconds", entry.durationSeconds);
                values.put("recorded_at", entry.recordedAt);
                if (db.insertWithOnConflict("entries", null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1) inserted++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return inserted;
    }

    private static String validName(String input) {
        String name = input.trim();
        if (name.isEmpty() || name.length() > 60) throw new IllegalArgumentException("백업의 운동명이 올바르지 않습니다.");
        return name;
    }

    private static String validReps(String input, int seconds) {
        String reps = input == null ? "" : input.trim();
        if (reps.length() > 500) throw new IllegalArgumentException("횟수 기록은 500자 이내로 입력해 주세요.");
        if (seconds < 0 || (reps.isEmpty() && seconds == 0))
            throw new IllegalArgumentException("횟수 또는 시간을 입력해 주세요.");
        return reps;
    }

    private static long exerciseId(SQLiteDatabase db, String name) {
        try (Cursor cursor = db.rawQuery("SELECT id FROM exercises WHERE name=? COLLATE NOCASE LIMIT 1", new String[]{name})) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        }
    }
}
