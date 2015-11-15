package com.nonninz.robomodel;

import java.util.List;

import android.test.AndroidTestCase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nonninz.robomodel.exceptions.InstanceNotFoundException;
import com.nonninz.robomodel.TestModel.Answer;

public class ManagerTestCase extends AndroidTestCase {
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

    public void testAllFindsAllInstances() {
        mManager.create().save();
        mManager.create().save();
        mManager.create().save();

        assertEquals(3, mManager.all().size());
    }


    public void testAllOnEmptyState() {
        assertEquals(0, mManager.all().size());
    }
    
    public void testFindOnEmptyState() {
        Exception e = null;
        try {
            mManager.find(1);
        } catch (Exception actual) {
            e = actual;
        }
        assertEquals(InstanceNotFoundException.class, e.getClass()); 
    }

    public void testWhereOnEmptyState1() throws InstanceNotFoundException {
        List<TestModel> result = mManager.where("byteField = 42");
        
        assertEquals(0, result.size());
    }
    
    public void testWhereOnEmptyState2() throws InstanceNotFoundException {
        List<TestModel> result = mManager.where("byteField = ?", 
                        new String[] { String.valueOf(42) });
        assertEquals(0, result.size());
    }
    
    public void testLastOnEmptyState() {
        Exception e = null;
        try {
            mManager.last();
        } catch (Exception actual) {
            e = actual;
        }
        assertEquals(InstanceNotFoundException.class, e.getClass()); 
    }
    
    public void testLast() throws InstanceNotFoundException  {
        mManager.create().save();
        TestModel expected = mManager.create();
        expected.stringField = "Hello there!";
        expected.save();

        assertEquals("Hello there!", mManager.last().stringField);
    }
    
    public void testClear() {
        mManager.create().save();
        mManager.create().save();
        assertEquals(2, mManager.all().size());
        mManager.deleteAll();
        assertEquals(0, mManager.all().size());
    }

    public void testClearOnEmptyState() {
        mManager.deleteAll();
    }
    
    public void testFind() throws InstanceNotFoundException {
        final TestModel model = mManager.create();
        model.booleanField = false;
        model.byteField = 23;
        model.doubleField = 56;
        model.enumOne = Answer.UNIVERSE;
        model.enumThree = Answer.LIFE;
        model.enumTwo = Answer.EVERYTHING;
        model.floatField = 34;
        model.intField = 12;
        model.longField = 89;
        model.shortField = 9;
        model.stringField = "Antani";
        model.save();
        final long id = model.getId();

        final TestModel found = mManager.find(id);
        assertEquals(model.booleanField, found.booleanField);
        assertEquals(model.byteField, found.byteField);
        assertEquals(model.doubleField, found.doubleField);
        assertEquals(model.enumOne, found.enumOne);
        assertEquals(model.enumThree, found.enumThree);
        assertEquals(model.enumTwo, found.enumTwo);
        assertEquals(model.floatField, found.floatField);
        assertEquals(model.intField, found.intField);
        assertEquals(model.longField, found.longField);
        assertEquals(model.shortField, found.shortField);
        assertEquals(model.stringField, found.stringField);
    }

    public void testWhere1() {
        mManager.create().save();
        mManager.create().save();
        final TestModel model = mManager.create();
        model.stringField = "Tapioca";
        model.save();

        final List<TestModel> foundModels = mManager.where("stringField = 'Tapioca'");
        assertEquals(1, foundModels.size());
        assertEquals(model.getId(), foundModels.get(0).getId());
    }

    public void testWhere2() {
        mManager.create().save();
        mManager.create().save();
        final TestModel model = mManager.create();
        model.stringField = "Tapioca";
        model.save();

        final List<TestModel> foundModels = mManager.where("stringField = ?",
                        new String[] { "Tapioca" });
        assertEquals(1, foundModels.size());
        assertEquals(model.getId(), foundModels.get(0).getId());
    }
    
    public void testFromJson() throws JsonProcessingException {
        TestModel expected = new TestModel();
        expected.setContext(mContext);
        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(expected);

        TestModel actual = mManager.create(json);
        
        assertEquals(expected.booleanField, actual.booleanField);
        assertEquals(expected.byteField, actual.byteField);
        assertEquals(expected.doubleField, actual.doubleField);
        assertEquals(expected.floatField, actual.floatField);
        assertEquals(expected.intField, actual.intField);
        assertEquals(expected.longField, actual.longField);
        assertEquals(expected.shortField, actual.shortField);
        assertEquals(expected.stringField, actual.stringField);
    }

}
