package com.taozeyu.taolan.intermediatecode;

import java.util.List;

import com.taozeyu.taolan.analysis.AnalysisNode;
import com.taozeyu.taolan.analysis.node.ChunkNode;
import com.taozeyu.taolan.analysis.node.CommandNode;
import com.taozeyu.taolan.analysis.node.DefineFunctionNode;
import com.taozeyu.taolan.analysis.node.DefineVariableNode;
import com.taozeyu.taolan.analysis.node.ExpressionNode;
import com.taozeyu.taolan.analysis.node.ForEachLoopNode;
import com.taozeyu.taolan.analysis.node.IfElseNode;
import com.taozeyu.taolan.analysis.node.LoopChunkNode;
import com.taozeyu.taolan.analysis.node.OperateNode;
import com.taozeyu.taolan.analysis.node.TryCatchNode;
import com.taozeyu.taolan.intermediatecode.CodeChunk.Command;
import com.taozeyu.taolan.intermediatecode.PlaceholderReplacement.RegisterNode;

class CodeCreator {

    final static CodeCreator instance = new CodeCreator();

    void handleChunk(ChunkNode chunk, ChunkContext context) throws IntermediateCodeExpression {
        if(chunk != null) {
            handleAnalysisNodeList(chunk.lineList, chunk, context);
            RegisterNode node = PlaceholderReplacement.instance.register(context.codeChunk, context.positionPlaceholder);
            context.placeholderRegisterList.add(node);
        }
    }

    private void handleAnalysisNodeList(List<AnalysisNode> list, ChunkNode chunk, ChunkContext context) throws IntermediateCodeExpression {
        for(AnalysisNode node:list) {
            if(node instanceof CommandNode) {
                handleCommand((CommandNode) node, context);

            } else if(node instanceof OperateNode) {
                handleOperate((OperateNode) node, context);

            } else if(node instanceof ExpressionNode) {
                handleExpression((ExpressionNode) node, context);

            } else if(node instanceof DefineVariableNode) {
                handleDefineVariable((DefineVariableNode) node, context);

            } else if(node instanceof DefineFunctionNode) {
                handleDefineFunction((DefineFunctionNode) node, context);

            } else if(node instanceof IfElseNode) {
                handleIfElse((IfElseNode) node, context);

            } else if(node instanceof LoopChunkNode) {
                handleLoop((LoopChunkNode) node, context);

            } else if(node instanceof ForEachLoopNode) {
                handleForLoop((ForEachLoopNode) node, chunk, context);

            } else if(node instanceof TryCatchNode) {
                handleTryCatch((TryCatchNode) node, context);

            } else {
                throw new RuntimeException("unknown node type " + node.getClass().getSimpleName());
            }
        }
    }

    private void handleCommand(CommandNode command, ChunkContext context) throws IntermediateCodeExpression {
        if(command.condition != null) {
            int endHolder = context.positionPlaceholder.createPosition();
            int conditionVar = ExpressionGenerator.instance.createExpression(command.condition.condition, context);
            context.codeChunk.push(Command.JumpUnless, endHolder, conditionVar);
            generateCommandCodes(command, context);
            context.positionPlaceholder.setPosition(endHolder, context.codeChunk.getCurrentPostion());
            context.variablePool.freeIndex(conditionVar);
        } else {
            generateCommandCodes(command, context);
        }
    }

    private void generateCommandCodes(CommandNode commandNode, ChunkContext context) throws IntermediateCodeExpression {
        Command command = null;
        if("return".equals(commandNode.command)) {
            command = Command.Return;
        } else if("throw".equals(commandNode.command)) {
            command = Command.Throw;
        }
        if(command != null) {
            int expression = -1;
            if(commandNode.expression != null) {
                expression = ExpressionGenerator.instance.createExpression(commandNode.expression, context);
            }
            context.codeChunk.push(command, expression);
            context.variablePool.freeIndex(expression);

        } else {
            if(context.jumpStack.isNotInLoop()) {
                throw new IntermediateCodeExpression("not in loop.");
            }
            if("break".equals(commandNode.command)) {
                context.codeChunk.push(Command.Jump, context.jumpStack.getCurrentBreakLocation());
            } else if("continue".equals(commandNode.command)) {
                context.codeChunk.push(Command.Jump, context.jumpStack.getCurrentContinueLocation());
            } else {
                throw new IntermediateCodeExpression("unknown command "+ commandNode.command);
            }
        }
    }

