package com.taozeyu.taolan.virtualMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DataChunk {

    private final List<Value> dataArray = new ArrayList<>();
    private final List<Value> paramArray;
    private final LinkedList<TryCatchBlockData> tryCatchBlockStack = new LinkedList<>();
    private final TaolanObject contextObject;

    private final int lambdaFunctionId;
    private final int resumeLine;

    private State state = State.Running;
    private Value returnObject;
    private Value errorObject;

    public enum State {
        Running, Error, Return
    }

    public static class TryCatchBlockData {

        private final int catchPosition;
        private final int finallyPosition;
        private boolean hasCatchError = false;

        public TryCatchBlockData(int catchPosition, int finallyPosition) {
            this.catchPosition = catchPosition;
            this.finallyPosition = finallyPosition;
        }

        public int getCatchPosition() {
            return catchPosition;
        }

        public int getFinallyPosition() {
            return finallyPosition;
        }

        public boolean isHasCatchError() {
            return hasCatchError;
        }

        public void setHasCatchError(boolean hasCatchError) {
            this.hasCatchError = hasCatchError;
        }
    }

    public DataChunk(TaolanObject contextObject) {
        this(Collections.emptyList(), contextObject, 0);
    }

    public DataChunk(List<Value> paramArray, TaolanObject contextObject, int resumeLine) {
        this(paramArray, contextObject, resumeLine, -1);
    }

    public DataChunk(List<Value> paramArray, TaolanObject contextObject, int resumeLine, int lambdaFunctionId) {
        this.paramArray = paramArray;
        this.contextObject = contextObject;
        this.resumeLine = resumeLine;
        this.lambdaFunctionId = lambdaFunctionId;
    }

    public void setData(int position, Value value) {
        operatePosition(dataArray, position);
        dataArray.set(position, value);
    }

    public Value getData(int position) {
        operatePosition(dataArray, position);
        return dataArray.get(position);
    }

    public Value getParam(int position) {
        if (position < paramArray.size()) {
            return paramArray.get(position);
        } else {
            return Value.NULL;
        }
    }

    public void clearParams() {
        paramArray.clear();
    }

    private void operatePosition(List<Value> list, int position) {
        if (position >= list.size()) {
            int needCount = position - list.size() + 1;
            for (int i=0; i<needCount; ++i) {
                list.add(Value.NULL);
            }
        }
    }

    public int getResumeLine() {
        return resumeLine;
    }

    public TaolanObject getContextObject() {
        return contextObject;
    }

    public int getLambdaFunctionId() {
        return lambdaFunctionId;
    }

    public LinkedList<TryCatchBlockData> getTryCatchBlockStack() {
        return tryCatchBlockStack;
    }

    public State getState() {
        return state;
    }

    public Value getReturnObject() {
        return returnObject;
    }

    public void setReturnObject(Value returnObject) {
        this.returnObject = returnObject;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Value getErrorObject() {
        return errorObject;
    }

    public void setErrorObject(Value errorObject) {
        this.errorObject = errorObject;
    }
}
