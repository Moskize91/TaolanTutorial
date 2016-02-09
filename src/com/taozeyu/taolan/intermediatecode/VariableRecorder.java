package com.taozeyu.taolan.intermediatecode;

import java.util.LinkedHashMap;
import java.util.Map;

class VariableRecorder {

    private final Map<String, Integer> variableMap = new LinkedHashMap<>();

    private final VariableRecorder parent;

    VariableRecorder() {
        this(null);
    }

    private VariableRecorder(VariableRecorder parent) {
        this.parent = parent;
    }

    VariableRecorder link() {
        return new VariableRecorder(this);
    }

    boolean contains(String variableName) {
        return getVarIndex(variableName) != null;
    }

    void define(String variableName, int varIndex) {
        variableMap.put(variableName, varIndex);
    }

    Integer getVarIndex(String variableName) {
        Integer varIndex = variableMap.get(variableName);
        if(varIndex == null && parent != null) {
            varIndex = parent.getVarIndex(variableName);
        }
        return varIndex;
    }
}
