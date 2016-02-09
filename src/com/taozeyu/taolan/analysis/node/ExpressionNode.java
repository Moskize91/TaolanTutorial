package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;

import com.taozeyu.taolan.analysis.AnalysisNode;
import com.taozeyu.taolan.analysis.TerminalSymbol;

public abstract class ExpressionNode extends AnalysisNode {

    public String sign = null;

    public InvokerNode invoker = null;

    final public ExpressionNode[] expressionOperands = new ExpressionNode[3];
    final public ElementNode[] elementOperands = new ElementNode[3];

    protected ExpressionNode fork() {
        ExpressionNode forked = new ExpressionNode() {
            @Override
            public void match(AnalysisNode analysisNode) {}

            @Override
            public void match(TerminalSymbol token) {}
        };
        forked.copy(this);
        return forked;
    }

    public void copy(ExpressionNode other) {
        for(int i=0; i<3; ++i) {
            this.expressionOperands[i] = other.expressionOperands[i];
            this.elementOperands[i] = other.elementOperands[i];
        }
        this.sign = other.sign;
        this.invoker = other.invoker;
    }

    public void clear() {
        for(int i=0; i<3; ++i) {
            expressionOperands[i] = null;
            elementOperands[i] = null;
        }
        sign = null;
        invoker = null;
    }

    protected boolean isSecondPositionFilled() {
        return invoker != null || (sign != null && any(1));
    }

    public ExpressionNode getExpressionAt(int index) {
        return expressionOperands[index];
    }

    public ElementNode getElementAt(int index) {
        return elementOperands[index];
    }

    public boolean isExpression(int index) {
        return expressionOperands[index] != null;
    }

    public boolean isElement(int index) {
        return elementOperands[index] != null;
    }

    public boolean any(int index) {
        return expressionOperands[index] != null || elementOperands[index] != null;
    }

    @Override
    public void print(int retractNum, PrintStream out) {
        if(invoker != null) {
            printAt(0, retractNum, out);
            invoker.print(retractNum, out);

        } else if(sign == null) {
            printAt(0, retractNum, out);

        } else {
            int num = 0;
            for(int i=0; i<3; ++i) {
                if(any(i)) {
                    num ++;
                }
            }
            if(num == 3) {
                printAt(0, retractNum, out);
                out.print(" ? ");
                printAt(1, retractNum, out);
                out.print(" : ");
                printAt(2, retractNum, out);
            }
            else if(num == 2) {
                printAt(0, retractNum, out);
                out.print(" ");
                out.print(sign);
                out.print(" ");
                printAt(1, retractNum, out);
            }
            else {
                out.print(sign);
                out.print(" ");
                printAt(0, retractNum, out);
            }
        }
    }

    private void printAt(int index, int retractNum, PrintStream out) {
        if(isElement(index)) {
            elementOperands[index].print(retractNum, out);
        } else {
            out.print("(");
            expressionOperands[index].print(retractNum, out);
            out.print(")");
        }
    }
}
