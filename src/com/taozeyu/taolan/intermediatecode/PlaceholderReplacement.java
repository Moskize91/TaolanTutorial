package com.taozeyu.taolan.intermediatecode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taozeyu.taolan.intermediatecode.CodeChunk.Command;

class PlaceholderReplacement {

    final static PlaceholderReplacement instance = new PlaceholderReplacement();

    private static final Map<Command, int[]> commandReplaceIndexMap = new HashMap<>();

    static {
        define(Command.Jump, 1);
        define(Command.JumpWhen, 1);
        define(Command.JumpUnless, 1);
        define(Command.PushTryBlock, 1, 2);
    }

    static class RegisterNode {
        CodeChunk codeChunk;
        PositionPlaceholder holder;
    }

    private static void define(Command command, int...replaceIndexes) {
        commandReplaceIndexMap.put(command, replaceIndexes);
    }

    RegisterNode register(CodeChunk codeChunk, PositionPlaceholder holder) {
        RegisterNode node = new RegisterNode();
        node.codeChunk = codeChunk;
        node.holder = holder;
        return node;
    }

    void handle(List<RegisterNode> list) {
        for(RegisterNode node:list) {
            handle(node.codeChunk, node.holder);
        }
    }

    private void handle(CodeChunk codeChunk, PositionPlaceholder holder) {
        for(CodeChunk.Code code:codeChunk) {
            int[] replaceIndexes = commandReplaceIndexMap.get(code.command);
            if (replaceIndexes != null) {
                for (int replaceIndex : replaceIndexes) {
                    replace(code, replaceIndex, holder);
                }
            }
        }
    }

    private void replace(CodeChunk.Code code, int replaceIndex, PositionPlaceholder holder) {
        switch(replaceIndex) {
        case 1:
            code.number1 = checkAndReplace(code.number1, holder);
            break;
        case 2:
            code.number2 = checkAndReplace(code.number2, holder);
            break;
        case 3:
            code.number3 = checkAndReplace(code.number3, holder);
            break;
        default:
            throw new RuntimeException("unknow replaceIndex " + replaceIndex);
        }
    }

    private int checkAndReplace(int position, PositionPlaceholder holder) {
        if(position >= 0) {
            return position;
        } else {
            return holder.getPosition(position);
        }
    }
}
