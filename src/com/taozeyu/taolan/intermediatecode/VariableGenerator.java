package com.taozeyu.taolan.intermediatecode;

import com.taozeyu.taolan.analysis.node.ElementNode;
import com.taozeyu.taolan.intermediatecode.CodeChunk.Command;
import com.taozeyu.taolan.intermediatecode.CodeChunk.ImmediateNumber;
import com.taozeyu.taolan.intermediatecode.CodeChunk.ImmediateType;

class VariableGenerator {

    final static VariableGenerator instance = new VariableGenerator();

    int defVariable(String varName, ChunkContext context) throws IntermediateCodeExpression {
        if(context.variableRecorder.contains(varName)) {
            throw new IntermediateCodeExpression("variable has defined " + varName);
        }
        int varIndex = context.variablePool.createIndex();
        context.variableRecorder.define(varName, varIndex);
        return varIndex;
    }

    void getAttributesValue(int var, ElementNode element, ChunkContext context) throws IntermediateCodeExpression {
        if(element.fromThis) {
            context.codeChunk.push(Command.Rel, var, getStringImmediateNumber(element.value));
        } else if(element.fromConstructor) {
            context.codeChunk.push(Command.RelStatic, var, getStringImmediateNumber(element.value));
        }
    }

    void setAttributeVariableValue(int resultVar, int setVar, ElementNode element, ChunkContext context) throws IntermediateCodeExpression {
        if(element.fromThis) {
            context.codeChunk.push(Command.Move, resultVar, setVar, -1, getStringImmediateNumber(element.value));
        } else if(element.fromConstructor) {
            context.codeChunk.push(Command.MoveStatic, resultVar, setVar, -1, getStringImmediateNumber(element.value));
        }
    }

    private ImmediateNumber getStringImmediateNumber(String variableName) {
        ImmediateNumber in = new ImmediateNumber();
        in.type = ImmediateType.String;
        in.stringValue = variableName;
        return in;
    }
}
