package com.taozeyu.taolan.intermediatecode;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.taozeyu.taolan.analysis.node.ArrayNode;
import com.taozeyu.taolan.analysis.node.ContainerNode;
import com.taozeyu.taolan.analysis.node.ElementNode;
import com.taozeyu.taolan.analysis.node.ElementNode.ElementType;
import com.taozeyu.taolan.analysis.node.ExpressionNode;
import com.taozeyu.taolan.intermediatecode.CodeChunk.Command;
import com.taozeyu.taolan.intermediatecode.CodeChunk.ImmediateNumber;
import com.taozeyu.taolan.intermediatecode.CodeChunk.ImmediateType;

class ExpressionGenerator {

    final static ExpressionGenerator instance = new ExpressionGenerator();

    int createExpression(ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        int var = context.variablePool.createIndex();
        createExpression(var, expression, context);
        return var;
    }

    int createChildExpressionAt(ExpressionNode parent, int index, ChunkContext context) throws IntermediateCodeExpression {
        int var = context.variablePool.createIndex();
        createChildExpressionAt(var,  parent, index, context);
        return var;
    }

    void createExpression(int var, ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        if(expression.invoker != null) {
            FunctionGenerator.instance.createInvoker(var, expression, context);
        } else if("?:".equals(expression.sign)) {
            createConditionExpression(var, expression, context);
        } else {
            createSingleOrDoubleOperator(var, expression, context);
        }
    }

    private void createConditionExpression(int var, ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        createChildExpressionAt(var, expression, 0, context);
        int leftExpressionHolder = context.positionPlaceholder.createPosition();
        int endHolder = context.positionPlaceholder.createPosition();
        context.codeChunk.push(Command.JumpWhen, leftExpressionHolder, var);
        createChildExpressionAt(var, expression, 2, context);
        context.codeChunk.push(Command.Jump, endHolder);
        context.positionPlaceholder.setPosition(leftExpressionHolder, context.codeChunk.getCurrentPostion());
        createChildExpressionAt(var, expression, 1, context);
        context.positionPlaceholder.setPosition(endHolder, context.codeChunk.getCurrentPostion());
    }

