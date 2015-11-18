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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;

/**
 * This class is a wrapper of {@link ContentValues} that also stores the type of the field.
 */
public final class TypedContentValues {
    enum ElementType {
        BOOLEAN, INTEGER, REAL, TEXT, BLOB
    }

    public static final String TAG = "TypedContentValues2";

    /** Holds the types values */
    private HashMap<String, ElementType> mTypes;
    private final ContentValues mValues;

    /**
     * Creates an empty set of values using the default initial size
     */
    public TypedContentValues() {
        mValues = new ContentValues();
        mTypes = new HashMap<String, ElementType>();
    }

    /**
     * Creates an empty set of values using the given initial size
     * 
     * @param size
     *            the initial size of the set of values
     */
    public TypedContentValues(int size) {
        mValues = new ContentValues(size);
        mTypes = new HashMap<String, ElementType>(size);
    }

    /**
     * Creates a set of values copied from the given set
     * 
     * @param from
     *            the values to copy
     */
    public TypedContentValues(TypedContentValues from) {
        mTypes = new HashMap<String, ElementType>(from.mTypes);
        mValues = new ContentValues(from.mValues);
    }

    /**
     * Removes all values.
     */
    public void clear() {
        mValues.clear();
        mTypes.clear();
    }

    /**
     * Returns true if this object has the named value.
     * 
     * @param key
     *            the value to check for
     * @return {@code true} if the value is present, {@code false} otherwise
     */
    public boolean containsKey(String key) {
        return mValues.containsKey(key);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TypedContentValues)) {
            return false;
        }
        boolean equals = mValues.equals(((TypedContentValues) object).mValues);
        return equals && mTypes.equals(((TypedContentValues) object).mTypes);
    }

    /**
     * Gets a value. Valid value types are {@link String}, {@link Boolean}, and {@link Number} implementations.
     * 
     * @param key
     *            the value to get
     * @return the data for the value
     */
    public Object get(String key) {
        return mValues.get(key);
    }

    /**
     * Gets a value and converts it to a Boolean.
     * 
     * @param key
     *            the value to get
     * @return the Boolean value, or null if the value is missing or cannot be converted
     */
    public Boolean getAsBoolean(String key) {
        return mValues.getAsBoolean(key);
    }

    /**
     * Gets a value and converts it to a Byte.
     * 
     * @param key
     *            the value to get
     * @return the Byte value, or null if the value is missing or cannot be converted
     */
    public Byte getAsByte(String key) {
        return mValues.getAsByte(key);
    }

    /**
     * Gets a value that is a byte array. Note that this method will not convert
     * any other types to byte arrays.
     * 
     * @param key
     *            the value to get
     * @return the byte[] value, or null is the value is missing or not a byte[]
     */
    public byte[] getAsByteArray(String key) {
        return mValues.getAsByteArray(key);
    }

    /**
     * Gets a value and converts it to a Double.
     * 
     * @param key
     *            the value to get
     * @return the Double value, or null if the value is missing or cannot be converted
     */
    public Double getAsDouble(String key) {
        return mValues.getAsDouble(key);
    }

    /**
     * Gets a value and converts it to a Float.
     * 
     * @param key
     *            the value to get
     * @return the Float value, or null if the value is missing or cannot be converted
     */
    public Float getAsFloat(String key) {
        return mValues.getAsFloat(key);
    }

    /**
     * Gets a value and converts it to an Integer.
     * 
     * @param key
     *            the value to get
     * @return the Integer value, or null if the value is missing or cannot be converted
     */
    public Integer getAsInteger(String key) {
        return mValues.getAsInteger(key);
    }

    /**
     * Gets a value and converts it to a Long.
     * 
     * @param key
     *            the value to get
     * @return the Long value, or null if the value is missing or cannot be converted
     */
    public Long getAsLong(String key) {
        return mValues.getAsLong(key);
    }

    /**
     * Gets a value and converts it to a Short.
     * 
     * @param key
     *            the value to get
     * @return the Short value, or null if the value is missing or cannot be converted
     */
    public Short getAsShort(String key) {
        return mValues.getAsShort(key);
    }

    /**
     * Gets a value and converts it to a String.
     * 
     * @param key
     *            the value to get
     * @return the String for the value
     */
    public String getAsString(String key) {
        return mValues.getAsString(key);
    }

    /**
     * Get the type of a value.
     * 
     * @param key
     *            the value to get
     * @return the type of the value, or null is the value is missing
     */
    public ElementType getType(String key) {
        return mTypes.get(key);
    }

    @Override
    public int hashCode() {
        int code = 17;
        code = 31 * code + mValues.hashCode();
        code = 31 * code + mTypes.hashCode();
        return code;
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, Boolean value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.BOOLEAN);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, Byte value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.INTEGER);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, byte[] value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.BLOB);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, Double value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.REAL);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, Float value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.REAL);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, Integer value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.INTEGER);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, Long value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.INTEGER);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, Short value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.INTEGER);
    }

    /**
     * Adds a value to the set.
     * 
     * @param key
     *            the name of the value to put
     * @param value
     *            the data for the value to put
     */
    public void put(String key, String value) {
        mValues.put(key, value);
        mTypes.put(key, ElementType.TEXT);
    }

    /**
     * Adds all values from the passed in ContentValues.
     * 
     * @param other
     *            the ContentValues from which to copy
     */
    public void putAll(TypedContentValues other) {
        mValues.putAll(other.mValues);
        mTypes.putAll(other.mTypes);
    }

    /**
     * Adds a null value to the set.
     * 
     * @param key
     *            the name of the value to make null
     */
    public void putNull(String key) {
        mValues.putNull(key);
        mTypes.put(key, null);
    }

    /**
     * Remove a single value.
     * 
     * @param key
     *            the name of the value to remove
     */
    public void remove(String key) {
        mValues.remove(key);
        mTypes.remove(key);
    }

    /**
     * Returns the number of values.
     * 
     * @return the number of values
     */
    public int size() {
        return mValues.size();
    }
    
    public Set<String> keySet() {
        return mTypes.keySet();
    }

    /**
     * Create a {@link ContentValues} instance with the contents of this instance.
     */

    public ContentValues toContentValues() {
        return new ContentValues(mValues);
    }

    /**
     * Returns a set of all of the keys and values
     * 
     * @return a set of all of the keys and values
     */
    public Set<Map.Entry<String, Object>> valueSet() {
        return mValues.valueSet();
    }
}
