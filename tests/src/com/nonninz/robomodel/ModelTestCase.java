package com.nonninz.robomodel;

import android.test.AndroidTestCase;

import com.nonninz.robomodel.RoboManager;

public class ModelTestCase extends AndroidTestCase {
    private RoboManager<TestModel> mManager;
    private RoboManager<ParentTestModel> mParentManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mManager = RoboManager.get(getContext(), TestModel.class);
        mParentManager = RoboManager.get(getContext(), ParentTestModel.class);
        mManager.dropDatabase();
    }

    public void testWritesDifferentInstances() {
        ParentTestModel parent = mParentManager.create();
        parent.save();
        
        for (int i = 0; i < 3; i++) {
            TestModel child = mManager.create();
            child.save();
        }

        assertEquals(1, mParentManager.all().size());
        assertEquals(3, mManager.all().size());
    }
    
    public void testToJson() {
        TestModel model = mManager.create();
        assertEquals(String.class, model.toJson().getClass());
    }

}
