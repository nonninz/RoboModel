package com.nonninz.robomodel.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.nonninz.robomodel.InstanceNotFoundException;
import com.nonninz.robomodel.RoboManager;
import com.nonninz.robomodel.test.TestModel.Answer;

public class ManagerTestCase extends AndroidTestCase {
    private RoboManager<TestModel> mManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mManager = new RoboManager<TestModel>(getContext());
    }

    public void testAllFindsAllInstances() {
        new TestModel(mContext).save();
        new TestModel(mContext).save();
        new TestModel(mContext).save();

        assertEquals(3, mManager.all().size());
    }

    public void testClear() {
        new TestModel(mContext).save();
        new TestModel(mContext).save();
        assertEquals(2, mManager.all().size());
        mManager.clear();
        assertEquals(0, mManager.all().size());
    }

    public void testFind() throws InstanceNotFoundException {
        final TestModel model = new TestModel(mContext);
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
        new TestModel(mContext).save();
        new TestModel(mContext).save();
        final TestModel model = new TestModel(mContext);
        model.springField = "Tapioca";
        model.save();

        final List<TestModel> foundModels = mManager.where("springField = 'Tapioca'");
        assertEquals(1, foundModels.size());
        assertEquals(model.getId(), foundModels.get(0).getId());
    }

    public void testWhere2() {
        new TestModel(mContext).save();
        new TestModel(mContext).save();
        final TestModel model = new TestModel(mContext);
        model.springField = "Tapioca";
        model.save();

        final List<TestModel> foundModels = mManager.where("springField = ?",
                        new String[] { "Tapioca" });
        assertEquals(1, foundModels.size());
        assertEquals(model.getId(), foundModels.get(0).getId());
    }

}
