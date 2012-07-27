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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

public abstract class RoboModel {
    public static final long UNSAVED_MODEL_ID = -1;

    @Inject
    Gson mGson;
    @Inject
    SQLiteOpenHelper mDbHelper;

    protected long mId = UNSAVED_MODEL_ID;
    private final Class<? extends RoboModel> mClass = this.getClass();
    private final Context mContext;
    private final DatabaseManager mDatabaseManager;

    /**
     * 
     */
    public RoboModel(Context context) {
        mContext = context;
        mDatabaseManager = new DatabaseManager(context);
    }

    public void delete() {
        if (!isSaved()) {
            throw new IllegalStateException("No record in database to delete");
        }

        mDatabaseManager.deleteRecord(getDatabaseName(), getTableName(), mId);
    }

    public String getDatabaseName() {
        // TODO: from annotation
        return mContext.getPackageName();
    }

    private Field getFieldForColumn(String column) {
        final Field[] fields = mClass.getDeclaredFields();
        for (final Field field : fields) {
            if (field.getName().equalsIgnoreCase(column)) {
                return field;
            }
        }

        // Not found: throw exception
        final String msg = String.format("Class %s does not contain a field named %s",
                        mClass.getSimpleName(), column);
        throw new RuntimeException(msg);
    }

    public long getId() {
        return mId;
    }

    private List<Field> getSavedFields() { //TODO: cache results
        final List<Field> savedFields = new ArrayList<Field>();

        // Check if we have a blacklist or whitelist policy for this model
        final boolean whitelist = getClass().isAnnotationPresent(ExcludeByDefault.class);

        final Field[] declaredFields = getClass().getDeclaredFields();
        boolean saved;
        for (final Field field : declaredFields) {

            saved = false;
            saved = saved || field.isAnnotationPresent(Save.class);
            saved = saved || !whitelist && Modifier.isPublic(field.getModifiers());
            saved = saved && !field.isAnnotationPresent(Exclude.class);

            if (saved) {
                savedFields.add(field);
            }
        }

        return savedFields;
    }

    protected String getTableName() {
        return mClass.getSimpleName();
    }

    public boolean isSaved() {
        return mId != UNSAVED_MODEL_ID;
    }

    public void load(long id) throws InstanceNotFoundException {
        if (id < 0) {
            throw new IllegalArgumentException("RoboModel id can not be negative.");
        }

        mId = id;
        reload();
    }

    public void reload() throws InstanceNotFoundException {
        if (!isSaved()) {
            throw new IllegalStateException(
                            "This model instance does not have a corresponding entry in the database");
        }

        // Retrieve current entry in the database
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final Cursor query = db.query(getTableName(), null, where(), null, null, null, null);
        if (query.moveToFirst()) {
            setFieldsWithQueryResult(query);
            query.close();
            mDbHelper.close();
        } else {
            final String msg = String.format("No entry in database with id %d for model %s",
                            getId(),
                            getTableName());
            throw new InstanceNotFoundException(msg);
        }
    }

    public void save() {
        final List<Field> savedFields = getSavedFields();
        final TypedContentValues cv = new TypedContentValues(savedFields.size());
        for (final Field field : savedFields) {
            saveField(field, cv);
        }
        // TODO: check no fields to save

        mId = mDatabaseManager.saveModel(getDatabaseName(), getTableName(), cv, mId);
    }

    private void saveField(Field field, TypedContentValues cv) {
        final Class<?> type = field.getType();
        final boolean wasAccessible = field.isAccessible();
        field.setAccessible(true);

        try {
            if (type == String.class) {
                cv.put(field.getName(), (String) field.get(this));
            } else if (type == Boolean.TYPE) {
                cv.put(field.getName(), field.getBoolean(this));
            } else if (type == Byte.TYPE) {
                cv.put(field.getName(), field.getByte(this));
            } else if (type == Double.TYPE) {
                cv.put(field.getName(), field.getDouble(this));
            } else if (type == Float.TYPE) {
                cv.put(field.getName(), field.getFloat(this));
            } else if (type == Integer.TYPE) {
                cv.put(field.getName(), field.getInt(this));
            } else if (type == Long.TYPE) {
                cv.put(field.getName(), field.getLong(this));
            } else if (type == Short.TYPE) {
                cv.put(field.getName(), field.getShort(this));
            } else if (type.isEnum()) {
                final Object value = field.get(this);
                if (value != null) {
                    final Method method = type.getMethod("name");
                    final String str = (String) method.invoke(value);
                    cv.put(field.getName(), str);
                }
            } else {
                // Try to JSONify it (db column must be of type text)
                final String json = mGson.toJson(field.get(this));
                cv.put(field.getName(), json);
            }
        } catch (final IllegalAccessException e) {
            final String msg = String.format("Field %s is not accessible", type, field.getName());
            throw new IllegalArgumentException(msg);
        } catch (final NoSuchMethodException e) {
            // Should not happen
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            // Should not happen
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(wasAccessible);
        }
    }

