package com.taozeyu.taolan.virtualMachine;

import com.taozeyu.taolan.virtualMachine.nativeobject.ArrayNativeObject;
import com.taozeyu.taolan.virtualMachine.nativeobject.ContainerNativeObject;

import javax.print.attribute.standard.MediaSize;
import java.util.HashMap;
import java.util.Map;

public class TaolanNativeObject extends TaolanObject {

    public static final TaolanNativeObject Object = new TaolanNativeObject(null, null);
    public static final TaolanNativeObject Globals = new TaolanNativeObject(Object, null);
    public static final TaolanNativeObject Error = new TaolanNativeObject(Object, null);
    public static final TaolanNativeObject Array = new ArrayNativeObject();
    public static final TaolanNativeObject Container = new ContainerNativeObject();

    private static boolean hasInitBinding = false;

    private final Map<String, NativeFunction> functionsTable = new HashMap<>();

    public TaolanNativeObject(TaolanObject prototype, TaolanObject constructor) {
        super(prototype, constructor);
    }

    public Integer getFunctionIdWithName(String functionName) {
        Integer functionId = super.getFunctionIdWithName(functionName);
        if (functionId == null) {
            NativeFunction nativeFunction = functionsTable.get(functionName);
            if (nativeFunction != null) {
                functionId = nativeFunction.functionId();
            }
        }
        return functionId;
    }

    private TaolanNativeObject bind(String functionName, NativeFunction nativeFunction) {
        functionsTable.put(functionName, nativeFunction);
        return this;
    }

    public static void bindAllNativeObjects() {
        if (hasInitBinding) {
            return;
        }
        hasInitBinding = true;

        Object.bind("Object", NativeFunction.Object)
              .bind("Error", NativeFunction.Error)
              .bind("Array", NativeFunction.Array)
              .bind("Container", NativeFunction.Container)
              .bind("alloc", NativeFunction.Alloc)
              .bind("prototype", NativeFunction.Prototype)
              .bind("constructor", NativeFunction.Constructor)
              .bind("extend", NativeFunction.Extend)
              .bind("hash", NativeFunction.Hash)
              .bind("equals", NativeFunction.Equals)
              .bind("toString", NativeFunction.ToString)
              .bind("_print", NativeFunction.Print);

        Array.bind("get", NativeFunction.ArrayGet)
             .bind("set", NativeFunction.ArraySet)
             .bind("add", NativeFunction.ArrayAdd)
             .bind("remove", NativeFunction.ArrayRemove)
             .bind("length", NativeFunction.ArrayLength);

        Container.bind("get", NativeFunction.ContainerGet)
                 .bind("set", NativeFunction.ContainerSet)
                 .bind("remove", NativeFunction.ContainerRemove)
                 .bind("keys", NativeFunction.ContainerAllKeys)
                 .bind("length", NativeFunction.ContainerLength);
    }
}
