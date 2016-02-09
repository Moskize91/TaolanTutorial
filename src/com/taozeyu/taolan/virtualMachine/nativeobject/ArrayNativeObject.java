package com.taozeyu.taolan.virtualMachine.nativeobject;

import com.taozeyu.taolan.virtualMachine.TaolanNativeObject;
import com.taozeyu.taolan.virtualMachine.TaolanObject;
import com.taozeyu.taolan.virtualMachine.Value;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class ArrayNativeObject extends TaolanNativeObject {

    private static Constructor<? extends TaolanObject> classConstructor;
    {
        try {
            classConstructor = ArrayNativeObject.class.getConstructor(classConstructorParams);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final ArrayList<Value> array = new ArrayList<>();

    public ArrayNativeObject() {
        this(TaolanNativeObject.Object, null);
    }

    public ArrayNativeObject(TaolanObject prototype, TaolanObject constructor) {
        super(prototype, constructor);
    }

    @Override
    protected Constructor<? extends TaolanObject> getChildConstructor() {
        return classConstructor;
    }

    public void setValue(int index, Value value) {
        index = convertNegativeIndex(index);
        int needAddedCount = index + 1 - array.size();
        for (int i=0; i<needAddedCount; ++i) {
            array.add(Value.NULL);
        }
        array.set(index, value);
    }

    public Value getValue(int index) {
        index = convertNegativeIndex(index);
        if (index >= array.size()) {
            return Value.NULL;
        }
        return array.get(index);
    }

    public void add(Value value) {
        array.add(value);
    }

    public Value remove(int index) {
        index = convertNegativeIndex(index);
        if (index >= array.size()) {
            return Value.NULL;
        }
        return array.remove(index);
    }

    public Value length() {
        return new Value(array.size());
    }

    private int convertNegativeIndex(int index) {
        if (index < 0) {
            //转化成能指向 array 的一个元素的非零数索引。
            int size = array.size();
            if (size == 0) {
                index = 0;
            } else {
                index = size - ((-index - 1)%size + 1);
            }
        }
        return index;
    }
}
