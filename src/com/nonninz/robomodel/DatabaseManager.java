/**
 * Copyright 2012 Francesco Donadon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nonninz.robomodel;

import static android.provider.BaseColumns._ID;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.nonninz.robomodel.util.Ln;

/**
 * 
 * DatabaseManager:
 * 1. Ensures the correct schema for the Database
 * 2. Holds the database specific configuration
 * 
 */
class DatabaseManager {
    public static String where(long id) {
        return _ID + " = " + id;
    }

    public static String getTypeForField(Field field) {
        final Class<?> type = field.getType();

        if (type == String.class) {
            return "TEXT";
        } else if (type == Boolean.TYPE) {
            return "BOOLEAN";
        } else if (type == Byte.TYPE) {
            return "INTEGER";
        } else if (type == Double.TYPE) {
            return "REAL";
        } else if (type == Float.TYPE) {
            return "REAL";
        } else if (type == Integer.TYPE) {
            return "INTEGER";
        } else if (type == Long.TYPE) {
            return "INTEGER";
        } else if (type == Short.TYPE) {
            return "INTEGER";
        } else if (type.isEnum()) {
            return "TEXT";
        }
        else {
            return "TEXT";
        }
    }

    private static SQLiteDatabase sDatabase;
    private static String sDatabaseName;
    private final Context mContext;

    /**
     * @param context
     */
    public DatabaseManager(Context context) {
        mContext = context;
    }

    public String getDatabaseName() {
        if (sDatabaseName == null) {
            sDatabaseName = mContext.getPackageName();
        }
        return sDatabaseName;
    }

    /**
     * @param tableName
     * @param column
     * @param type
     * @param db
     */
    private void addColumn(String tableName, String column, String type, SQLiteDatabase db) {
        final String sql = String.format("ALTER TABLE %s ADD %s %s;", tableName, column,
                        type);
        db.execSQL(sql);
    }

    long insertOrUpdate(String tableName, TypedContentValues values, long id,
                    SQLiteDatabase database) {
        if (id == RoboModel.UNSAVED_MODEL_ID) {
            return database.insertOrThrow(tableName, null, values.toContentValues());
        } else {
            database.update(tableName, values.toContentValues(), where(id), null);
            return id;
        }
    }

    void dropTable(String tableName, SQLiteDatabase db) {
        final StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ").append(tableName).append(";");
        Ln.d("Dropping table: %s", sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * Creates the table or populates it with missing fields
     * 
     * @param tableName
     *            The name of the table
     * @param values
     *            The columns of the table
     * @param db
     *            The database where the table is situated
     * @throws SQLException
     *             if it cannot create the table
     */
    void createOrPopulateTable(String tableName, List<Field> fields,
                    SQLiteDatabase db) {

        Ln.d("Fixing table %s...", tableName);

        // Check if table exists
        try {
            DatabaseUtils.queryNumEntries(db, tableName);
        } catch (final SQLiteException ex) {
            // If it doesn't, create it and return
            createTable(tableName, fields, db);
            return;
        }

        // Otherwise, check if all fields exist
        for (final Field field : fields) {
            try {
                // Get type of column
                final Cursor typeCursor = db.rawQuery("select typeof (" + field.getName()
                                + ") from "
                                + tableName, null);
                typeCursor.moveToFirst();
                final String type = typeCursor.getString(0);
                Ln.v("Type of %s is %s", field.getName(), type);

                // TODO: correct type?
            } catch (final SQLiteException e) {
                // No such column
                addColumn(tableName, field.getName(), getTypeForField(field), db);
            } catch (final CursorIndexOutOfBoundsException e) {
                // No such column
                addColumn(tableName, field.getName(), getTypeForField(field), db);
            }
        }
    }

    /**
     * @param tableName
     * @param values
     * @param db
     * @return
     */
    private void createTable(String tableName, List<Field> fields, SQLiteDatabase db) {
        final StringBuilder sql = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");

        for (final Field field : fields) {
            sql.append(field.getName()).append(" ").append(getTypeForField(field)).append(", ");
        }
        sql.append(_ID).append(" integer primary key autoincrement);");
        Ln.d("Creating table: %s", sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * @param databaseName
     * @param tableName
     */
    public void deleteAllRecords(String databaseName, String tableName) {
        final SQLiteDatabase db = openOrCreateDatabase(databaseName);
        db.delete(tableName, null, null);
    }

    void deleteRecord(String databaseName, String tableName, long id) {
        final SQLiteDatabase db = openOrCreateDatabase(databaseName);
        db.delete(tableName, where(id), null);
    }

    SQLiteDatabase openOrCreateDatabase(String databaseName) {
        if (sDatabase == null) {
            sDatabase = mContext.getApplicationContext().openOrCreateDatabase(databaseName,
                            Context.MODE_PRIVATE, null);
        }
        return sDatabase;
    }
}
