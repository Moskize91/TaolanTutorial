package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.taozeyu.taolan.analysis.AnalysisNode;

public abstract class DefineFunctionNode extends AnalysisNode {

    public String functionName;
    public ExpressionNode parentExpression = null;
    public String operator = null;
    public ChunkNode body;

    public List<String> paramNames = new LinkedList<>();

    public Set<String> requireSet;
    public int id;

    @Override
    public void print(int retractNum, PrintStream out) {
        out.print("def ");
        if(parentExpression != null) {
            parentExpression.print(retractNum, out);
            out.print("->");
        }
        out.print(functionName);
        if(operator != null) {
            out.print(" operator");
            out.print(operator);
            out.print(" ");
        }
        out.print("{");
        printParams(retractNum, out, paramNames);
        out.print("}\n");
        if(body != null) {
            body.print(retractNum + 1, out);
        }
        printRetract(retractNum, out);
        out.print("end\n");
    }

}
