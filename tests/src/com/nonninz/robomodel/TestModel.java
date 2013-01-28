package com.nonninz.robomodel;

import android.content.Context;

import com.google.gson.annotations.Expose;

public class TestModel extends RoboModel {

    TestModel(Context context) {
        super(context);
    }

    public enum Answer {
        LIFE, UNIVERSE, EVERYTHING
    }

    @Expose public String springField = "BTW thanks for all the fish!";;

    @Expose public boolean bowlFish = true;
    @Expose public byte byteField = 42;
    @Expose public short shortField = 4242;
    @Expose public int intField = 424242;
    @Expose public long longField = 42424242;
    @Expose public float floatField = 42.42f;
    @Expose public double doubleField = 42.4242;
    @Expose public Answer enumOne = Answer.LIFE;
    @Expose public Answer enumTwo = Answer.UNIVERSE;
    @Expose public Answer enumThree = Answer.EVERYTHING;
}