    private void handleOperate(OperateNode operate, ChunkContext context) throws IntermediateCodeExpression {
        int expressionVar;
        if(operate.condition != null) {
            int conditionVar = ExpressionGenerator.instance.createExpression(operate.condition.condition, context);
            int nextHolder = context.positionPlaceholder.createPosition();
            context.codeChunk.push(Command.JumpUnless, nextHolder, conditionVar);
            context.variablePool.freeIndex(conditionVar);
            expressionVar = ExpressionGenerator.instance.createExpression(operate.expression, context);
            context.positionPlaceholder.setPosition(nextHolder, context.codeChunk.getCurrentPostion());
        } else {
            expressionVar = ExpressionGenerator.instance.createExpression(operate.expression, context);
        }
        context.variablePool.freeIndex(expressionVar);
    }

    private void handleExpression(ExpressionNode expression, ChunkContext context) throws IntermediateCodeExpression {
        int expressionVar = ExpressionGenerator.instance.createExpression(expression, context);
        context.variablePool.freeIndex(expressionVar);
    }

    private void handleDefineVariable(DefineVariableNode defVar, ChunkContext context) throws IntermediateCodeExpression {
        String name = defVar.variableName;
        int varIndex = VariableGenerator.instance.defVariable(name, context);
        if(defVar.initValue != null) {
            ExpressionGenerator.instance.createExpression(varIndex, defVar.initValue, context);
        }
    }

    private void handleDefineFunction(DefineFunctionNode defFun, ChunkContext context) throws IntermediateCodeExpression {
        FunctionGenerator.instance.createFunction(defFun, context);
    }

    private void handleIfElse(IfElseNode ifelse, ChunkContext context) throws IntermediateCodeExpression {
        int endHolder = context.positionPlaceholder.createPosition();
        for(int i=0; i<ifelse.conditionExpressionList.size(); ++i) {
            int nextConditionHolder = context.positionPlaceholder.createPosition();
            ExpressionNode condition = ifelse.conditionExpressionList.get(i);
            ChunkNode chunk = ifelse.conditionChuckList.get(i);
            int conditionVar = ExpressionGenerator.instance.createExpression(condition, context);
            context.codeChunk.push(Command.JumpUnless, nextConditionHolder, conditionVar);
            context.variablePool.freeIndex(conditionVar);
            ChunkContext childContext = context.link();
            handleChunk(chunk, childContext);
            context.codeChunk.push(Command.Jump, endHolder);
            context.positionPlaceholder.setPosition(nextConditionHolder, context.codeChunk.getCurrentPostion());
        }
        if(ifelse.elseChuck != null) {
            ChunkContext childContext = context.link();
            handleChunk(ifelse.elseChuck, childContext);
        }
        context.positionPlaceholder.setPosition(endHolder, context.codeChunk.getCurrentPostion());
    }

    private void handleLoop(LoopChunkNode loop, ChunkContext context) throws IntermediateCodeExpression {
        int checkConditionLocation = context.positionPlaceholder.createPosition();
        int endHolder = context.positionPlaceholder.createPosition();

        context.jumpStack.push(endHolder, checkConditionLocation);

        ChunkContext childContext = context.link();
        if(loop.isWhile) {
            childContext.positionPlaceholder.setPosition(checkConditionLocation, childContext.codeChunk.getCurrentPostion());
            int conditionVar = ExpressionGenerator.instance.createExpression(loop.condition, childContext);
            childContext.codeChunk.push(Command.JumpUnless, endHolder, conditionVar);
            childContext.variablePool.freeIndex(conditionVar);
            handleChunk(loop.chunk, childContext);
            childContext.positionPlaceholder.setPosition(endHolder, childContext.codeChunk.getCurrentPostion());
        } else {
            int loopStartPosition = childContext.codeChunk.getCurrentPostion();
            handleChunk(loop.chunk, childContext);
            childContext.positionPlaceholder.setPosition(checkConditionLocation, childContext.codeChunk.getCurrentPostion());
            int conditionVar = ExpressionGenerator.instance.createExpression(loop.condition, childContext);
            childContext.codeChunk.push(Command.JumpWhen, loopStartPosition, conditionVar);
            childContext.variablePool.freeIndex(conditionVar);
            childContext.positionPlaceholder.setPosition(endHolder, childContext.codeChunk.getCurrentPostion());
        }
        context.jumpStack.pop();
        context.codeChunk.push(Command.Jump, checkConditionLocation);
        context.positionPlaceholder.setPosition(endHolder, context.codeChunk.getCurrentPostion());
    }

