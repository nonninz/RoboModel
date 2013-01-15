package com.nonninz.robomodel.test;

import java.util.List;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.nonninz.robomodel.RoboModel;

public class ParentTestModel extends RoboModel {

    ParentTestModel(Context context) {
        super(context);
    }

    @Expose public String test = "test";
    @Expose public List<TestModel> testModels;

}