    private void createSingleOrDoubleOperator(int var, ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        int num1, num2;
        if("+".equals(expression.sign)) {
            if(expression.any(1)) {
                num1 = var;
                num2 = context.variablePool.createIndex();
                createChildExpressionAt(num1, expression, 0, context);
                createChildExpressionAt(num2, expression, 1, context);
                context.codeChunk.push(Command.Add, num1, num2);
                context.variablePool.freeIndex(num2);
            } else {
                createChildExpressionAt(var, expression, 0, context);
            }
        } else if("-".equals(expression.sign)) {
            if(expression.any(1)) {
                num1 = var;
                num2 = context.variablePool.createIndex();
                createChildExpressionAt(num1, expression, 0, context);
                createChildExpressionAt(num2, expression, 1, context);
                context.codeChunk.push(Command.Sub, num1, num2);
                context.variablePool.freeIndex(num2);
            } else {
                createChildExpressionAt(var, expression, 0, context);
                context.codeChunk.push(Command.Opposite, var);
            }
        } else if("*".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Mul, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("/".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Div, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("%".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Mod, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if(">".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Gt, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("<".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Lt, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if(">=".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Gte, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("<=".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Lte, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("==".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Equal, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("!=".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.NotEqual, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("=~".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Match, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("<<".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Insert, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("..".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Range, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("!".equals(expression.sign)) {
            createChildExpressionAt(var, expression, 0, context);
            context.codeChunk.push(Command.Not, var);

        } else if("&&".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.And, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("||".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Or, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("^".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Xor, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("instanceof".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.InstanceOf, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("is".equals(expression.sign)) {
            num1 = var;
            num2 = context.variablePool.createIndex();
            createChildExpressionAt(num1, expression, 0, context);
            createChildExpressionAt(num2, expression, 1, context);
            context.codeChunk.push(Command.Is, num1, num2);
            context.variablePool.freeIndex(num2);

        } else if("=".equals(expression.sign)) {
            createSetter(var, expression, context);

        } else if("+=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.Add, context);

        } else if("-=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.Sub, context);

        } else if("*=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.Mul, context);

        } else if("/=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.Div, context);

        } else if("%=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.Mod, context);

        } else if("&&=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.And, context);

        } else if("||=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.Or, context);

        } else if("^=".equals(expression.sign)) {
            createOperatorAndSetter(var, expression, Command.Xor, context);

        } else if(".".equals(expression.sign)) {
            int target = context.variablePool.createIndex();
            createChildExpressionAt(target, expression, 0, context);
            String funName = expression.getElementAt(1).value;
            FunctionGenerator.instance.createInvoker(target, var, funName, null, null, context);

        } else if("[".equals(expression.sign)) {
            int indexVar = context.variablePool.createIndex();
            int targetVar = context.variablePool.createIndex();
            createChildExpressionAt(targetVar, expression, 0, context);
            createChildExpressionAt(indexVar, expression, 1, context);

            ImmediateNumber immediateNumber = new ImmediateNumber();
            immediateNumber.type = ImmediateType.String;
            immediateNumber.stringValue = "get";
            context.codeChunk.push(Command.Push, indexVar);
            context.codeChunk.push(Command.InvokeVirtual, targetVar, 1, -1, immediateNumber);
            context.codeChunk.push(Command.Pop, var);

            context.variablePool.freeIndex(targetVar);
            context.variablePool.freeIndex(indexVar);

        } else if(expression.isElement(0)){
            createElement(var, expression.getElementAt(0), context);
        }
    }

    private void createOperatorAndSetter(int var, ExpressionNode expression, Command command, ChunkContext context) throws IntermediateCodeExpression {
        int num1 = var;
        int num2 = context.variablePool.createIndex();
        createChildExpressionAt(num1, expression, 0, context);
        createChildExpressionAt(num2, expression, 1, context);
        context.codeChunk.push(command, num1, num2);
        context.variablePool.freeIndex(num2);
        createSetterWithInitSet(var, num1, expression, context);
    }

    private void createSetter(int resultVar, ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        if(expression.isElement(0)) {
            int setVar = context.variablePool.createIndex();
            createChildExpressionAt(setVar, expression, 1, context);
            createSetterWithInitSet(resultVar, setVar, expression, context);
            context.variablePool.freeIndex(setVar);
        } else {
            ExpressionNode target = expression.getExpressionAt(0);
            if("[".equals(target.sign)) {
                int indexVar = context.variablePool.createIndex();
                int targetVar = context.variablePool.createIndex();
                createChildExpressionAt(targetVar, target, 0, context);
                createChildExpressionAt(indexVar, target, 1, context);
                createChildExpressionAt(resultVar, expression, 1, context);

                ImmediateNumber immediateNumber = new ImmediateNumber();
                immediateNumber.type = ImmediateType.String;
                immediateNumber.stringValue = "set";
                context.codeChunk.push(Command.Push, indexVar);
                context.codeChunk.push(Command.Push, resultVar);
                context.codeChunk.push(Command.InvokeVirtual, targetVar, 2, -1, immediateNumber);
                context.codeChunk.push(Command.Pop, resultVar);

                context.variablePool.freeIndex(targetVar);
                context.variablePool.freeIndex(indexVar);
            } else {
                throw new IntermediateCodeExpression("unknown sign "+ target.sign);
            }
        }
    }

    private void createSetterWithInitSet(int resultVar, int setVar, ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        if(expression.isElement(0)) {

            ElementNode element = expression.getElementAt(0);
            String variableName = element.value;
            if (element.isLocalVariable()) {
                if (context.variableRecorder.contains(variableName)) {
                    int localVariableIndex = context.variableRecorder.getVarIndex(variableName);
                    context.codeChunk.push(Command.Set, localVariableIndex, setVar);
                    context.codeChunk.push(Command.Set, resultVar, localVariableIndex);
                } else {
                    context.codeChunk.push(Command.GetThis, resultVar);
                    context.codeChunk.push(Command.Push, setVar);
                    FunctionGenerator.instance.invokeByName(resultVar, 1, resultVar, -1, variableName+"=", context);
                }
            } else if (element.isAttributeVariable()) {
                VariableGenerator.instance.setAttributeVariableValue(resultVar, setVar, element, context);
            }
        } else {
            if (true) throw new RuntimeException("not implements");
        }
    }

    private void createChildExpressionAt(int var, ExpressionNode parent, int index, ChunkContext context) throws IntermediateCodeExpression {
        if(parent.isElement(index)) {
            createElement(var, parent.getElementAt(index), context);
        } else {
            createExpression(var, parent.getExpressionAt(index), context);
        }
    }

    private void createElement(int var, ElementNode element, ChunkContext context) throws IntermediateCodeExpression {
        if(element.type == ElementType.Variable) {
            String variableName = element.value;
            if (element.isLocalVariable()) {
                if (context.variableRecorder.contains(variableName)) {
                    int localVariableIndex = context.variableRecorder.getVarIndex(variableName);
                    context.codeChunk.push(Command.Set, var, localVariableIndex);
                } else {
                    context.codeChunk.push(Command.GetThis, var);
                    FunctionGenerator.instance.invokeByName(var, 0, var, -1, variableName, context);
                }
            } else if (element.isAttributeVariable()) {
                VariableGenerator.instance.getAttributesValue(var, element, context);
            }
        } else if(element.type == ElementType.RegEx) {
            int paramVar = context.variablePool.createIndex();
            ImmediateNumber immediateNumber = new ImmediateNumber();
            immediateNumber.type = ImmediateType.String;
            immediateNumber.stringValue = element.value;
            context.codeChunk.push(Command.SetImmediate, paramVar, immediateNumber);
            context.codeChunk.push(Command.Push, paramVar);
            context.codeChunk.push(Command.NewRegEx);
            context.variablePool.freeIndex(paramVar);

        } else if(element.type == ElementType.This) {
            context.codeChunk.push(Command.GetThis, var);

        } else if(element.type == ElementType.Array) {
            ArrayNode array = element.array;
            for(ExpressionNode expression:array.content) {
                int paramVar = createExpression(expression, context);
                context.codeChunk.push(Command.Push, paramVar);
                context.variablePool.freeIndex(paramVar);
            }
            context.codeChunk.push(Command.NewArray, var, array.content.size());

        } else if(element.type == ElementType.Container) {
            ContainerNode container = element.container;
            for(Entry<String, ExpressionNode> e:container.content.entrySet()) {
                String key = e.getKey();
                ExpressionNode value = e.getValue();
                ImmediateNumber immediateNumber = new ImmediateNumber();

                int keyVar = context.variablePool.createIndex();
                immediateNumber.type = ImmediateType.String;
                immediateNumber.stringValue = key;
                context.codeChunk.push(Command.SetImmediate, keyVar, immediateNumber);
                context.codeChunk.push(Command.Push, keyVar);
                context.variablePool.freeIndex(keyVar);

                int valueVar = createExpression(value, context);
                context.codeChunk.push(Command.Push, valueVar);
                context.variablePool.freeIndex(valueVar);
            }
            context.codeChunk.push(Command.NewContainer, var, container.content.size());

        } else {
            ImmediateNumber immediateNumber = new ImmediateNumber();
            if(element.type == ElementType.Null) {
                immediateNumber.type = ImmediateType.Null;

            } else if(element.type == ElementType.Integer) {
                immediateNumber.type = ImmediateType.Integer;
                immediateNumber.integerValue = Integer.valueOf(element.value);

            } else if(element.type == ElementType.Number) {
                immediateNumber.type = ImmediateType.Number;
                immediateNumber.numberValue = Float.valueOf(element.value);

            } else if(element.type == ElementType.String) {
                immediateNumber.type = ImmediateType.String;
                immediateNumber.stringValue = element.value;

            } else if (element.type == ElementType.Boolean) {
                immediateNumber.type = ImmediateType.Boolean;
                immediateNumber.booleanValue = element.value.equals("true");
            }
            context.codeChunk.push(Command.SetImmediate, var, immediateNumber);
        }
    }
}
