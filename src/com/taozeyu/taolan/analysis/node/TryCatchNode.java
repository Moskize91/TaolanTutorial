package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import com.taozeyu.taolan.analysis.AnalysisNode;

public abstract class TryCatchNode extends AnalysisNode {

    public ChunkNode tryChunk;
    public ChunkNode finallyChunk = null;
    public List<String> errorNameList = new LinkedList<>();
    public List<ExpressionNode> typeList = new LinkedList<>();
    public List<ChunkNode> catchChunkList = new LinkedList<>();

    protected void addErrorName(String errorName) {
        errorNameList.add(errorName);
        typeList.add(null);
        catchChunkList.add(null);
    }

    protected void addErrorType(ExpressionNode type) {
        typeList.set(typeList.size() - 1, type);
    }

    protected void addChunk(ChunkNode chunk) {
        catchChunkList.set(catchChunkList.size() - 1, chunk);
    }

    @Override
    public void print(int retractNum, PrintStream out) {
        if(tryChunk != null) {
            out.print("try\n");
            tryChunk.print(retractNum + 1, out);
        }
        for(int i=0; i<catchChunkList.size(); ++i)
        {
            printRetract(retractNum, out);
            out.print("catch ");
            out.print(errorNameList.get(i));
            if(typeList.get(i) != null) {
                out.print(" <- ");
                typeList.get(i).print(retractNum + 1, out);
            }
            out.print("\n");
            if(catchChunkList.get(i) != null) {
                catchChunkList.get(i).print(retractNum + 1, out);
            }
        }
        if(finallyChunk != null) {
            printRetract(retractNum, out);
            out.print("finally\n");
            finallyChunk.print(retractNum + 1, out);
        }
        printRetract(retractNum, out);
        out.print("end");
    }
}
