package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;
import java.util.*;

import com.taozeyu.taolan.analysis.AnalysisNode;


public abstract class ChunkNode extends AnalysisNode {

    public List<AnalysisNode> lineList = new LinkedList<>();

    @Override
    public void print(int retractNum, PrintStream out) {
        for(AnalysisNode line: lineList) {
            printRetract(retractNum, out);
            line.print(retractNum, out);
            out.println();
        }
    }
}
