package com.nonninz.robomodel.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fra on 15/11/15.
 */
public class DbUtils {
    private DbUtils() {}

    public static Map<String, String> getTableColumns(String tableName, SQLiteDatabase db) {
        final HashMap<String, String> map = new HashMap<>();
        final String sql = String.format("PRAGMA table_info(%s)", tableName);
        final Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            final String columnName = cursor.getString(1);
            final String columnType = cursor.getString(2).toUpperCase();
            map.put(columnName, columnType);
        }

        return map;
    }
}
