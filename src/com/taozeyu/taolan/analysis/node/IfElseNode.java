package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import com.taozeyu.taolan.analysis.AnalysisNode;

public abstract class IfElseNode extends AnalysisNode {

    public List<ExpressionNode> conditionExpressionList = new LinkedList<>();
    public List<ChunkNode> conditionChuckList = new LinkedList<>();
    public ChunkNode elseChuck = null;

    protected void addExpression(ExpressionNode node) {
        conditionExpressionList.add(node);
        conditionChuckList.add(null);
    }

    protected void addChunk(ChunkNode node) {
        conditionChuckList.set(conditionChuckList.size() - 1, node);
    }

    @Override
    public void print(int retractNum, PrintStream out) {
        for(int i=0; i<conditionExpressionList.size(); ++i) {
            if(i == 0) {
                out.print("if ");
            } else {
                printRetract(retractNum, out);
                out.print("elsif ");
            }
            conditionExpressionList.get(i).print(retractNum, out);
            out.print("\n");
            if(conditionChuckList.get(i) != null) {
                conditionChuckList.get(i).print(retractNum + 1, out);
            }
        }
        if(elseChuck != null) {
            printRetract(retractNum, out);
            out.print("else\n");
            elseChuck.print(retractNum + 1, out);
        }
        printRetract(retractNum, out);
        out.print("end");
    }
}
