package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;

import com.taozeyu.taolan.analysis.AnalysisNode;


public abstract class WhenNode extends AnalysisNode {

    public ExpressionNode condition;

    @Override
    public void print(int retractNum, PrintStream out) {
        out.print(" when ");
        condition.print(retractNum, out);
    }
}
