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
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * @author Francesco Donadon <francesco.donadon@gmail.com>
 * 
 */
public class RoboManager<T extends RoboModel> {
    private static final String CREATE_ERROR = "Error while creating a model instance.";

    private static String sDatabaseName;
    private static String sTableName;

    private final DatabaseManager mDatabaseManager;
    private final Context mContext;

    public RoboManager(Context context) {
        mContext = context;
        mDatabaseManager = new DatabaseManager(context);
    }

    public List<T> all() {
        final long[] ids = getSelectedModelIds(null, null, null, null, null);
        return getRecords(ids);
    }

    public void clear() {
        mDatabaseManager.deleteAllRecords(getDatabaseName(), getTableName());
    }

    @SuppressWarnings("unchecked")
    public T create() {
        try {
            return (T) createModelObject();
        } catch (final Exception e) {
            throw new RuntimeException(CREATE_ERROR, e);
        }
    }

    private Object createModelObject() throws ClassNotFoundException, InstantiationException,
                    IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?> modelType = getModelType();
        final Object newInstance = modelType.getConstructor(Context.class).newInstance(mContext);
        return newInstance;
    }

    public T find(long id) throws InstanceNotFoundException {
        final T record = create();
        record.load(id);
        return record;
    }

    private String getDatabaseName() {
        if (sDatabaseName == null) {
            readModelParameters();
        }

        return sDatabaseName;
    }

    private Class<?> getModelType() throws ClassNotFoundException {
        final TypeVariable<?>[] typeParameters = this.getClass().getTypeParameters();
        final String typeName = typeParameters[0].getName();
        return ClassLoader.getSystemClassLoader().loadClass(typeName);
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

    private long[] getSelectedModelIds(String selection, String[] selectionArgs, String groupBy,
                    String having, String orderBy) {
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());
        final String columns[] = new String[] { BaseColumns._ID };
        final Cursor query = db.query(getTableName(), columns, selection, selectionArgs, groupBy,
                        having, orderBy);
        final int columnIndex = query.getColumnIndex(BaseColumns._ID);
        final long result[] = new long[query.getCount()];
        for (query.moveToFirst(); !query.isAfterLast(); query.moveToNext()) {
            result[query.getPosition()] = query.getLong(columnIndex);
        }
        return result;
    }

    private String getTableName() {
        if (sTableName == null) {
            readModelParameters();
        }

        return sTableName;
    }

    private void readModelParameters() {
        try {
            final RoboModel model = (RoboModel) createModelObject();
            sDatabaseName = model.getDatabaseName();
            sTableName = model.getTableName();
        } catch (final Exception e) {
            throw new RuntimeException("Error: getDatabaseName()", e);
        }
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
}
