package com.nonninz.robomodel;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.nonninz.robomodel.RoboModel;
import com.nonninz.robomodel.annotations.HasMany;

public class ParentTestModel extends RoboModel {

    @Expose 
    public String test = "test";
    @Expose @HasMany(TestModel.class)
    public List<TestModel> testModels = new ArrayList<TestModel>();

    ParentTestModel(Context context) {
        super(context);
    }


}