    private void handleForLoop(ForEachLoopNode loop, ChunkNode outChunk, ChunkContext context) throws IntermediateCodeExpression {
        ChunkContext childContext = context.link();
        int continueHolder = childContext.positionPlaceholder.createPosition();
        int endHolder = childContext.positionPlaceholder.createPosition();

        context.jumpStack.push(endHolder, continueHolder);

        handleAnalysisNodeList(loop.beforeCommandList, outChunk, childContext);
        int loopStartPosition = childContext.codeChunk.getCurrentPostion();
        handleChunk(loop.chunk, childContext);
        childContext.positionPlaceholder.setPosition(continueHolder, childContext.codeChunk.getCurrentPostion());
        handleAnalysisNodeList(loop.afterCommandList, outChunk, childContext);

        int conditionVar = ExpressionGenerator.instance.createExpression(loop.loopCondition, childContext);
        childContext.codeChunk.push(Command.JumpWhen, loopStartPosition, conditionVar);
        childContext.variablePool.freeIndex(conditionVar);
        childContext.positionPlaceholder.setPosition(endHolder, childContext.codeChunk.getCurrentPostion());

        context.jumpStack.pop();
    }

    private void handleTryCatch(TryCatchNode trycatch, ChunkContext context) throws IntermediateCodeExpression {
        int catchHolder = context.positionPlaceholder.createPosition();
        int finallyHolder =  context.positionPlaceholder.createPosition();
        ChunkContext childContext = context.link();

        childContext.codeChunk.push(Command.PushTryBlock, catchHolder, finallyHolder);
        handleChunk(trycatch.tryChunk, childContext);
        childContext.codeChunk.push(Command.JumpFinally);

        context.positionPlaceholder.setPosition(catchHolder, context.codeChunk.getCurrentPostion());
        int errorVar = context.variablePool.createIndex();
        context.codeChunk.push(Command.GetError, errorVar);
        for(int i=0; i<trycatch.typeList.size(); ++i) {
            childContext = context.link();
            int nextLocation = childContext.positionPlaceholder.createPosition();
            ExpressionNode type = trycatch.typeList.get(i);
            String errorName = trycatch.errorNameList.get(i);
            ChunkNode chunk = trycatch.catchChunkList.get(i);
            if(type != null) {
                int typeVar = ExpressionGenerator.instance.createExpression(type, childContext);
                int rsVar = childContext.variablePool.createIndex();
                childContext.codeChunk.push(Command.Set, rsVar, errorVar);
                childContext.codeChunk.push(Command.InstanceOf, rsVar, typeVar);
                childContext.codeChunk.push(Command.JumpUnless, nextLocation, rsVar);
                childContext.variablePool.freeIndex(typeVar);
                childContext.variablePool.freeIndex(rsVar);
            }
            int errorChildVarIndex = VariableGenerator.instance.defVariable(errorName, childContext);
            childContext.codeChunk.push(Command.Set, errorChildVarIndex, errorVar);

            handleChunk(chunk, childContext);
            childContext.codeChunk.push(Command.JumpFinally);
            childContext.positionPlaceholder.setPosition(nextLocation, childContext.codeChunk.getCurrentPostion());
        }
        //至此，说明error未被catch。
        childContext.codeChunk.push(Command.Throw, errorVar);
        childContext.variablePool.freeIndex(errorVar);

        context.positionPlaceholder.setPosition(finallyHolder, context.codeChunk.getCurrentPostion());
        if(trycatch.finallyChunk != null) {
            childContext = context.link();
            handleChunk(trycatch.finallyChunk, childContext);
        }
        context.codeChunk.push(Command.PopTryBlock);
    }
}
