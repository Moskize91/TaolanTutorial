package com.taozeyu.taolan.intermediatecode;

import java.util.*;

import com.taozeyu.taolan.analysis.node.ChunkNode;
import com.taozeyu.taolan.analysis.node.DefineFunctionNode;
import com.taozeyu.taolan.analysis.node.ElementNode;
import com.taozeyu.taolan.analysis.node.ElementNode.ElementType;
import com.taozeyu.taolan.analysis.node.ExpressionNode;
import com.taozeyu.taolan.analysis.node.InvokerNode;
import com.taozeyu.taolan.analysis.node.LambdaNode;
import com.taozeyu.taolan.intermediatecode.CodeChunk.Command;
import com.taozeyu.taolan.intermediatecode.CodeChunk.ImmediateNumber;
import com.taozeyu.taolan.intermediatecode.CodeChunk.ImmediateType;

class FunctionGenerator {

    final static FunctionGenerator instance = new FunctionGenerator();

    void createInvoker(int var, ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        InvokerNode invoker = expression.invoker;
        Integer target;
        String funName;
        if(expression.isExpression(0)) {
            ExpressionNode parentNode = expression.getExpressionAt(0);
            if(".".equals(parentNode.sign)) {
                target = ExpressionGenerator.instance.createChildExpressionAt(parentNode, 0, context);
                funName = getFunctionNameFrom(1, parentNode);
            } else {
                throw new IntermediateCodeExpression();
            }
        } else {
            target = null;
            funName = getFunctionNameFrom(0, expression);
        }
        createInvoker(target, var, funName, invoker.paramList, invoker.lambda, context);
        if(target != null) {
            context.variablePool.freeIndex(target);
        }
    }

    private String getFunctionNameFrom(int index, ExpressionNode parent) throws IntermediateCodeExpression {
        String funName;
        if(parent.isElement(index)) {
            ElementNode element = parent.getElementAt(index);
            if(element.type == ElementType.Variable && !element.fromConstructor && !element.fromThis) {
                funName = element.value;
            } else {
                throw new IntermediateCodeExpression();
            }
        } else {
            throw new IntermediateCodeExpression();
        }
        return funName;
    }

    void createInvoker(Integer target, int res, String funName, List<ExpressionNode> paramList, LambdaNode lambda, ChunkContext context) throws IntermediateCodeExpression {
        if(target != null) {
            invokerVirtual(target, res, funName, paramList, lambda, context);
        } else {
            invoker(res, funName, paramList, lambda, context);
        }
    }

    private void invokerVirtual(int target, int res, String funName, List<ExpressionNode> paramList, LambdaNode lambda, ChunkContext context) throws IntermediateCodeExpression {
        pushParamsBeforeInvoke(paramList, context);
        int lambdaID = -1;
        if(lambda != null) {
            lambdaID = createLambda(lambda, context);
        }
        int paramsCount = paramList == null? 0: paramList.size();
        invokeByName(target, paramsCount, res, lambdaID, funName, context);
    }

    private void invoker(int res, String funName, List<ExpressionNode> paramList, LambdaNode lambda, ChunkContext context) throws IntermediateCodeExpression {
        pushParamsBeforeInvoke(paramList, context);
        int paramsCount = paramList == null? 0: paramList.size();

        int lambdaFunctionId = -1;
        if(lambda != null) {
            lambdaFunctionId = createLambda(lambda, context);
        }
        Integer funID = context.functionIDMapper.get(funName);

        if(funID != null) {
            context.codeChunk.push(Command.Invoke, funID, paramsCount, lambdaFunctionId);
            context.codeChunk.push(Command.Pop, res);

        } else if (funName.equals("lambda")) {
            context.codeChunk.push(Command.InvokeLambda, paramsCount, lambdaFunctionId);
            context.codeChunk.push(Command.Pop, res);

        } else {
            int target = context.variablePool.createIndex();
            context.codeChunk.push(Command.GetThis, target);
            invokeByName(target, paramsCount, res, lambdaFunctionId, funName, context);
            context.variablePool.freeIndex(target);
        }
    }

    private void pushParamsBeforeInvoke(List<ExpressionNode> paramList, ChunkContext context) throws IntermediateCodeExpression {
        if(paramList != null) {
            for(ExpressionNode param:paramList) {
                int paramVar = ExpressionGenerator.instance.createExpression(param, context);
                context.codeChunk.push(Command.Push, paramVar);
                context.variablePool.freeIndex(paramVar);
            }
        }
    }

    void invokeByName(int target, int paramsSize, int res, int lambdaID, String funName, ChunkContext context) {
        ImmediateNumber immediateNumber = new ImmediateNumber();
        immediateNumber.type = ImmediateType.String;
        immediateNumber.stringValue = funName;
        context.codeChunk.push(Command.InvokeVirtual, target, paramsSize, lambdaID, immediateNumber);
        context.codeChunk.push(Command.Pop, res);
    }

    private int createLambda(LambdaNode lambda, ChunkContext context) throws IntermediateCodeExpression {
        return generateFunction(context, lambda.chunk, lambda.paramNameList);
    }

    int createFunction(DefineFunctionNode funNode, ChunkContext context) throws IntermediateCodeExpression {
        String funName = funNode.functionName;
        if(funNode.operator != null) {
            funName += funNode.operator;
        }
        int funID = generateFunction(context, funNode.body, funNode.paramNames);
        if(funNode.parentExpression != null) {
            int parentVar = ExpressionGenerator.instance.createExpression(funNode.parentExpression, context);
            ImmediateNumber functionName = new ImmediateNumber();
            functionName.type = ImmediateType.String;
            functionName.stringValue = funName;
            context.codeChunk.push(Command.DefFunction, parentVar, funID, -1, functionName);
            context.variablePool.freeIndex(parentVar);
        } else {
            if(context.functionIDMapper.containsKey(funName)) {
                throw new IntermediateCodeExpression("function has defined " + funName);
            }
            context.functionIDMapper.put(funName, funID);
        }
        return funID;
    }

    private int generateFunction(ChunkContext context, ChunkNode body, List<String> paramNames) throws IntermediateCodeExpression {
        ChunkContext childContext = context.extend();
        if(paramNames != null) {
            int index = 0;
            for(String paramName:paramNames) {
                int paramVariableIndex = VariableGenerator.instance.defVariable(paramName, childContext);
                childContext.codeChunk.push(Command.Take, paramVariableIndex, index);
                index++;
            }
        }
        childContext.codeChunk.push(Command.Clear);
        CodeCreator.instance.handleChunk(body, childContext);
        childContext.codeChunk.push(Command.Return, -1);
        int functionId = context.functionRecorder.createFunctionBody(childContext.codeChunk);
        functionId += context.functionStartIndex;
        return functionId;
    }
}
