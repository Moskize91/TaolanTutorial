package com.taozeyu.taolan.virtualMachine;

import com.taozeyu.taolan.virtualMachine.nativeobject.ArrayNativeObject;
import com.taozeyu.taolan.virtualMachine.nativeobject.ContainerNativeObject;

import java.util.List;

public enum NativeFunction {

    Object((TaolanObject thisObject, List<Value> params)-> {
        return new Value(TaolanNativeObject.Object);
    }),

    Error((TaolanObject thisObject, List<Value> params)-> {
        return new Value(TaolanNativeObject.Error);
    }),

    Array((TaolanObject thisObject, List<Value> params)-> {
        return new Value(TaolanNativeObject.Array);
    }),

    Container((TaolanObject thisObject, List<Value> params)-> {
        return new Value(TaolanNativeObject.Container);
    }),

    Alloc((TaolanObject thisObject, List<Value> params)-> {
        return new Value(TaolanObject.constructBy(thisObject));
    }),

    Extend((TaolanObject thisObject, List<Value> params)-> {
        return new Value(TaolanObject.extend(thisObject));
    }),

    Prototype((TaolanObject thisObject, List<Value> params)-> {
        return new Value(thisObject.getPrototype());
    }),

    Constructor((TaolanObject thisObject, List<Value> params)-> {
        return new Value(thisObject.getConstructor());
    }),

    Hash((TaolanObject thisObject, List<Value> params)-> {
        return new Value(thisObject.hashCode());
    }),

    Equals((TaolanObject thisObject, List<Value> params)-> {
        Value param0 = params.isEmpty()? Value.NULL: params.get(0);
        if (param0.type == Value.Type.Object) {
            return new Value(false);
        }
        return new Value(thisObject = param0.objValue);
    }),

    ToString((TaolanObject thisObject, List<Value> params)-> {
        return new Value(thisObject.toString());
    }),

    Print((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() >= 1) {
            Value param = params.get(0);
            System.out.println(param);
        }
        return Value.NULL;
    }),

    ArrayGet((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() < 1) {
            return Value.NULL;
        }
        Value indexValue = params.get(0);
        if (indexValue.type != Value.Type.Integer) {
            return Value.NULL;
        }
        ArrayNativeObject obj = (ArrayNativeObject) thisObject;
        return obj.getValue(indexValue.intValue);
    }),

    ArraySet((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() < 2) {
            return Value.NULL;
        }
        Value indexValue = params.get(0);
        Value value = params.get(1);
        if (indexValue.type != Value.Type.Integer) {
            return Value.NULL;
        }
        ArrayNativeObject obj = (ArrayNativeObject) thisObject;
        obj.setValue(indexValue.intValue, value);
        return value;
    }),

    ArrayAdd((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() < 1) {
            return Value.NULL;
        }
        Value value = params.get(0);
        ArrayNativeObject obj = (ArrayNativeObject) thisObject;
        obj.add(value);
        return Value.NULL;
    }),

    ArrayRemove((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() < 1) {
            return Value.NULL;
        }
        Value indexValue = params.get(0);
        if (indexValue.type != Value.Type.Integer) {
            return Value.NULL;
        }
        ArrayNativeObject obj = (ArrayNativeObject) thisObject;
        return obj.remove(indexValue.intValue);
    }),

    ArrayLength((TaolanObject thisObject, List<Value> params)-> {
        ArrayNativeObject obj = (ArrayNativeObject) thisObject;
        return obj.length();
    }),

    ContainerGet((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() < 1) {
            return Value.NULL;
        }
        Value keyValue = params.get(0);
        ContainerNativeObject obj = (ContainerNativeObject) thisObject;
        return obj.getValue(keyValue);
    }),

    ContainerSet((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() < 2) {
            return Value.NULL;
        }
        Value keyValue = params.get(0);
        Value value = params.get(1);
        ContainerNativeObject obj = (ContainerNativeObject) thisObject;
        return obj.setValue(keyValue, value);
    }),

    ContainerRemove((TaolanObject thisObject, List<Value> params)-> {
        if (params.size() < 1) {
            return Value.NULL;
        }
        Value keyValue = params.get(0);
        ContainerNativeObject obj = (ContainerNativeObject) thisObject;
        return obj.remove(keyValue);
    }),

    ContainerAllKeys((TaolanObject thisObject, List<Value> params)-> {
        ContainerNativeObject obj = (ContainerNativeObject) thisObject;
        ArrayNativeObject array = new ArrayNativeObject(TaolanNativeObject.Array, TaolanNativeObject.Array);
        for (Object keyObject : obj.allKeys()) {
            if (keyObject instanceof  TaolanObject) {
                array.add(new Value((TaolanObject) keyObject));
            } else {
                array.add(new Value((String) keyObject));
            }
        }
        return new Value(array);
    }),

    ContainerLength((TaolanObject thisObject, List<Value> params)-> {
        ContainerNativeObject obj = (ContainerNativeObject) thisObject;
        return obj.length();
    });

    private final NativeInvoker nativeInvoker;
    private static final NativeFunction[] nativeFunctions = NativeFunction.values();

    NativeFunction(NativeInvoker nativeInvoker) {
        this.nativeInvoker = nativeInvoker;
    }

    public int functionId() {
        return this.ordinal();
    }

    private interface NativeInvoker {
        Value call(TaolanObject thisObject, List<Value> params);
    }

    public Value invoke(TaolanObject thisObject, List<Value> params) {
        return nativeInvoker.call(thisObject, params);
    }

    public static Value invoke(int functionId, TaolanObject thisObject, List<Value> params) {
        return nativeFunctions[functionId].invoke(thisObject, params);
    }

    public static int nativeFunctionCount() {
        return nativeFunctions.length;
    }
}
