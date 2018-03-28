package com.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.download.lib.Downloader;

/**
 * Created by peiboning on 2018/3/14.
 */

public class SqliteDB extends SQLiteOpenHelper {
    public static String default_db_name = "download_lib.db";
    public SqliteDB(Context context, String name) {
        super(context, TextUtils.isEmpty(name)?default_db_name:name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TaskTable.getCreateSql());
        db.execSQL(ExecutorTable.getCreateSql());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class TaskTable{
        public static String TABLE_NAME = "task";

        public static String _ID = "_id";
        public static String URL = "url";
        public static String KEY = "key";

        public static String getCreateSql(){
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS ")
                    .append(TABLE_NAME)
                    .append("(")
                    .append(_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(URL).append(" TEXT,")
                    .append(KEY).append(" TEXT")
                    .append(")");
            return sb.toString();
        }
    }

    public static class ExecutorTable{
        public static String TABLE_NAME = "executor";

        public static String _ID      = "_id";
        public static String URL      = "url";
        public static String KEY      = "key";
        public static String BEGIN    = "begin";
        public static String END      = "end";
        public static String PROGRESS = "progress";
        public static String INDEX    = "_index";

        public static String getCreateSql(){
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS ")
                    .append(TABLE_NAME)
                    .append("(")
                    .append(_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(URL).append(" TEXT,")
                    .append(KEY).append(" TEXT,")
                    .append(BEGIN).append(" INTEGER,")
                    .append(END).append(" INTEGER,")
                    .append(PROGRESS).append(" INTEGER,")
                    .append(INDEX).append(" INTEGER")
                    .append(")");
            return sb.toString();
        }
    }
}
