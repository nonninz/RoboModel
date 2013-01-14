package com.nonninz.robomodel.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nonninz.robomodel.InstanceNotFoundException;
import com.nonninz.robomodel.RoboManager;
import com.nonninz.robomodel.test.TestModel.Answer;

public class ManagerTestCase extends AndroidTestCase {
    private RoboManager<TestModel> mManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mManager = RoboManager.get(getContext(), TestModel.class);
        mManager.clear();
    }

    public void testAllFindsAllInstances() {
        mManager.create().save();
        mManager.create().save();
        mManager.create().save();

        assertEquals(3, mManager.all().size());
    }

    public void testClear() {
        mManager.create().save();
        mManager.create().save();
        assertEquals(2, mManager.all().size());
        mManager.clear();
        assertEquals(0, mManager.all().size());
    }

    public void testFind() throws InstanceNotFoundException {
        final TestModel model = mManager.create();
        model.bowlFish = false;
        model.byteField = 23;
        model.doubleField = 56;
        model.enumOne = Answer.UNIVERSE;
        model.enumThree = Answer.LIFE;
        model.enumTwo = Answer.EVERYTHING;
        model.floatField = 34;
        model.intField = 12;
        model.longField = 89;
        model.shortField = 9;
        model.springField = "Antani";
        model.save();
        final long id = model.getId();

        final TestModel found = mManager.find(id);
        assertEquals(model.bowlFish, found.bowlFish);
        assertEquals(model.byteField, found.byteField);
        assertEquals(model.doubleField, found.doubleField);
        assertEquals(model.enumOne, found.enumOne);
        assertEquals(model.enumThree, found.enumThree);
        assertEquals(model.enumTwo, found.enumTwo);
        assertEquals(model.floatField, found.floatField);
        assertEquals(model.intField, found.intField);
        assertEquals(model.longField, found.longField);
        assertEquals(model.shortField, found.shortField);
        assertEquals(model.springField, found.springField);
    }

    public void testWhere1() {
        mManager.create().save();
        mManager.create().save();
        final TestModel model = mManager.create();
        model.springField = "Tapioca";
        model.save();

        final List<TestModel> foundModels = mManager.where("springField = 'Tapioca'");
        assertEquals(1, foundModels.size());
        assertEquals(model.getId(), foundModels.get(0).getId());
    }

    public void testWhere2() {
        mManager.create().save();
        mManager.create().save();
        final TestModel model = mManager.create();
        model.springField = "Tapioca";
        model.save();

        final List<TestModel> foundModels = mManager.where("springField = ?",
                        new String[] { "Tapioca" });
        assertEquals(1, foundModels.size());
        assertEquals(model.getId(), foundModels.get(0).getId());
    }
    
    public void testFromJson() {
        TestModel expected = new TestModel(mContext);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        
        String json = gson.toJson(expected);
        
        TestModel actual = mManager.create(json);
        
        assertEquals(expected.bowlFish, actual.bowlFish);
        assertEquals(expected.byteField, actual.byteField);
        assertEquals(expected.doubleField, actual.doubleField);
        assertEquals(expected.floatField, actual.floatField);
        assertEquals(expected.intField, actual.intField);
        assertEquals(expected.longField, actual.longField);
        assertEquals(expected.shortField, actual.shortField);
        assertEquals(expected.springField, actual.springField);
    }

}
