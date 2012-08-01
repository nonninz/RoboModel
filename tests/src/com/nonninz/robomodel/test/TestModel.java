package com.nonninz.robomodel.test;

import android.content.Context;

import com.nonninz.robomodel.RoboModel;

public class TestModel extends RoboModel {

    public enum Answer {
        LIFE, UNIVERSE, EVERYTHING
    }

    public String springField = "BTW thanks for all the fish!";;

    public boolean bowlFish = true;
    public byte byteField = 42;
    public short shortField = 4242;
    public int intField = 424242;
    public long longField = 42424242;
    public float floatField = 42.42f;
    public double doubleField = 42.4242;
    public Answer enumOne = Answer.LIFE;
    public Answer enumTwo = Answer.UNIVERSE;
    public Answer enumThree = Answer.EVERYTHING;

    public TestModel(Context context) {
        super(context);
    }
}
