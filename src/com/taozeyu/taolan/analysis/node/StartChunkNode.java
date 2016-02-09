package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;

import com.taozeyu.taolan.analysis.AnalysisNode;

public abstract class StartChunkNode extends AnalysisNode  {

    public ChunkNode chunk;

    @Override
    public void print(int retractNum, PrintStream out) {
        chunk.print(retractNum, out);
    }
}
