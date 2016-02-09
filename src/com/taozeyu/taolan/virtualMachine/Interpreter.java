package com.taozeyu.taolan.virtualMachine;

import com.taozeyu.taolan.intermediatecode.CodeChunk;
import com.taozeyu.taolan.intermediatecode.CodeChunk.*;
import com.taozeyu.taolan.virtualMachine.nativeobject.ArrayNativeObject;
import com.taozeyu.taolan.virtualMachine.nativeobject.ContainerNativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Interpreter {

    private final static Map<Command, CommandInterpreter> commandMap = new EnumMap<>(Command.class);
    private final Runtime runtime;

    public int run(int line, Code code, DataChunk dataChunk) {
        CommandInterpreter interpreter = commandMap.get(code.getCommand());
        if (interpreter != null) {
            return interpreter.run(this, line, code, dataChunk);
        }
        throw new RuntimeException("unknown command "+ code.getCommand());
    }

    Interpreter(Runtime runtime) {
        this.runtime = runtime;
    }

    private interface CommandInterpreter{
        int run(Interpreter self, int line, Code code, DataChunk dataChunk);
    }

    static {
        Map<Command, CommandInterpreter> M = commandMap;

        /** 流控制 */
        M.put(Command.Jump, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            return code.getNumber1();
        });

        M.put(Command.JumpUnless, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value value = dataChunk.getData(code.getNumber2());
            int nextLine = line + 1;
            if (!value.convertToBoolean()) {
                nextLine = code.getNumber1();
            }
            return nextLine;
        });

        M.put(Command.JumpWhen, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value value = dataChunk.getData(code.getNumber2());
            int nextLine = line + 1;
            if (value.convertToBoolean()) {
                nextLine = code.getNumber1();
            }
            return nextLine;
        });

        /** 函数调用 */
        M.put(Command.Push, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value param = dataChunk.getData(code.getNumber1());
            self.runtime.getParamStack().push(param);
            return line + 1;
        });

        M.put(Command.Take, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            dataChunk.setData(code.getNumber1(), dataChunk.getParam(code.getNumber2()));
            return line + 1;
        });

        M.put(Command.Clear, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            dataChunk.clearParams();
            return line + 1;
        });

        M.put(Command.GetThis, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value context = new Value(dataChunk.getContextObject());
            dataChunk.setData(code.getNumber1(), context);
            return line + 1;
        });

        M.put(Command.DefFunction, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            String functionName = code.getImmediateNumber().getStringValue();
            Value parentValue = dataChunk.getData(code.getNumber1());
            if (parentValue.type != Value.Type.Object) {
                return throwError("can't def function `"+ functionName + "` for "+ parentValue.type, self, dataChunk);
            }
            TaolanObject parent = parentValue.objValue;

            int functionId = code.getNumber2();
            parent.bindFunctionIdWithName(functionName, functionId);
            return line + 1;
        });

        M.put(Command.Invoke, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            int functionID = code.getNumber1();
            int paramsCount = code.getNumber2();
            int lambdaFunctionId = code.getNumber3();

            ArrayList<Value> params = new ArrayList<>(paramsCount);
            for (int i=0; i<paramsCount; ++i) {
                Value param = self.runtime.getParamStack().pop();
                params.add(param);
            }
            Collections.reverse(params);
            CodeChunk body = VirtualMachine.instance().getCustomFunctionBody(functionID);
            DataChunk data = new DataChunk(params, dataChunk.getContextObject(), line + 1, lambdaFunctionId);
            self.runtime.getCodeStack().push(body);
            self.runtime.getDataStack().push(data);
            return 0;
        });

        M.put(Command.InvokeLambda, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            int functionID = dataChunk.getLambdaFunctionId();
            if (functionID == -1) {
                // 如果当前函数没有传入 lambda，则直接作为空返回。
                self.runtime.getParamStack().push(Value.NULL);
                return line + 1;
            }
            int paramsCount = code.getNumber1();
            int lambdaFunctionId = code.getNumber2();

            ArrayList<Value> params = new ArrayList<>(paramsCount);
            for (int i=0; i<paramsCount; ++i) {
                Value param = self.runtime.getParamStack().pop();
                params.add(param);
            }
            Collections.reverse(params);
            CodeChunk body = VirtualMachine.instance().getCustomFunctionBody(functionID);
            DataChunk data = new DataChunk(params, dataChunk.getContextObject(), line + 1, lambdaFunctionId);
            self.runtime.getCodeStack().push(body);
            self.runtime.getDataStack().push(data);
            return 0;
        });

        M.put(Command.InvokeVirtual, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            String functionName = code.getImmediateNumber().getStringValue();
            Value contextValue = dataChunk.getData(code.getNumber1());
            if (contextValue.type != Value.Type.Object) {
                return throwError("can't call `"+ functionName + "` for "+ contextValue.type, self, dataChunk);
            }
            TaolanObject context = contextValue.objValue;
            Integer functionId = context.getFunctionIdWithName(functionName);
            if (functionId == null) {
                return throwError("undefined method `"+ functionName + "` for "+ context, self, dataChunk);
            }

            int paramsCount = code.getNumber2();
            ArrayList<Value> params = new ArrayList<>(paramsCount);
            for (int i=0; i<paramsCount; ++i) {
                Value param = self.runtime.getParamStack().pop();
                params.add(param);
            }
            Collections.reverse(params);

            int lambdaFunctionId = code.getNumber3();

            if (VirtualMachine.instance().isCustomFunction(functionId)) {
                CodeChunk body = VirtualMachine.instance().getCustomFunctionBody(functionId);
                DataChunk data = new DataChunk(params, context, line + 1, lambdaFunctionId);
                self.runtime.getCodeStack().push(body);
                self.runtime.getDataStack().push(data);
                return 0;
            } else {
                Value returnValue = NativeFunction.invoke(functionId, context, params);
                self.runtime.getParamStack().push(returnValue);
                return line + 1;
            }
        });

        /** 异常处理与抛出异常 */
        M.put(Command.PushTryBlock, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            DataChunk.TryCatchBlockData tryCatchBlock = new DataChunk.TryCatchBlockData(code.getNumber1(), code.getNumber2());
            dataChunk.getTryCatchBlockStack().push(tryCatchBlock);
            return line + 1;
        });

        M.put(Command.Throw, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value error = dataChunk.getData(code.getNumber1());
            if (!error.isInstanceOf(new Value(TaolanNativeObject.Error))) {
                return throwError("can only throw a Error.", self, dataChunk);
            }
            return throwError(error, self, dataChunk);
        });

        M.put(Command.GetError, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value error = dataChunk.getErrorObject();
            dataChunk.setErrorObject(null);
            dataChunk.setData(code.getNumber1(), error);
            return line + 1;
        });

        M.put(Command.JumpFinally, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            DataChunk.TryCatchBlockData tryCatchBlock = dataChunk.getTryCatchBlockStack().getFirst();
            return tryCatchBlock.getFinallyPosition();
        });

        M.put(Command.Pop, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            if (self.runtime.isCatchError()) {
                Value error = self.runtime.getParamStack().pop();
                self.runtime.setCatchError(false);
                return throwError(error, self, dataChunk);
            } else {
                dataChunk.setData(code.getNumber1(), self.runtime.getParamStack().pop());
                return line + 1;
            }
        });

        M.put(Command.PopTryBlock, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            dataChunk.getTryCatchBlockStack().pop();
            if (dataChunk.getState() == DataChunk.State.Running) {
                return line + 1;
            } else {
                if (dataChunk.getTryCatchBlockStack().isEmpty()) {
                    if (dataChunk.getState() == DataChunk.State.Error) {
                        self.runtime.setCatchError(true);
                        self.runtime.getParamStack().push(dataChunk.getErrorObject());
                    } else {
                        self.runtime.setCatchError(false);
                        self.runtime.getParamStack().push(dataChunk.getReturnObject());
                    }
                    self.runtime.getCodeStack().pop();
                    self.runtime.getDataStack().pop();
                    return dataChunk.getResumeLine();

                } else {
                    DataChunk.TryCatchBlockData tryCatchBlock = dataChunk.getTryCatchBlockStack().getFirst();
                    if (dataChunk.getState() == DataChunk.State.Error) {
                        return tryCatchBlock.getCatchPosition();
                    } else if (dataChunk.getState() == DataChunk.State.Return) {
                        return tryCatchBlock.getFinallyPosition();
                    }
                }
            }
            throw new RuntimeException();
        });

        M.put(Command.Return, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value returnValue = Value.NULL;
            if (code.getNumber1() != -1) {
                returnValue = dataChunk.getData(code.getNumber1());
            }
            dataChunk.setReturnObject(returnValue);

            if (dataChunk.getTryCatchBlockStack().isEmpty()) {
                self.runtime.setCatchError(false);
                self.runtime.getParamStack().push(returnValue);
                self.runtime.getCodeStack().pop();
                self.runtime.getDataStack().pop();
                return dataChunk.getResumeLine();

            } else {
                DataChunk.TryCatchBlockData tryCatchBlock = dataChunk.getTryCatchBlockStack().getFirst();
                dataChunk.setState(DataChunk.State.Return);
                return tryCatchBlock.getFinallyPosition();
            }
        });

        /** 赋值 */
        M.put(Command.Set, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value value = dataChunk.getData(code.getNumber2());
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        M.put(Command.SetImmediate, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value value = new Value(code.getImmediateNumber());
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        M.put(Command.Move, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            String propertyName = code.getImmediateNumber().getStringValue();
            Value value = dataChunk.getData(code.getNumber2());
            dataChunk.getContextObject().setProperty(propertyName, value);
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        M.put(Command.MoveStatic, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            String propertyName = code.getImmediateNumber().getStringValue();
            Value value = dataChunk.getData(code.getNumber2());
            dataChunk.getContextObject().getConstructor().setProperty(propertyName, value);
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        M.put(Command.Rel, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            String propertyName = code.getImmediateNumber().getStringValue();
            Value value = dataChunk.getContextObject().getProperty(propertyName);
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        M.put(Command.RelStatic, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            String propertyName = code.getImmediateNumber().getStringValue();
            Value value = dataChunk.getContextObject().getConstructor().getProperty(propertyName);
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        /** 数组和映射 */
        M.put(Command.NewArray, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            ArrayNativeObject arrayObject = new ArrayNativeObject(TaolanNativeObject.Array, TaolanNativeObject.Array);
            int length = code.getNumber2();
            for (int i=0; i<length; ++i) {
                arrayObject.add(self.runtime.getParamStack().pop());
            }
            dataChunk.setData(code.getNumber1(), new Value(arrayObject));
            return line + 1;
        });

        M.put(Command.NewContainer, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            ContainerNativeObject containerObject = new ContainerNativeObject(TaolanNativeObject.Container, TaolanNativeObject.Container);
            int length = code.getNumber2();
            for (int i=0; i<length; ++i) {
                //方向要反过来,因为栈是先进后出
                Value value = self.runtime.getParamStack().pop();
                Value key =self.runtime.getParamStack().pop();
                containerObject.setValue(key, value);
            }
            dataChunk.setData(code.getNumber1(), new Value(containerObject));
            return line + 1;
        });

        /** 数值运算 */
        M.put(Command.Add, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.add(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '+' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Sub, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.sub(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '-' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Mul, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.mul(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '*' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Div, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.div(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '/' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Mod, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            if (v1.type != Value.Type.Integer && v2.type == Value.Type.Integer) {
                return throwError("undefined operation for "+ v1 +" '/' "+ v2, self, dataChunk);
            }
            Value rs = new Value(v1.intValue % v2.intValue);
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Gt, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.gt(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '>' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Lt, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.lt(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '<' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Gte, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.gte(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '>=' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Lte, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.lte(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '<=' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Equal, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.checkEquals(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '==' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.NotEqual, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = Value.checkNotEquals(v1, v2);
            if (rs == null) {
                return throwError("undefined operation for "+ v1 +" '!=' "+ v2, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Not, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value value = dataChunk.getData(code.getNumber1());
            value = new Value(!value.convertToBoolean());
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        M.put(Command.Opposite, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value value = dataChunk.getData(code.getNumber1());
            if (value.type == Value.Type.Integer) {
                value = new Value(-value.intValue);
            } else if (value.type == Value.Type.Number) {
                value = new Value(-value.numValue);
            } else {
                return throwError("undefined operation for '-' "+ value, self, dataChunk);
            }
            dataChunk.setData(code.getNumber1(), value);
            return line + 1;
        });

        M.put(Command.And, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = v1.convertToBoolean()? v2: v1;
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Or, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = v1.convertToBoolean()? v1: v2;
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Xor, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = new Value(v1.convertToBoolean() != v2.convertToBoolean());
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.InstanceOf, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = new Value(v1.isInstanceOf(v2));
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });

        M.put(Command.Is, (Interpreter self, int line, Code code, DataChunk dataChunk)->{
            Value v1 = dataChunk.getData(code.getNumber1());
            Value v2 = dataChunk.getData(code.getNumber2());
            Value rs = new Value(v1.isSameTypeOf(v2));
            dataChunk.setData(code.getNumber1(), rs);
            return line + 1;
        });
    }

    private static int throwError(String message, Interpreter self, DataChunk dataChunk) {
        TaolanObject error = TaolanObject.constructBy(TaolanNativeObject.Error);
        error.setProperty("message", new Value(message));
        return throwError(new Value(error), self, dataChunk);
    }

    private static int throwError(Value error, Interpreter self, DataChunk dataChunk) {
        dataChunk.setErrorObject(error);

        if (dataChunk.getTryCatchBlockStack().isEmpty()) {
            self.runtime.setCatchError(true);
            self.runtime.getParamStack().push(error);
            self.runtime.getCodeStack().pop();
            self.runtime.getDataStack().pop();
            return dataChunk.getResumeLine();

        } else {
            DataChunk.TryCatchBlockData tryBlock = dataChunk.getTryCatchBlockStack().getFirst();
            if (tryBlock.isHasCatchError()) {
                dataChunk.setState(DataChunk.State.Error);
                return tryBlock.getFinallyPosition();
            } else {
                dataChunk.setState(DataChunk.State.Running);
                tryBlock.setHasCatchError(true);
                return tryBlock.getCatchPosition();
            }
        }
    }
}
