package com.nonninz.robomodel.test;

import android.test.AndroidTestCase;

import com.nonninz.robomodel.RoboManager;

public class ModelTestCase extends AndroidTestCase {
    private RoboManager<TestModel> mChildManager;
    private RoboManager<ParentTestModel> mParentManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mChildManager = RoboManager.get(getContext(), TestModel.class);
        mParentManager = RoboManager.get(getContext(), ParentTestModel.class);
        mChildManager.dropDatabase();
    }

    public void testWritesDifferentInstances() {
        ParentTestModel parent = mParentManager.create();
        parent.save();
        
        for (int i = 0; i < 3; i++) {
            TestModel child = mChildManager.create();
            child.save();
        }

        assertEquals(1, mParentManager.all().size());
        assertEquals(3, mChildManager.all().size());
    }

}
