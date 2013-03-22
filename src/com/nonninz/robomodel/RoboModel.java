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
import static com.nonninz.robomodel.DatabaseManager.where;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nonninz.robomodel.annotations.Exclude;
import com.nonninz.robomodel.annotations.Save;
import com.nonninz.robomodel.exceptions.InstanceNotFoundException;
import com.nonninz.robomodel.exceptions.JsonException;
import com.nonninz.robomodel.util.Ln;

/**
 * RoboModel:
 * 1. Provides ORM style methods to operate with Model instances
 * - save()
 * - delete()
 * - reload()
 * 
 */
@JsonAutoDetect(creatorVisibility = Visibility.NONE, fieldVisibility = Visibility.PUBLIC_ONLY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public abstract class RoboModel {
    public static final long UNSAVED_MODEL_ID = -1;

    private String mTableName;

    protected long mId = UNSAVED_MODEL_ID;

    private final Class<? extends RoboModel> mClass = this.getClass();
    private Context mContext;
    private DatabaseManager mDatabaseManager;
    private final ObjectMapper mMapper = new ObjectMapper();

    void setContext(Context context) {
        mContext = context;
        mDatabaseManager = new DatabaseManager(context);
    }

    protected Context getContext() {
        return mContext;
    }

    public void delete() {
        if (!isSaved()) {
            throw new IllegalStateException("No record in database to delete");
        }

        mDatabaseManager.deleteRecord(getDatabaseName(), getTableName(), mId);
    }

    public String getDatabaseName() {
        return mDatabaseManager.getDatabaseName();
    }

    String getTableName() {
        if (mTableName == null) {
            mTableName = mClass.getSimpleName();
        }
        return mTableName;
    }

    public long getId() {
        return mId;
    }

    List<Field> getSavedFields() { //TODO: cache results
        final List<Field> savedFields = new ArrayList<Field>();

        final Field[] declaredFields = getClass().getDeclaredFields();
        boolean saved;
        for (final Field field : declaredFields) {

            saved = false;
            saved = saved || field.isAnnotationPresent(Save.class); // If @Save is present, save it
            saved = saved || Modifier.isPublic(field.getModifiers()); // If it is public, save it
            saved = saved && !field.isAnnotationPresent(Exclude.class); // If @Exclude, don't save it

            if (saved) {
                savedFields.add(field);
            }
        }

        return savedFields;
    }

    public boolean isSaved() {
        return mId != UNSAVED_MODEL_ID;
    }

    void load(long id) throws InstanceNotFoundException {
        if (id < 0) {
            throw new IllegalArgumentException("RoboModel id can not be negative.");
        }

        mId = id;
        reload();
    }

    private void loadField(Field field, Cursor query) {
        final Class<?> type = field.getType();
        final boolean wasAccessible = field.isAccessible();
        final int columnIndex = query.getColumnIndex(field.getName());
        field.setAccessible(true);

        /*
         * TODO: There is the potential of a problem here:
         * What happens if the developer changes the type of a field between releases?
         * 
         * If he saves first, then the column type will be changed (In the future).
         * If he loads first, we don't know if an Exception will be thrown if the
         * types are incompatible, because it's undocumented in the Cursor documentation.
         */

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
                    final Object value = mMapper.readValue(query.getString(columnIndex),
                                    field.getType());
                    field.set(this, value);
                } catch (final Exception e) {
                    final String msg = String.format("Type %s is not supported for field %s", type,
                                    field.getName());
                    Ln.w(e, msg);
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

    @SuppressLint("DefaultLocale")
    public void reload() throws InstanceNotFoundException {
        if (!isSaved()) {
            throw new IllegalStateException("This instance has not yet been saved.");
        }

        // Retrieve current entry in the database
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());
        Cursor query;

        /*
         * Try to query the table. If the Table doesn't exist, fix the DB and re-run the query.
         */
        try {
            query = db.query(getTableName(), null, where(mId), null, null, null, null);
        } catch (final SQLiteException e) {
            mDatabaseManager.createOrPopulateTable(mTableName, getSavedFields(), db);
            query = db.query(getTableName(), null, where(mId), null, null, null, null);
        }

        if (query.moveToFirst()) {
            setFieldsWithQueryResult(query);
            query.close();
            db.close();
        } else {
            query.close();
            db.close();
            final String msg = String.format("No entry in database with id %d for model %s",
                            getId(),
                            getTableName());
            throw new InstanceNotFoundException(msg);
        }
    }

    public void save() {
        final SQLiteDatabase database = mDatabaseManager.openOrCreateDatabase(getDatabaseName());

        List<Field> fields = getSavedFields();
        final TypedContentValues cv = new TypedContentValues(fields.size());
        for (final Field field : fields) {
            saveField(field, cv);
        }

        // First try to save it. Then deal with errors (like table/field not existing);
        try {
            mId = mDatabaseManager.insertOrUpdate(getTableName(), cv, mId, database);
        } catch (final SQLiteException ex) {
            mDatabaseManager.createOrPopulateTable(getTableName(), fields, database);
            mId = mDatabaseManager.insertOrUpdate(getTableName(), cv, mId, database);
        } finally {
            database.close();
        }
    }

    void saveField(Field field, TypedContentValues cv) {
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
            }
            else {
                // Try to JSONify it (db column must be of type text)
                final String json = mMapper.writeValueAsString(field.get(this));
                cv.put(field.getName(), json);
            }
        } catch (final IllegalAccessException e) {
            final String msg = String.format("Field %s is not accessible", type, field.getName());
            throw new IllegalArgumentException(msg);
        } catch (final JsonProcessingException e) {
            Ln.w(e, "Error while dumping %s of type %s to Json", field.getName(), type);
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

            final List<Field> fields = getSavedFields();
            for (final Field field : fields) {
                loadField(field, query);
            }
        }
    }

    @Override
    public String toString() {
        final List<Field> fields = getSavedFields();

        final StringBuilder b = new StringBuilder();
        b.append(getTableName() + " {id: " + getId() + ", ");
        String fieldName;
        for (final Field f : fields) {
            fieldName = f.getName();
            final boolean accessible = f.isAccessible();
            f.setAccessible(true);
            try {
                b.append(fieldName + ": " + f.get(this) + ", ");
            } catch (final IllegalAccessException e) {
                b.append(fieldName + ": (INACCESSIBLE), ");
            }
            f.setAccessible(accessible);
        }
        b.append("}");

        return b.toString();
    }

    public String toJson() {
        try {
            return mMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }
}