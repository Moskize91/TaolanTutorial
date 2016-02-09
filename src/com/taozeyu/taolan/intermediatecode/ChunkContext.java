package com.taozeyu.taolan.intermediatecode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ChunkContext {

    int functionStartIndex;

    List<PlaceholderReplacement.RegisterNode> placeholderRegisterList;
    Map<String, Integer> functionIDMapper;

    VariableRecorder variableRecorder;
    FunctionRecorder functionRecorder;

    CodeChunk codeChunk;
    JumpStack jumpStack;

    LocalVariablePool variablePool;
    PositionPlaceholder positionPlaceholder;

    private ChunkContext(ChunkContext parent) {
        functionStartIndex = parent.functionStartIndex;
        placeholderRegisterList = parent.placeholderRegisterList;
        functionRecorder = parent.functionRecorder;
        functionIDMapper = parent.functionIDMapper;
    }

    ChunkContext(int _functionStartIndex) {
        functionStartIndex = _functionStartIndex;
        placeholderRegisterList = new LinkedList<>();
        functionIDMapper = new HashMap<>();
        variableRecorder = new VariableRecorder();
        functionRecorder = new FunctionRecorder();
        codeChunk = new CodeChunk();
        jumpStack = new JumpStack();
        variablePool = new LocalVariablePool();
        positionPlaceholder = new PositionPlaceholder();
    }

    ChunkContext link() {
        ChunkContext context = new ChunkContext(this);
        context.codeChunk = this.codeChunk;
        context.variablePool = this.variablePool;
        context.positionPlaceholder = this.positionPlaceholder;
        context.jumpStack = this.jumpStack;
        context.variableRecorder = this.variableRecorder.link();
        return context;
    }

    ChunkContext extend() {
        ChunkContext context = new ChunkContext(this);
        context.codeChunk = new CodeChunk();
        context.variablePool = new LocalVariablePool();
        context.positionPlaceholder = new PositionPlaceholder();
        context.jumpStack = new JumpStack();
        context.variableRecorder = new VariableRecorder();
        return context;
    }
}
