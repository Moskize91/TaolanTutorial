package com.taozeyu.taolan.analysis;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.taozeyu.taolan.analysis.NonTerminalSymbol.Exp;
import com.taozeyu.taolan.analysis.Token.Type;
import com.taozeyu.taolan.analysis.node.ArrayNode;
import com.taozeyu.taolan.analysis.node.ChunkNode;
import com.taozeyu.taolan.analysis.node.CommandNode;
import com.taozeyu.taolan.analysis.node.DefineFunctionNode;
import com.taozeyu.taolan.analysis.node.DefineVariableNode;
import com.taozeyu.taolan.analysis.node.ElementNode;
import com.taozeyu.taolan.analysis.node.ElementNode.ElementType;
import com.taozeyu.taolan.analysis.node.OperateNode;
import com.taozeyu.taolan.analysis.node.StartChunkNode;
import com.taozeyu.taolan.analysis.node.WhenNode;
import com.taozeyu.taolan.analysis.node.ExpressionNode;
import com.taozeyu.taolan.analysis.node.ForEachLoopNode;
import com.taozeyu.taolan.analysis.node.IfElseNode;
import com.taozeyu.taolan.analysis.node.InvokerNode;
import com.taozeyu.taolan.analysis.node.LambdaNode;
import com.taozeyu.taolan.analysis.node.LoopChunkNode;
import com.taozeyu.taolan.analysis.node.TryCatchNode;

class AnalysisDefine {

    private static final HashMap<Exp, Supplier<AnalysisNode>> containerMap = new HashMap<>();
    private static final Supplier<AnalysisNode> containerNodeSupplier = new Supplier<AnalysisNode>() {
        @Override
        public AnalysisNode get() {
            return new DefaultContainerNode();
        }
    };
    /** 仅可用于双目运算符 */
    private static final Supplier<AnalysisNode> expressionDefaultNode = new Supplier<AnalysisNode>() {
        @Override
        public AnalysisNode get() {
            return new ExpressionDefaultNode();
        }
    };
    private static final Pattern IntegerPattern = Pattern.compile("\\d+");

