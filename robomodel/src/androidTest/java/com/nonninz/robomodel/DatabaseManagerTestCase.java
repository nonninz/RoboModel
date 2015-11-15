package com.nonninz.robomodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

public class DatabaseManagerTestCase extends AndroidTestCase {

    private final String TEST_DB_NAME = "DatabaseManagerTestCaseDB";
    private DatabaseManager mDatabaseManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getContext().deleteDatabase(TEST_DB_NAME);
        mDatabaseManager = new DatabaseManager(getContext());
    }

    public void testWhereConstruct() {
        assertEquals("_id = 5", DatabaseManager.where(5));
    }

    public void testCreateOrPopulateTable() throws NoSuchFieldException {
        List<Field> fields = new ArrayList<Field>();
        TestModel model = new TestModel();
        model.setContext(mContext);

        fields.add(model.getClass().getDeclaredField("stringField"));
        fields.add(model.getClass().getDeclaredField("booleanField"));
        fields.add(model.getClass().getDeclaredField("intField"));

        SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(TEST_DB_NAME);
        mDatabaseManager.createOrPopulateTable("Test", fields, db);

        // There should be a table "Test", created with correct SQL 
        String sql = null;
        Cursor tablesCursor = db.rawQuery("SELECT * FROM SQLITE_MASTER", null);
        while (tablesCursor.moveToNext()) {
            String tableName = tablesCursor.getString(tablesCursor.getColumnIndex("name"));
            if (tableName.equals("Test")) {
                sql = tablesCursor.getString(tablesCursor.getColumnIndex("sql"));
                break;
            }
        }
        assertEquals("CREATE TABLE Test (" +
                        "stringField TEXT, " +
                        "booleanField BOOLEAN, " +
                        "intField INTEGER, " +
                        "_id integer primary key autoincrement)", sql);

        // Table Columns should be added after we alter the fields collection
        fields.add(model.getClass().getDeclaredField("doubleField"));
        mDatabaseManager.createOrPopulateTable("Test", fields, db);

        sql = null;
        tablesCursor = db.rawQuery("SELECT * FROM SQLITE_MASTER", null);
        while (tablesCursor.moveToNext()) {
            String tableName = tablesCursor.getString(tablesCursor.getColumnIndex("name"));
            if (tableName.equals("Test")) {
                sql = tablesCursor.getString(tablesCursor.getColumnIndex("sql"));
                break;
            }
        }
        assertEquals("CREATE TABLE Test (" +
                        "stringField TEXT, " +
                        "booleanField BOOLEAN, " +
                        "intField INTEGER, " +
                        "_id integer primary key autoincrement, " +
                        "doubleField REAL)", sql);
    }

    public void testDeleteAll() throws SecurityException, NoSuchFieldException {
        SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(TEST_DB_NAME);
        db.execSQL("CREATE TABLE Test (stringField TEXT, _id integer primary key autoincrement)");
        db.execSQL("INSERT INTO Test (stringField) VALUES ('Test1')");
        db.execSQL("INSERT INTO Test (stringField) VALUES ('Test2')");
        db.execSQL("INSERT INTO Test (stringField) VALUES ('Test2')");

        Cursor beforeCursor = db.rawQuery("SELECT * FROM Test", null);
        assertEquals(2, beforeCursor.getColumnCount());

        mDatabaseManager.deleteAllRecords(TEST_DB_NAME, "Test");

        Cursor afterCursor = db.rawQuery("SELECT * FROM Test", null);
        assertEquals(0, afterCursor.getCount());
    }

    public void testDeleteRecord() {
        SQLiteDatabase db = mDatabaseManager.openOrCreateDatabase(TEST_DB_NAME);
        db.execSQL("CREATE TABLE Test (stringField TEXT, _id integer primary key autoincrement)");
        db.execSQL("INSERT INTO Test (stringField) VALUES ('Test1')");
        db.execSQL("INSERT INTO Test (stringField) VALUES ('Test2')");

        mDatabaseManager.deleteRecord(TEST_DB_NAME, "Test", 1);
        Cursor afterCursor = db.rawQuery("SELECT * FROM Test", null);
        assertEquals(1, afterCursor.getCount());
    }

}
