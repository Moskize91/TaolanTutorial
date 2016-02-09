package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;

import com.taozeyu.taolan.analysis.AnalysisNode;


public abstract class CommandNode extends AnalysisNode {

    public String command = null;
    public ExpressionNode expression = null;
    public WhenNode condition = null;

    @Override
    public void print(int retractNum, PrintStream out) {
        out.print(command);
        if(expression != null) {
            out.print(" ");
            expression.print(retractNum, out);
        }
        if(condition != null) {
            condition.print(retractNum, out);
        }
    }
}
