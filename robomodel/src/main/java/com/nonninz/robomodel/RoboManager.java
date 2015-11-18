/**
 * Copyright 2012 Francesco Donadon
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nonninz.robomodel;

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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private RoboManager(Context context, Class<T> klass) {
        mContext = context;
        mKlass = klass;
        mDatabaseManager = new DatabaseManager(context);
        mSampleModel = create();
    }

    /**
     * Get an instance of Robomanager for the given class.
     *
     * @param context a context instance.
     * @param klass the Robomodel superclass associated to the manager instance.
     */
    public static <TT extends RoboModel> RoboManager<TT> get(Context context, Class<TT> klass) {
        return new RoboManager<TT>(context, klass);
    }

    /**
     * Retrieves all the records in the databases.
     *
     * @return a List containing all the records in the database.
     */
    public List<T> all() {
        final long[] ids = getSelectedModelIds(null, null, null, null, null);
        return getRecords(ids);
    }

    /**
     * Removes all the entries from the database.
     */
    public void clear() {
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

    /**
     * Closes the database instance.
     *
     * This should not be needed.
     */
    public void closeDatabase() {
        mDatabaseManager.closeDatabase();
    }

    /**
     * Returns a count of all the records in the database.
     *
     * @return the number of records in the database.
     */
    public int count() {
        //TODO: Direct COUNT() query
        final long[] ids = getSelectedModelIds(null, null, null, null, null);
        return ids.length;
    }

    /**
     * Creates a new RoboModel instance of the associated type.
     *
     * @return an empty instance of a RoboModel object.
     */
    public T create() {
        try {
            return (T) createModelObject();
        } catch (final Exception e) {
            throw new RuntimeException(CREATE_ERROR, e);
        }
    }

    /**
     * Creates a new RoboModel instance initialized with a JSON object.
     *
     * This new instance will not be saved to database.
     *
     * @param json a String representing a JSON object.
     * @return a new instance of T.
     */
    public T create(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            final T result = mapper.readValue(json, mKlass);
            result.setContext(mContext);
            return result;
        } catch (Exception e) {
            // FIXME: catch Exception? Really, past me?
            throw new JsonException("Error while parsing JSON", e);
        }
    }

    /**
     * Utility function to create a collection of models from json.
     *
     * This method, and the RoboModelCollection class, have been written for the use case where
     * a REST endpoint will return a collection of elements in the format:
     *
     * {
     *     "elements": [{...}, ..., {...}]
     * }
     *
     * In this use case one could get and save all elements with a single liner:
     *
     * manager.createCollection(response, ElementCollection.class).save();
     *
     * Assuming that manager is an instance of RoboManager<Element> and
     * ElementCollection is defined as:
     *
     * class ElementCollection extends RoboModelCollection<Element> {
     *     public List<Element> elements;
     * }
     *
     * @param json a string representing the collection in JSON
     * @param klass the class of the RoboModelCollection
     * @param <C> a superclass of RoboModelCollection as defined by the user
     * @return The collection of models decoded from the provided json.
     */
    public <C extends RoboModelCollection<T>> C createCollection(String json, Class<C> klass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            final C result = mapper.readValue(json, klass);
            result.setContext(mContext);
            return result;
        } catch (Exception e) {
            // FIXME: Catch Exception??? Really, past me? /o\
            throw new JsonException("Error while parsing JSON", e);
        }
    }

    /**
     * Drops the table containing all the records for type T.
     */
    public void dropTable() {
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());
        mDatabaseManager.dropTable(getTableName(), db);
    }

    /**
     * Returns the instance with the data for the record with the provided id.
     *
     * @param id the unique id pertaining to a record as returned by RoboModel#getId()
     * @return the instance with the data for the record with the provided id.
     * @throws InstanceNotFoundException if there is no record with such id.
     */
    public T find(long id) throws InstanceNotFoundException {
        final T record = create();
        record.load(id);
        return record;
    }

    /**
     * Finds a record with the specified unique id, different than the id provided by default.
     *
     * This is useful if the model has already another unique id assigned, for example by the REST
     * api this data is coming from, and we need to query for that id.
     *
     * Note that as this is implemented at the moment there is no requisite for the key to be
     * unique at all. All this method does is to issue a where() for that column and retrieve the
     * first record it finds.
     *
     * @param columnName the name of the column with the unique key.
     * @param key the unique key of the record
     * @return An instance of RoboModel containing the data of the specified record.
     * @throws InstanceNotFoundException if no instance with that key exists.
     */
    public T findByUniqueKey(String columnName, long key) throws InstanceNotFoundException {
        final List<T> found = where(String.format(Locale.US, "%s = %d", columnName, key));
        if (found.size() > 0) {
            return found.get(0);
        } else {
            final String msg = String.format("No record for table %s with column %s = %d",
                    mSampleModel.getTableName(), columnName, key);
            throw new InstanceNotFoundException(msg);
        }
    }

    /**
     * Returns the name of the database where the persisted data of this model is stored.
     * @return the name of the database where the persisted data of this model is stored.
     */
    public String getDatabaseName() {
        return mDatabaseManager.getDatabaseName();
    }

    /**
     * Returns the last inserted record (in theory)
     *
     * In practice since no ordering is implied this method will return whatever SQLite decides
     * to be the last record. Which may or may not be the last INSERTed one. Or maybe not.
     * who knows.
     *
     * What was I even thinking?
     *
     * @return the last record in the database according to the planet alignment.
     * @throws InstanceNotFoundException if there is no record in the database.
     */
    public T last() throws InstanceNotFoundException {
        final T record = create();
        final long id = getLastId();
        record.load(id);
        return record;
    }

    public T loadRecord(int position) throws InstanceNotFoundException {
        final T model = create();
        model.loadRecord(position);
        return model;
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

    private T createModelObject() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        T newModel = mKlass.newInstance();
        newModel.setContext(mContext);
        return newModel;
    }

    private long getLastId() throws InstanceNotFoundException {
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());

        final String columns[] = new String[]{BaseColumns._ID};
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

    private long[] getSelectedModelIds(String selection, String[] selectionArgs, String groupBy,
                                       String having, String orderBy) {
        final SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(getDatabaseName());

        final String columns[] = new String[]{BaseColumns._ID};
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

    private String getTableName() {
        return mSampleModel.getTableName();
    }

    private void prepareTable(final SQLiteDatabase db) {
        T model = create();

        mDatabaseManager.createOrPopulateTable(getTableName(), model.getSavedFields(), db);
    }
}
