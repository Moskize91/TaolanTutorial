package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import com.taozeyu.taolan.analysis.AnalysisNode;

public abstract class InvokerNode extends AnalysisNode {

    public List<ExpressionNode> paramList = new LinkedList<>();
    public LambdaNode lambda = null;

    @Override
    public void print(int retractNum, PrintStream out) {
        out.print("{");
        printParams(retractNum, out, paramList);
        out.print("}");
        if(lambda != null) {
            lambda.print(retractNum, out);
        }
    }
}
