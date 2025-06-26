package com.example.bdapps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper_login extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="UserDatabase.db";
    private static final int DATABASE_VERSION=3;
    private static final String TABLE_USERS="users";

    private static final String COLUMN_ID="id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL="email";
    private static final String COLUMN_PASSWORD = "password";


    //Group Table
    private static final String TABLE_GROUPS="groups";
    private static final String COLUMN_GROUP_ID="group_id";
    private static final String COLUMN_GROUP_NAME="group_name";
    private static final String COLUMN_GROUP_DESCRIPTION="group_description";
    private static final String COLUMN_CREATED_BY = "created_by";
    private static final String COLUMN_CREATED_AT = "created_at";


    // Group members table (junction table)
    private static final String TABLE_GROUP_MEMBERS = "group_members";
    private static final String COLUMN_MEMBER_ID = "member_id";
    private static final String COLUMN_MEMBER_GROUP_ID = "group_id";
    private static final String COLUMN_MEMBER_USER_ID = "user_id";
    private static final String COLUMN_JOINED_AT = "joined_at";

    public DatabaseHelper_login(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CRATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +"("
                +COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
                +COLUMN_USERNAME+" TEXT UNIQUE," +
                COLUMN_NAME+" TEXT,"+
                COLUMN_EMAIL+" TEXT UNIQUE,"
                +COLUMN_PASSWORD +" TEXT"+")";
        db.execSQL(CRATE_USERS_TABLE);

        // Create groups table
        String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                + COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GROUP_NAME + " TEXT NOT NULL,"
                + COLUMN_GROUP_DESCRIPTION + " TEXT,"
                + COLUMN_CREATED_BY + " INTEGER,"
                + COLUMN_CREATED_AT + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CREATED_BY + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_GROUPS_TABLE);

        // Create group members table
        String CREATE_GROUP_MEMBERS_TABLE = "CREATE TABLE " + TABLE_GROUP_MEMBERS + "("
                + COLUMN_MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEMBER_GROUP_ID + " INTEGER,"
                + COLUMN_MEMBER_USER_ID + " INTEGER,"
                + COLUMN_JOINED_AT + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_MEMBER_GROUP_ID + ") REFERENCES " + TABLE_GROUPS + "(" + COLUMN_GROUP_ID + "),"
                + "FOREIGN KEY(" + COLUMN_MEMBER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
                + "UNIQUE(" + COLUMN_MEMBER_GROUP_ID + "," + COLUMN_MEMBER_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_GROUP_MEMBERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        onCreate(db);
    }

    public boolean addUser(String username, String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL,email);
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

    public User getUserById(int userId){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_USERS,null,COLUMN_ID+"=?",new String[]{String.valueOf(userId)},null,null,null);
        User user=null;

        if(cursor.moveToFirst()){
            user=new User(
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            );
        }
        cursor.close();
        db.close();
        return user;
    }

    public User getUserByUsername(String username){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_USERS,null,COLUMN_USERNAME+"=?",new String[]{username},null,null,null);
        User user=null;

        if(cursor.moveToFirst()){
            user=new User(
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            );
        }
        cursor.close();
        db.close();
        return user;
    }
    public List<User> searchUsers(String query){
        List<User> users=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();

        String searchQuery="SELECT * FROM "+ TABLE_USERS+" WHERE "+COLUMN_USERNAME + " LIKE ? OR " +
                COLUMN_NAME + " LIKE ? OR " +
                COLUMN_EMAIL + " LIKE ?";
        String searchPattern="%"+query+"%";
        Cursor cursor=db.rawQuery(searchQuery,new String[]{searchPattern,searchPattern,searchPattern});
        if(cursor.moveToFirst()){
            do{
                User user=new User(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                );
                users.add(user);
            }while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return users;
    }
    public List<User> getAllUsers(){
        List<User> users=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_USERS,null,null,null,null,null,COLUMN_NAME+" ASC");
        if(cursor.moveToFirst()){
            do {
                User user = new User(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                );
                users.add(user);
            } while (cursor.moveToNext());
            }
        cursor.close();
        db.close();
        return users;
        }
        public long createGroup(String groupName,String description, int createBy,List<Integer> mumberIds){
        SQLiteDatabase db=this.getWritableDatabase();
        db.beginTransaction();
        try{
            ContentValues groupValues=new ContentValues();
            groupValues.put(COLUMN_GROUP_NAME,groupName);
            groupValues.put(COLUMN_GROUP_DESCRIPTION,description);
            groupValues.put(COLUMN_CREATED_BY,createBy);
            groupValues.put(COLUMN_CREATED_AT,System.currentTimeMillis());

            long groupId=db.insert(TABLE_GROUPS,null,groupValues);

            if(groupId!=-1){
                addGroupMember(db,(int)groupId,createBy);
                for(int memberId:mumberIds){
                    if(memberId!=createBy){
                        addGroupMember(db,(int)groupId,memberId);
                    }
                }
                db.setTransactionSuccessful();
            }
            return groupId;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }finally {
            db.endTransaction();
            db.close();
        }
        }


    private void addGroupMember(SQLiteDatabase db, int groupId, int userId) {
        ContentValues memberValues=new ContentValues();
        memberValues.put(COLUMN_MEMBER_GROUP_ID,groupId);
        memberValues.put(COLUMN_MEMBER_ID,userId);
        memberValues.put(COLUMN_JOINED_AT,System.currentTimeMillis());
        db.insert(TABLE_GROUP_MEMBERS,null,memberValues);
    }

    public List<User> getGroupMembers(int groupId){
        List<User> members=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        String query="SELECT u.* FROM " + TABLE_USERS + " u " +
                "INNER JOIN " + TABLE_GROUP_MEMBERS + " gm ON u." + COLUMN_ID + " = gm." + COLUMN_MEMBER_USER_ID + " " +
                "WHERE gm." + COLUMN_MEMBER_GROUP_ID + " = ? " +
                "ORDER BY u." + COLUMN_NAME + " ASC";
        Cursor cursor=db.rawQuery(query,new String[]{String.valueOf(groupId)});

        if(cursor.moveToFirst()){
            do {
                User user=new User(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                );
                members.add(user);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return members;
    }

    public Group getGroupById(int groupId){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_GROUPS,null,COLUMN_GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, null, null, null);

        Group group=null;
        if(cursor.moveToFirst()){
            group = new Group(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_DESCRIPTION)),
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CREATED_BY)))
            );
            group.setGroupId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GROUP_ID))));
            group.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        }
        cursor.close();
        db.close();
        return group;
    }

    public boolean deleteGroup(int groupId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Delete group members first
            db.delete(TABLE_GROUP_MEMBERS, COLUMN_MEMBER_GROUP_ID + "=?", new String[]{String.valueOf(groupId)});

            // Delete group
            int result = db.delete(TABLE_GROUPS, COLUMN_GROUP_ID + "=?", new String[]{String.valueOf(groupId)});

            db.setTransactionSuccessful();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public List<Group> getUserGroups(int userId){
        List<Group> userGroups=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        String query="SELECT g.* FROM "+TABLE_GROUPS+" g "+"INNER JOIN "
                +TABLE_GROUP_MEMBERS + " gm ON g." + COLUMN_GROUP_ID + " = gm." + COLUMN_MEMBER_GROUP_ID + " " +
                "WHERE gm." + COLUMN_MEMBER_USER_ID + " = ? " +
                "ORDER BY g." + COLUMN_GROUP_NAME + " ASC";
        Cursor cursor=db.rawQuery(query,new String[]{String.valueOf(userId)});

        if(cursor.moveToFirst()){
            do {
                Group group=new Group(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GROUP_DESCRIPTION)),
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CREATED_BY)))
                );
                group.setGroupId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GROUP_ID))));
                group.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                userGroups.add(group);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userGroups;
    }
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        int userId = -1; // Return -1 if user not found

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }

        cursor.close();
        db.close();
        return userId;
    }

}
