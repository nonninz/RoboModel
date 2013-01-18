package com.nonninz.robomodel;

import java.util.List;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.nonninz.robomodel.RoboModel;

public class ParentTestModel extends RoboModel {

    @Expose 
    public String test = "test";
    @Expose @HasMany(TestModel.class)
    public List<TestModel> testModels;

    ParentTestModel(Context context) {
        super(context);
    }


}
