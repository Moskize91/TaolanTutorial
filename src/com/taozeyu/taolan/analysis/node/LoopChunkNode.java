package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;

import com.taozeyu.taolan.analysis.AnalysisNode;


public abstract class LoopChunkNode extends AnalysisNode {

    public ExpressionNode condition;
    public boolean isWhile;
    public ChunkNode chunk;

    @Override
    public void print(int retractNum, PrintStream out) {
        if(isWhile) {
            out.print("while ");
            condition.print(retractNum, out);
            out.print(" do\n");
            if(chunk != null) {
                chunk.print(retractNum + 1, out);
            }
            printRetract(retractNum, out);
            out.print("end");
        }
        else {
            out.print("begin");
            out.print("\n");
            if(chunk != null) {
                chunk.print(retractNum + 1, out);
            }
            printRetract(retractNum, out);
            out.print("until ");
            condition.print(retractNum, out);
        }
    }
}
