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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nonninz.robomodel.exceptions.InstanceNotFoundException;
import com.nonninz.robomodel.exceptions.JsonException;
import com.nonninz.robomodel.util.Ln;

/**
 * @author Francesco Donadon <francesco.donadon@gmail.com>
 * 
 *         RoboManager:
 *         1. Provides an interface to conveniently query and operate the DB for RoboModel instances with:
 *         - all()
 *         - first() - TODO
 *         - last()
 *         - find(id)
 *         - deleteAll()
 * @param <T>
 * 
 */
public class RoboManager<T extends RoboModel> {
    private static final String CREATE_ERROR = "Error while creating a model instance.";

    private final DatabaseManager mDatabaseManager;
    private final Context mContext;
    private final Class<T> mKlass;
    private final RoboModel mSampleModel;

    /**
     * @param context
     * @param klass
     */
    public static <TT extends RoboModel> RoboManager<TT> get(Context context, Class<TT> klass) {
        return new RoboManager<TT>(context, klass);
    }

    private RoboManager(Context context, Class<T> klass) {
        mContext = context;
        mKlass = klass;
        mDatabaseManager = new DatabaseManager(context);
        mSampleModel = create();
    }

    public List<T> all() {
        final long[] ids = getSelectedModelIds(null, null, null, null, null);
        return getRecords(ids);
    }

    public int count() {
        //TODO: Direct COUNT() query
        final long[] ids = getSelectedModelIds(null, null, null, null, null);
        return ids.length;
    }

    public T last() throws InstanceNotFoundException {
        final T record = create();
        final long id = getLastId();
        record.load(id);
        return record;
    }

    public void deleteAll() {
        /*
         * In case of invalid DB structure we try to fix it and re-run the delete
         */
        try {
            mDatabaseManager.deleteAllRecords(getDatabaseName(), getTableName());
        } catch (final SQLiteException e) {
            prepareTable(mDatabaseManager.openOrCreateDatabase(getDatabaseName()));
            mDatabaseManager.deleteAllRecords(getDatabaseName(), getTableName());
        }
    }

    public T create(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            final T result = mapper.readValue(json, mKlass);
            result.setContext(mContext);
            return result;
        } catch (Exception e) {
            throw new JsonException("Error while parsing JSON", e);
        }
    }

    public <C extends RoboModelCollection<T>> C createCollection(String json, Class<C> klass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            final C result = mapper.readValue(json, klass);
            result.setContext(mContext);
            return result;
        } catch (Exception e) {
            throw new JsonException("Error while parsing JSON", e);
        }
    }

    public T create() {
        try {
            return (T) createModelObject();
        } catch (final Exception e) {
            throw new RuntimeException(CREATE_ERROR, e);
        }
    }

    private T createModelObject() throws ClassNotFoundException, InstantiationException,
                    IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        T newModel = mKlass.newInstance();
        newModel.setContext(mContext);
        return newModel;
    }

    public void dropTable() {
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());
        mDatabaseManager.dropTable(getTableName(), db);
    }

    public T find(long id) throws InstanceNotFoundException {
        final T record = create();
        record.load(id);
        return record;
    }

    public String getDatabaseName() {
        return mDatabaseManager.getDatabaseName();
    }

    private long getLastId() throws InstanceNotFoundException {
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());

        final String columns[] = new String[] { BaseColumns._ID };
        Cursor query;
        /*
         * Try the query. If the Table doesn't exist, fix the DB and re-run the query.
         */
        try {
            query = db.query(getTableName(), columns, null, null, null, null, null);
        } catch (final SQLiteException e) {
            prepareTable(db);
            query = db.query(getTableName(), columns, null, null, null, null, null);
        }

        if (query.moveToLast()) {
            final int columnIndex = query.getColumnIndex(BaseColumns._ID);
            return query.getLong(columnIndex);
        } else {
            throw new InstanceNotFoundException("table " + getTableName() + " is empty");
        }
    }

    public long[] getSelectedModelIds(String selection, String[] selectionArgs, String groupBy,
                    String having, String orderBy) {
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());

        final String columns[] = new String[] { BaseColumns._ID };
        Cursor query;

        /*
         * Try the query. If the Table doesn't exist, fix the DB and re-run the query.
         */
        try {
            query = db.query(getTableName(), columns, selection, selectionArgs, groupBy,
                            having, orderBy);
        } catch (final SQLiteException e) {
            prepareTable(db);
            query = db.query(getTableName(), columns, selection, selectionArgs, groupBy,
                            having, orderBy);
        }

        final int columnIndex = query.getColumnIndex(BaseColumns._ID);
        final long result[] = new long[query.getCount()];
        for (query.moveToFirst(); !query.isAfterLast(); query.moveToNext()) {
            result[query.getPosition()] = query.getLong(columnIndex);
        }

        return result;
    }

    private void prepareTable(final SQLiteDatabase db) {
        T model = create();

        mDatabaseManager.createOrPopulateTable(getTableName(), model.getSavedFields(), db);
    }

    private String getTableName() {
        return mSampleModel.getTableName();
    }

    public List<T> where(String selection) {
        return where(selection, null, null, null, null);
    }

    public List<T> where(String selection, String[] selectionArgs) {
        return where(selection, selectionArgs, null, null, null);
    }

    public List<T> where(String selection, String[] selectionArgs, String groupBy, String having,
                    String orderBy) {
        final long[] ids = getSelectedModelIds(selection, selectionArgs, groupBy, having, orderBy);
        return getRecords(ids);
    }

    private List<T> getRecords(long[] ids) {
        final List<T> result = new ArrayList<T>(ids.length);
        for (final long id : ids) {
            try {
                result.add(find(id));
            } catch (final InstanceNotFoundException e) {
                Ln.w(e, "Record with id %d was deleted while being loaded", id);
            }
        }
        return result;
    }
}
