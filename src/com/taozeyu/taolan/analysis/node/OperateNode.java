package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;

import com.taozeyu.taolan.analysis.AnalysisNode;

public abstract class OperateNode extends AnalysisNode {

    public ExpressionNode expression;
    public WhenNode condition;

    @Override
    public void print(int retractNum, PrintStream out) {
        expression.print(retractNum, out);
        if(condition != null) {
            condition.print(retractNum, out);
        }
    }

}
