package com.nonninz.robomodel;

import android.test.AndroidTestCase;

import com.nonninz.robomodel.RoboManager;

public class ModelTestCase extends AndroidTestCase {

    private RoboManager<TestModel> mManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mManager = RoboManager.get(getContext(), TestModel.class);
        mManager.dropDatabase();
    }


    public void testSaveSeveralModels() {
        RoboManager<ParentTestModel> parentManager = RoboManager.get(getContext(), ParentTestModel.class);

        parentManager.create().save();
        parentManager.create().save();
        mManager.create().save();
        mManager.create().save();
        mManager.create().save();
        
        assertEquals(2, parentManager.all().size());
        assertEquals(3, mManager.all().size());
    }
    
    public void testSaveTree() {
        RoboManager<ParentTestModel> parentManager = RoboManager.get(getContext(), ParentTestModel.class);
        ParentTestModel parent = parentManager.create();
        
        for (int i = 0; i < 3; i++) {
            TestModel child = mManager.create();
            parent.testModels.add(child);
        }
        
        parent.save();

        assertEquals(1, parentManager.all().size());
        assertEquals(3, mManager.all().size());
    }
    
    public void testToJson() {
        TestModel model = mManager.create();
        assertEquals(String.class, model.toJson().getClass());
    }

}
