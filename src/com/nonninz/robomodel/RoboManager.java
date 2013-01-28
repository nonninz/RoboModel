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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.nonninz.robomodel.exceptions.InstanceNotFoundException;

/**
 * @author Francesco Donadon <francesco.donadon@gmail.com>
 * 
 * RoboManager:
 * 1. Provides an interface to conveniently query and operate the DB for RoboModel instances with:
 *  - all()
 *  - first() - TODO
 *  - last()
 *  - find(id)
 *  - deleteAll()
 *  
 */
public class RoboManager<T extends RoboModel> {
    private static final String CREATE_ERROR = "Error while creating a model instance.";

    private final DatabaseManager mDatabaseManager;
    private final Context mContext;
    private final Class<T> mKlass;
    private final RoboModel mSampleModel;

    /**
     * @param context2
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
        Gson gson = new GsonBuilder().registerTypeAdapter(mKlass, new RoboInstanceCreator()).setPrettyPrinting().create();
        return gson.fromJson(json, mKlass);
    }
    
    private class RoboInstanceCreator implements InstanceCreator<T> {

        @Override
        public T createInstance(Type t) {
          return create();
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
        final Constructor<T> constructor = mKlass.getDeclaredConstructor(Context.class);
        final boolean accessible = constructor.isAccessible();
        constructor.setAccessible(true);
        T newModel = constructor.newInstance(mContext);
        constructor.setAccessible(accessible);
        return newModel;
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
          throw new InstanceNotFoundException("table " + getTableName() +" is empty");
        }
    }
    
    private long[] getSelectedModelIds(String selection, String[] selectionArgs, String groupBy,
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
        try {
            final List<T> result = new ArrayList<T>(ids.length);
            for (final long id : ids) {
                result.add(find(id));
            }
            return result;
        } catch (final InstanceNotFoundException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
    }
}
