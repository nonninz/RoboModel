package com.nonninz.robomodel;

import android.test.AndroidTestCase;

/**
 * Created by fra on 16/11/15.
 */
public class RoboCollectionTestCase extends AndroidTestCase {
    private static final String JSON = "" +
            "{\"models\" : [" +
            "   {" +
            "       \"stringField\" : \"First\", " +
            "       \"booleanField\" : true," +
            "       \"byteField\" : 42," +
            "       \"shortField\" : 4242," +
            "       \"intField\" : 424242," +
            "       \"longField\" : 42424242," +
            "       \"floatField\" : 42.4242," +
            "       \"doubleField\" : 42.42424242," +
            "       \"enumOne\" : \"LIFE\"," +
            "       \"enumTwo\" : \"UNIVERSE\"," +
            "       \"enumThree\" : \"EVERYTHING\"" +
            "}, {" +
            "       \"stringField\" : \"Second\", " +
            "       \"booleanField\" : false," +
            "       \"byteField\" : 43," +
            "       \"shortField\" : 4343," +
            "       \"intField\" : 434343," +
            "       \"longField\" : 43434343," +
            "       \"floatField\" : 43.4343," +
            "       \"doubleField\" : 43.43434343," +
            "       \"enumOne\" : \"EVERYTHING\"," +
            "       \"enumTwo\" : \"LIFE\"," +
            "       \"enumThree\" : \"UNIVERSE\"" +
            "}]}";

    private RoboManager<TestModel> mManager;

    @Override
    protected void setUp() throws Exception {
        mManager = RoboManager.get(getContext(), TestModel.class);
    }

    @Override
    protected void tearDown() throws Exception {
        mManager.closeDatabase();
        getContext().deleteDatabase(mManager.getDatabaseName());
    }

    public void testCreateArrayCollection() {
        final TestModel.TestModelArrayCollection collection = mManager.createCollection(JSON, TestModel.TestModelArrayCollection.class);

        assertEquals(2, collection.models.length);

        assertEquals("First", collection.models[0].stringField);
        assertEquals("Second", collection.models[1].stringField);
    }

    public void testSaveArrayCollection() {
        final TestModel.TestModelArrayCollection collection = mManager.createCollection(JSON, TestModel.TestModelArrayCollection.class);
        collection.save();

        assertEquals(2, mManager.count());
    }

    public void testCreateListCollection() {
        final TestModel.TestModelListCollection collection = mManager.createCollection(JSON, TestModel.TestModelListCollection.class);

        assertEquals(2, collection.models.size());

        assertEquals("First", collection.models.get(0).stringField);
        assertEquals("Second", collection.models.get(1).stringField);
    }

    public void testSaveListCollection() {
        final TestModel.TestModelListCollection collection = mManager.createCollection(JSON, TestModel.TestModelListCollection.class);
        collection.save();

        assertEquals(2, mManager.count());
    }
}