    private void setFieldsWithQueryResult(Cursor query) {
        // Iterate over the columns and auto-assign values on corresponding fields
        final String[] columns = query.getColumnNames();
        for (final String column : columns) {
            // Skip id column
            if (column.equals(_ID)) {
                continue;
            }

            final Field field = getFieldForColumn(column);
            setFieldValue(field, query);
        }
    }

    private void setFieldValue(Field field, Cursor query) {
        final Class<?> type = field.getType();
        final boolean wasAccessible = field.isAccessible();
        final int columnIndex = query.getColumnIndex(field.getName());
        field.setAccessible(true);

        try {
            if (type == String.class) {
                field.set(this, query.getString(columnIndex));
            } else if (type == Boolean.TYPE) {
                final boolean value = query.getInt(columnIndex) == 1 ? true : false;
                field.setBoolean(this, value);
            } else if (type == Byte.TYPE) {
                field.setByte(this, (byte) query.getShort(columnIndex));
            } else if (type == Double.TYPE) {
                field.setDouble(this, query.getDouble(columnIndex));
            } else if (type == Float.TYPE) {
                field.setFloat(this, query.getFloat(columnIndex));
            } else if (type == Integer.TYPE) {
                field.setInt(this, query.getInt(columnIndex));
            } else if (type == Long.TYPE) {
                field.setLong(this, query.getLong(columnIndex));
            } else if (type == Short.TYPE) {
                field.setShort(this, query.getShort(columnIndex));
            } else if (type.isEnum()) {
                final String string = query.getString(columnIndex);
                if (string != null && string.length() > 0) {
                    final Object[] constants = type.getEnumConstants();
                    final Method method = type.getMethod("valueOf", Class.class, String.class);
                    final Object value = method.invoke(constants[0], type, string);
                    field.set(this, value);
                }
            } else {
                // Try to de-json it (db column must be of type text)
                try {
                    final Object value = mGson.fromJson(query.getString(columnIndex),
                                    field.getType());
                    field.set(this, value);
                } catch (final JsonSyntaxException e) {
                    final String msg = String.format("Type %s is not supported for field %s", type,
                                    field.getName());
                    throw new IllegalArgumentException(msg);
                }
            }
        } catch (final IllegalAccessException e) {
            final String msg = String.format("Field %s is not accessible", type, field.getName());
            throw new IllegalArgumentException(msg);
        } catch (final NoSuchMethodException e) {
            // Should not happen
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            // Should not happen
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(wasAccessible);
        }
    }

    //    @Override
    //    public String toString() {
    //        SQLiteDatabase db = mDbHelper.getReadableDatabase();
    //        String[] columns = getColumns(db);
    //        mDbHelper.close();
    //
    //        StringBuilder b = new StringBuilder();
    //        b.append(getTableName() + " {");
    //        for (String string : columns) {
    //            if (string.equals(BaseColumns._ID)) {
    //                b.append(string + ": " + getId() + ", ");
    //            } else {
    //                Field f = getFieldForColumn(string);
    //                boolean accessible = f.isAccessible();
    //                f.setAccessible(true);
    //                try {
    //                    b.append(string + ": " + f.get(this) + ", ");
    //                } catch (IllegalAccessException e) {
    //                    b.append(string + ": (INACCESSIBLE), ");
    //                }
    //                f.setAccessible(accessible);
    //            }
    //        }
    //        b.append("}");
    //
    //        return b.toString();
    //    }

    private String where() {
        return _ID + " = " + mId;
    }
}
