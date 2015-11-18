package com.nonninz.robomodel;

import android.content.Context;
import android.test.AndroidTestCase;

import com.nonninz.robomodel.TestModel.Answer;

public class ModelReadTestCase extends AndroidTestCase {
    private TestModel mModel;
    private RoboManager<TestModel> mManager;

    @Override
    protected void setUp() throws Exception {
        final Context context = getContext();
        mManager = RoboManager.get(context, TestModel.class);

        final RoboModel model = mManager.create();
        model.save();
        mModel = mManager.find(model.getId());
    }

    @Override
    protected void tearDown() throws Exception {
        mManager.closeDatabase();
        getContext().deleteDatabase(mManager.getDatabaseName());
    }

    public void testBooleanSavedCorrectly() {
        assertEquals(true, mModel.booleanField);
    }

    public void testByteSavedCorrectly() {
        assertEquals(42, mModel.byteField);
    }

    public void testDoubleSavedCorrectly() {
        assertEquals(42.4242, mModel.doubleField);
    }

    public void testEnumSavedCorrectly() {
        assertEquals(Answer.LIFE, mModel.enumOne);
        assertEquals(Answer.UNIVERSE, mModel.enumTwo);
        assertEquals(Answer.EVERYTHING, mModel.enumThree);
    }

    public void testFloatSavedCorrectly() {
        assertEquals(42.42f, mModel.floatField);
    }

    public void testIntSavedCorrectly() {
        assertEquals(424242, mModel.intField);
    }

    public void testLongSavedCorrectly() {
        assertEquals(42424242, mModel.longField);
    }

    public void testShortSavedCorrectly() {
        assertEquals(4242, mModel.shortField);
    }

    public void testStringSavedCorrectly() {
        assertEquals("BTW thanks for all the fish!", mModel.stringField);
    }

}