    static {
        creator(new Exp[] {Exp.Number}, () -> {
            return new ElementNode() {

                @Override
                public void match(AnalysisNode analysisNode) {}

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(Type.Number != token.type) {
                        return;
                    }
                    Matcher matcher = IntegerPattern.matcher(token.value);
                    if(!matcher.matches()) {
                        throw new SyntacticAnalysisException(token);
                    }
                    if(this.value == null) {
                        this.type = ElementType.Integer;
                        this.value = token.value;

                    } else {
                        this.type = ElementType.Number;
                        this.value += "." + token.value;
                    }
                }
            };
        });
        creator(new Exp[] {Exp.Variable}, () -> {
            return new ElementNode() {

                @Override
                public void match(AnalysisNode analysisNode) {}

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(Type.Identifier == token.type) {
                        this.type = ElementType.Variable;
                        this.value = token.value;

                    } else if(Type.Sign == token.type) {
                        if("@".equals(token.value)) {
                            this.fromThis = true;
                        } else if("@@".equals(token.value)) {
                            this.fromConstructor = true;
                        }
                    }
                }
            };
        });
        creator(new Exp[] {Exp.String}, () -> {
            return new ElementNode() {

                @Override
                public void match(AnalysisNode analysisNode) {}

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(Type.Identifier == token.type || Type.String == token.type) {
                        this.type = ElementType.String;
                        this.value = token.value;
                    }
                }
            };
        });
        creator(new Exp[] {Exp.This, Exp.Null}, () -> {
            return new ElementNode() {

                @Override
                public void match(AnalysisNode analysisNode) {}

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if("this".equals(token.value)) {
                        this.type = ElementType.This;
                    } else if("null".equals(token.value)) {
                        this.type = ElementType.Null;
                    }
                }
            };
        });
        creator(new Exp[] {Exp.Boolean}, () -> {
            return new ElementNode() {

                @Override
                public void match(AnalysisNode analysisNode) {}

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    this.type = ElementType.Boolean;
                    this.value = token.value;
                }
            };
        });
        creator(new Exp[] {Exp.RegEx}, () -> {
            return new ElementNode() {

                @Override
                public void match(AnalysisNode analysisNode) {}

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(Type.RegEx == token.type) {
                        this.type = ElementType.RegEx;
                        this.value = token.value;
                    }
                }
            };
        });
        creator(new Exp[] {Exp.L11Expression}, () -> {
            return new ExpressionNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    analysisNode = tryGetSingleElement(analysisNode);

                    if(analysisNode instanceof ExpressionNode) {
                        this.expressionOperands[0] = (ExpressionNode) analysisNode;
                    }
                    else if(analysisNode instanceof ElementNode) {
                        this.elementOperands[0] = (ElementNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {}
            };
        });
        creator(new Exp[] {
                Exp.L10Expression, Exp.L8Expression,
                Exp.L7Expression, Exp.L6Expression,
                Exp.L5Expression, Exp.L4Expression,
                Exp.L3Expression, Exp.L2Expression,
                Exp.L0Expression,

                Exp.L10ParamExpression, Exp.L8ParamExpression,
                Exp.L7ParamExpression, Exp.L6ParamExpression,
                Exp.L5ParamExpression, Exp.L4ParamExpression,
                Exp.L3ParamExpression, Exp.L2ParamExpression,
                Exp.L0ParamExpression,

        }, expressionDefaultNode);

        creator(new Exp[] {Exp.L9Expression, Exp.L9ParamExpression}, () -> {
            return new ExpressionNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    analysisNode = tryGetSingleElement(analysisNode);
                    if(analysisNode instanceof ExpressionNode) {
                        this.expressionOperands[0] = (ExpressionNode)analysisNode;
                    } else {
                        this.elementOperands[0] = (ElementNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) {
                    if(token.type == Type.Sign) {
                        this.sign = token.value;
                    }
                }
            };
        });
        creator(new Exp[] {Exp.L1Expression, Exp.L1ParamExpression}, () -> {
            return new ExpressionNode() {

                private int hasSetOperands = 0;

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    analysisNode = tryGetSingleElement(analysisNode);
                    if(analysisNode instanceof ExpressionNode) {
                        this.expressionOperands[hasSetOperands] = (ExpressionNode)analysisNode;
                    } else {
                        this.elementOperands[hasSetOperands] = (ElementNode) analysisNode;
                    }
                    hasSetOperands++;
                    if(hasSetOperands >= 3) {
                        this.sign = "?:";
                    }
                }

                @Override
                public void match(TerminalSymbol token) {}
            };
        });
        creator(new Exp[] {Exp.Chunk}, () -> {
            return new ChunkNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ExpressionNode) {
                        ExpressionNode expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);
                        analysisNode = expression;
                    }
                    lineList.add(analysisNode);
                }

                @Override
                public void match(TerminalSymbol token) { }
            };
        });
        creator(new Exp[] {Exp.StartChunk}, () -> {
            return new StartChunkNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ChunkNode) {
                        chunk = (ChunkNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) { }
            };
        });
        creator(new Exp[] {Exp.Command}, () -> {
            return new CommandNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {

                    if(analysisNode instanceof ExpressionNode) {
                        expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);

                    } else if(analysisNode instanceof WhenNode){
                        condition = (WhenNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) {
                    if(token.type == Type.Keyword) {
                        command = token.value;
                    }
                }
            };
        });
        creator(new Exp[] {Exp.Operate}, () -> {
            return new OperateNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {

                    if(analysisNode instanceof ExpressionNode) {
                        expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);

                    } else if(analysisNode instanceof WhenNode){
                        condition = (WhenNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) { }
            };
        });
        creator(new Exp[] {Exp.DefineVariableElement}, () -> {
            return new DefineVariableNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {

                    if(analysisNode instanceof ExpressionNode) {
                        initValue = (ExpressionNode) analysisNode;
                        clearRedundancy(initValue);
                    }
                }

                @Override
                public void match(TerminalSymbol token) {
                    if(token.type == Type.Identifier) {
                        variableName = token.value;
                    }
                }
            };
        });
        creator(new Exp[] {Exp.DefineFunction}, () -> {
            return new DefineFunctionNode() {

                private boolean nextIdentifyIsFunctionName = false;
                private boolean hasHandledFunctionName = false;

                private boolean hasOperator = false;

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ExpressionNode) {
                        ExpressionNode expression = (ExpressionNode) analysisNode;
                        parentExpression = expression;
                        AnalysisNode target = tryGetSingleElement(expression);
                        if(target instanceof ElementNode) {
                            ElementNode element = (ElementNode) target;
                            if(element.type == ElementType.Variable && !element.fromThis && !element.fromConstructor) {
                                functionName = element.value;
                            }
                        }

                    } else if (analysisNode instanceof ChunkNode) {
                        body = (ChunkNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(token.type == Type.Keyword && "operator".equals(token.value)) {
                        hasOperator = true;

                    } else if(hasOperator && token.type == Type.Sign) {
                        operator = token.value;
                        hasOperator = false;

                    } else if(token.type == Type.Sign && "->".equals(token.value)) {
                        nextIdentifyIsFunctionName = true;

                    } else if(token.type == Type.Identifier) {
                        if(!tryToHandleFunctionName(token.value)) {
                            paramNames.add(token.value);
                        }
                    }
                }

                @Override
                public void finish() throws SyntacticAnalysisException {
                    tryToHandleFunctionName(functionName);
                }

                private boolean tryToHandleFunctionName(String name) throws SyntacticAnalysisException {
                    boolean success = false;
                    if(!hasHandledFunctionName) {
                        success = true;
                        if(nextIdentifyIsFunctionName) {
                            functionName = name;
                        } else {
                            success = false;
                            parentExpression = null;
                        }
                        if(functionName == null) {
                            throw new SyntacticAnalysisException();
                        }
                        hasHandledFunctionName = true;
                    }
                    return success;
                }
            };
        });
        creator(new Exp[] {Exp.IfElseChunk}, () -> {
            return new IfElseNode() {

                private boolean isNextCondition;

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {

                    if(analysisNode instanceof ExpressionNode) {
                        ExpressionNode expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);
                        addExpression(expression);
                    }
                    else if(analysisNode instanceof ChunkNode) {
                        ChunkNode chunck = (ChunkNode) analysisNode;
                        if(isNextCondition) {
                            addChunk(chunck);
                        } else {
                            elseChuck = chunck;
                        }
                    }
                }

                @Override
                public void match(TerminalSymbol token) {
                    if(token.type == Type.Keyword) {
                        if("if".equals(token.value) || "elsif".equals(token.value)) {
                            isNextCondition = true;

                        } else {
                            isNextCondition = false;
                        }
                    }
                }
            };
        });
        creator(new Exp[] {Exp.When}, () -> {
            return new WhenNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {

                    if(analysisNode instanceof ExpressionNode) {
                        condition = (ExpressionNode) analysisNode;
                        clearRedundancy(condition);
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException { }
            };
        });
        creator(new Exp[] {Exp.TryCatch}, () -> {
            return new TryCatchNode() {

                private String lastKeyword;

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {

                    if(analysisNode instanceof ExpressionNode) {
                        ExpressionNode expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);
                        addErrorType(expression);
                    }
                    else if(analysisNode instanceof ChunkNode) {
                        ChunkNode chunk = (ChunkNode) analysisNode;
                        if(lastKeyword.equals("try")) {
                            tryChunk = chunk;

                        } else if(lastKeyword.equals("finally")) {
                            finallyChunk = chunk;

                        } else if(lastKeyword.equals("catch")) {
                            addChunk(chunk);
                        }
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(token.type == Type.Keyword) {
                        lastKeyword = token.value;

                    } else if(token.type == Type.Identifier) {
                        addErrorName(token.value);
                    }
                }
            };
        });
        creator(new Exp[] {Exp.WhileChunk}, () -> {
            return new LoopChunkNode() {
                {
                    isWhile = true;
                }
                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ExpressionNode) {
                        condition = (ExpressionNode) analysisNode;
                        clearRedundancy(condition);
                    }
                    else if(analysisNode instanceof ChunkNode) {
                        chunk = (ChunkNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {}
            };
        });
        creator(new Exp[] {Exp.DoUntilChunk}, () -> {
            return new LoopChunkNode() {
                {
                    isWhile = false;
                }
                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ExpressionNode) {
                        condition = (ExpressionNode) analysisNode;
                        clearRedundancy(condition);
                    }
                    else if(analysisNode instanceof ChunkNode) {
                        chunk = (ChunkNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {}
            };
        });
        creator(new Exp[] {Exp.ForEachCommand}, containerNodeSupplier);
        creator(new Exp[] {Exp.ForEachChunk}, () -> {
            return new ForEachLoopNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof DefaultContainerNode) {
                        DefaultContainerNode container = (DefaultContainerNode) analysisNode;
                        List<AnalysisNode> list = new LinkedList<>();
                        if(beforeCommandList == null) {
                            beforeCommandList = list;
                        } else {
                            afterCommandList = list;
                        }
                        for(Object obj:container) {
                            if(obj instanceof ExpressionNode) {
                                list.add((ExpressionNode) obj);
                                ExpressionNode node = (ExpressionNode) obj;
                                clearRedundancy(node);

                            } else if(obj instanceof DefineVariableNode) {
                                list.add((DefineVariableNode) obj);
                            }
                        }
                    }
                    else if(analysisNode instanceof ExpressionNode) {
                        loopCondition = (ExpressionNode) analysisNode;
                        clearRedundancy(loopCondition);
                    }
                    else if(analysisNode instanceof ChunkNode) {
                        chunk = (ChunkNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {}
            };
        });
        creator(new Exp[] {Exp.Lambda}, () -> {
            return new LambdaNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ChunkNode) {
                        chunk = (ChunkNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(token.type == Type.Identifier) {
                        paramNameList.add(token.value);
                    }
                }
            };
        });
        creator(new Exp[] {
                Exp.Invoker, Exp.InvokerBraceless,
                Exp.InvokerBanLambda, Exp.InvokerBracelessBanLambda
        }, () -> {
            return new InvokerNode() {

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ExpressionNode) {
                        ExpressionNode expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);
                        paramList.add(expression);

                    } else if(analysisNode instanceof LambdaNode) {
                        lambda = (LambdaNode) analysisNode;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {}
            };
        });
        creator(new Exp[] {Exp.Array}, () -> {
            return new ElementNode() {
                {
                    array = new ArrayNode();
                    type = ElementType.Array;
                }
                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ExpressionNode) {
                        ExpressionNode expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);
                        array.content.add(expression);
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {}
            };
        });
        creator(new Exp[] {Exp.Container}, () -> {
            return new ElementNode() {
                {
                    container = new com.taozeyu.taolan.analysis.node.ContainerNode();
                    type = ElementType.Container;
                }
                private String keyBuffered = null;

                @Override
                public void match(AnalysisNode analysisNode) throws SyntacticAnalysisException {
                    if(analysisNode instanceof ExpressionNode) {
                        ExpressionNode expression = (ExpressionNode) analysisNode;
                        clearRedundancy(expression);
                        container.content.put(keyBuffered, expression);
                    }
                    else if(analysisNode instanceof ElementNode) {
                        keyBuffered = ((ElementNode) analysisNode).value;
                    }
                }

                @Override
                public void match(TerminalSymbol token) throws SyntacticAnalysisException {
                    if(token.type == Type.Identifier) {
                        keyBuffered = token.value;
                    }
                }
            };
        });
    }

    static AnalysisNode createContainer(Exp exp) {
        Supplier<AnalysisNode> supplier = containerMap.get(exp);
        AnalysisNode analysisNode = null;
        if(supplier != null) {
            analysisNode = supplier.get();
            analysisNode.setExp(exp);
        }
        return analysisNode;
    }

    private static void clearRedundancy(ExpressionNode node) {
        if(node.sign == null && node.invoker == null && node.isExpression(0) && !node.any(1)) {
            ExpressionNode child = node.getExpressionAt(0);
            AnalysisNode target = tryGetSingleElement(child);
            if(target instanceof ExpressionNode) {
                node.copy((ExpressionNode) target);
            } else {
                node.clear();
                node.elementOperands[0] = (ElementNode) target;
            }

        } else {
            for(int i=0; i<3; ++i) {
                if(node.isExpression(i)) {
                    ExpressionNode child = node.getExpressionAt(i);
                    AnalysisNode target = tryGetSingleElement(child);
                    if(target instanceof ElementNode) {
                        node.expressionOperands[i] = null;
                        node.elementOperands[i] = (ElementNode) target;
                    }
                }
            }
        }
    }

    private static void creator(Exp[] exps, Supplier<AnalysisNode> supplier) {
        for(Exp exp:exps) {
            containerMap.put(exp, supplier);
        }
    }

    private static class DefaultContainerNode extends AnalysisNode implements Iterable<Object>{

        private final LinkedList<Object> containerList = new LinkedList<>();

        @Override
        public Iterator<Object> iterator() {
            return containerList.iterator();
        }

        @Override
        public void match(AnalysisNode analysisNode) {
            containerList.add(analysisNode);
        }

        @Override
        public void match(TerminalSymbol token) {
            containerList.add(token);
        }

        @Override
        public void print(int retractNum, PrintStream out) { }
    }

    private static class ExpressionDefaultNode extends ExpressionNode {

        private TerminalSymbol lastToken = null;

        @Override
        public void match(AnalysisNode node) throws SyntacticAnalysisException {
            if(node instanceof InvokerNode) {
                if(isSecondPositionFilled()) {
                    ExpressionNode forked = this.fork();
                    clear();
                    expressionOperands[0] = forked;
                }
                InvokerNode invokerNode = (InvokerNode) node;
                if(!invokerNode.paramList.isEmpty() || invokerNode.lambda != null) {
                    invoker = invokerNode;
                }
            } else {
                node = tryGetSingleElement(node);
                if(this.any(0)) {
                    forkIfSecondPositionFilled();
                    setAnalysisNodeAt(node, 1);

                } else {
                    setAnalysisNodeAt(node, 0);
                }
            }
        }

        @Override
        public void match(TerminalSymbol token) throws SyntacticAnalysisException {
            if(this.sign == null) {
                if (token.type == Type.Sign ||
                    (token.type == Type.Keyword && (token.value.equals("instanceof") || token.value.equals("is")))) {
                    this.sign = token.value;
                    this.lastToken = token;
                }
            }
            else if(token.type == Type.Identifier) {
                forkIfSecondPositionFilled();
                ElementNode element = createVariableElement(token.value);
                setAnalysisNodeAt(element, 1);
            }
        }

        private void forkIfSecondPositionFilled() {
            if(isSecondPositionFilled()) {
                ExpressionNode forked = this.fork();
                this.clear();
                this.expressionOperands[0] = forked;
                this.sign = lastToken.value;
                lastToken = null;
            }
        }

        private void setAnalysisNodeAt(AnalysisNode analysisNode, int index) {
            if(analysisNode instanceof ExpressionNode) {
                this.expressionOperands[index] = (ExpressionNode) analysisNode;
            } else {
                this.elementOperands[index] = (ElementNode) analysisNode;
            }
        }
    }

    private static AnalysisNode tryGetSingleElement(AnalysisNode node) {
        if(node instanceof ExpressionNode) {
            ExpressionNode expression = (ExpressionNode) node;
            clearRedundancy(expression);
            if(expression.sign == null && expression.invoker == null && expression.isElement(0)) {
                node = expression.getElementAt(0);
            }
        }
        return node;
    }

    private static ElementNode createVariableElement(String varName) {
        ElementNode element = new ElementNode() {
            @Override
            public void match(TerminalSymbol token) {}

            @Override
            public void match(AnalysisNode analysisNode) {}
        };
        element.type = ElementType.Variable;
        element.value = varName;
        return element;
    }
}
