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

import java.util.Set;

import roboguice.util.Ln;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.nonninz.robomodel.TypedContentValues.ElementType;

class DatabaseManager {
    private final Context mContext;

    /**
     * @param context
     */
    public DatabaseManager(Context context) {
        mContext = context;
    }

    /**
     * @param tableName
     * @param column
     * @param type
     * @param db
     */
    private void addColumn(String tableName, String column, ElementType type, SQLiteDatabase db) {
        final String sql = String.format("ALTER TABLE %s ADD %s %s;", tableName, column,
                        type.name());
        db.execSQL(sql);
    }

    private long attemptSave(String tableName, TypedContentValues values, long id,
                    SQLiteDatabase database) {
        if (id == RoboModel.UNSAVED_MODEL_ID) {
            return database.insertOrThrow(tableName, null, values.toContentValues());
        } else {
            database.update(tableName, values.toContentValues(), _ID + " = " + id, null);
            return id;
        }
    }

    void clearUnusedColumns(String databaseName, String tableName, TypedContentValues values) {
        // TODO: implement
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
    private void createOrPopulateTable(String tableName, TypedContentValues values,
                    SQLiteDatabase db) {
        Ln.d("Fixing table %s...", tableName);
        // Check if table exists
        try {
            DatabaseUtils.queryNumEntries(db, tableName);
        } catch (final SQLiteException ex) {
            // If it doesn't, create it and return
            createTable(tableName, values, db);
            return;
        }

        // Otherwise, check if all fields exist
        final Set<String> keySet = values.keySet();
        for (final String column : keySet) {
            try {
                // Get type of column
                final Cursor typeCursor = db.rawQuery("select typeof (" + column + ") from "
                                + tableName, null);
                typeCursor.moveToFirst();
                final String type = typeCursor.getString(0);
                Ln.v("Type of %s is %s", column, type);

                // TODO: correct type?
            } catch (final SQLiteException e) {
                addColumn(tableName, column, values.getType(column), db);
            }
        }
    }

    /**
     * @param tableName
     * @param values
     * @param db
     * @return
     */
    private void createTable(String tableName, TypedContentValues values, SQLiteDatabase db) {
        final StringBuilder sql = new StringBuilder("CREATE TABLE ").append(tableName).append(" (");
        final Set<String> columns = values.keySet();
        for (final String column : columns) {
            sql.append(column).append(" ").append(values.getType(column).name()).append(", ");
        }
        sql.append(_ID).append(" integer primary key autoincrement);");
        Ln.d("Creating table: %s", sql.toString());
        db.execSQL(sql.toString());
    }

    void deleteRecord(String databaseName, String tableName, long id) {
        final SQLiteDatabase db = openOrCreateDatabase(databaseName);
        db.delete(tableName, _ID + " = " + id, null);
        db.close();
    }

    private SQLiteDatabase openOrCreateDatabase(String databaseName) {
        // TODO: allow other flags than MODE_PRIVATE?
        return mContext.openOrCreateDatabase(databaseName, 0, null);
    }

    long saveModel(String databaseName, String tableName, TypedContentValues values, long id) {
        final SQLiteDatabase database = openOrCreateDatabase(databaseName);

        // For optimizing speed, first try to save it. Then deal with errors (like table/field not existing);
        try {
            return attemptSave(tableName, values, id, database);
        } catch (final SQLiteException ex) {
            createOrPopulateTable(tableName, values, database);
            return attemptSave(tableName, values, id, database);
        } finally {
            database.close();
        }
    }
}
