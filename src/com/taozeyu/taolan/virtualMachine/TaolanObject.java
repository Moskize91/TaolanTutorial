package com.taozeyu.taolan.virtualMachine;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class TaolanObject {

    protected static final Class<TaolanObject>[] classConstructorParams = new Class[]{
            TaolanObject.class, TaolanObject.class,
    };
    private static Constructor<? extends TaolanObject> classConstructor;
    {
        try {
            classConstructor = TaolanObject.class.getConstructor(classConstructorParams);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final TaolanObject prototype;
    private final TaolanObject constructor;

    private final Map<String, Integer> bindedFunctionIdMap = new HashMap<>();
    private final Map<String, Value> propertiesTable = new HashMap<>();

    public TaolanObject(TaolanObject prototype, TaolanObject constructor) {
        this.prototype = prototype;
        this.constructor = constructor == null? this: constructor;
    }

    protected Constructor<? extends TaolanObject> getChildConstructor() {
        return classConstructor;
    }

    public static TaolanObject extend(TaolanObject parentObject) {
        Constructor<? extends TaolanObject> childConstructor = parentObject.getChildConstructor();
        try {
            return childConstructor.newInstance(parentObject, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TaolanObject constructBy(TaolanObject constructor) {
        Constructor<? extends TaolanObject> childConstructor = constructor.getChildConstructor();
        try {
            return childConstructor.newInstance(constructor, constructor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TaolanObject getPrototype() {
        return prototype;
    }

    public TaolanObject getConstructor() {
        return constructor;
    }

    public void bindFunctionIdWithName(String functionName, int functionId) {
        bindedFunctionIdMap.put(functionName, functionId);
    }

    public Integer getFunctionIdWithName(String functionName) {
        Integer id = bindedFunctionIdMap.get(functionName);
        if (id == null && prototype != null) {
            id = prototype.getFunctionIdWithName(functionName);
        }
        return id;
    }

    public void setProperty(String name, Value value) {
        propertiesTable.put(name, value);
    }

    public Value getProperty(String name) {
        Value value = propertiesTable.get(name);
        if (value == null) {
            value = Value.NULL;
        }
        return value;
    }
}
