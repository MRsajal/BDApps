package com.example.bdapps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DatabaseHelper_login extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="UserDatabase.db";
    private static final int DATABASE_VERSION=1;
    private static final String TABLE_USERS="users";

    private static final String COLUMN_ID="id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_INSTITUTION = "institution";
    private static final String COLUMN_SERIES = "series";
    private static final String COLUMN_PASSWORD = "password";
    public DatabaseHelper_login(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CRATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +"("
                +COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
                +COLUMN_USERNAME+" TEXT UNIQUE," +
                COLUMN_NAME+" TEXT,"+
                COLUMN_INSTITUTION+" TEXT,"+
                COLUMN_SERIES + " TEXT,"
                +COLUMN_PASSWORD +" TEXT"+")";
        db.execSQL(CRATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String username, String name, String institution, String series, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_INSTITUTION, institution);
        values.put(COLUMN_SERIES, series);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }
}
