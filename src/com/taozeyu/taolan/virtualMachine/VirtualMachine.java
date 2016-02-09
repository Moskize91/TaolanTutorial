package com.taozeyu.taolan.virtualMachine;

import com.taozeyu.taolan.analysis.LexicalAnalysis;
import com.taozeyu.taolan.analysis.LexicalAnalysisException;
import com.taozeyu.taolan.analysis.SyntacticAnalysis;
import com.taozeyu.taolan.analysis.SyntacticAnalysisException;
import com.taozeyu.taolan.analysis.node.ChunkNode;
import com.taozeyu.taolan.intermediatecode.CodeChunk;
import com.taozeyu.taolan.intermediatecode.IntermediateCodeCreator;
import com.taozeyu.taolan.intermediatecode.IntermediateCodeExpression;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class VirtualMachine {

    private final List<CodeChunk> functionBodyTable = new ArrayList<>();

    private static class SingletonHelper {
        private static final VirtualMachine instance = new VirtualMachine();

        static {
            try {
                instance.loadAllSystemLib();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static VirtualMachine instance() {
        return SingletonHelper.instance;
    }

    private VirtualMachine() {
        TaolanNativeObject.bindAllNativeObjects();
    }

    public Value run(Reader reader) throws SyntacticAnalysisException, LexicalAnalysisException, IOException, IntermediateCodeExpression {
        int functionStartIndex = hasRegisteredFunctionCount();
        IntermediateCodeCreator.Result result = getIntermediateCodeFromReader(functionStartIndex, reader);
        functionBodyTable.addAll(result.functionBodyTable);
        return runCodeChunk(result.targetCode);
    }

    public boolean isNativeFunction(int functionId) {
        return functionId >= 0 && functionId < NativeFunction.nativeFunctionCount();
    }

    public boolean isCustomFunction(int functionId) {
        if (isNativeFunction(functionId)) {
            return false;
        }
        functionId -= NativeFunction.nativeFunctionCount();
        return functionId < functionBodyTable.size();
    }

    public CodeChunk getCustomFunctionBody(int functionId) {
        return functionBodyTable.get(functionId - NativeFunction.nativeFunctionCount());
    }

    private IntermediateCodeCreator.Result getIntermediateCodeFromReader(int functionStartIndex, Reader reader) throws SyntacticAnalysisException, LexicalAnalysisException, IOException, IntermediateCodeExpression {
        LexicalAnalysis la = new LexicalAnalysis(reader);
        SyntacticAnalysis sa = new SyntacticAnalysis(la);
        ChunkNode node = sa.analyze();
        return new IntermediateCodeCreator(functionStartIndex).create(node);
    }

    private int hasRegisteredFunctionCount() {
        return NativeFunction.nativeFunctionCount() + functionBodyTable.size();
    }

    private void loadAllSystemLib() throws IOException, SyntacticAnalysisException, IntermediateCodeExpression, LexicalAnalysisException {
        Charset systemLibCharset = Charset.forName("UTF-8");
        File libDirectory = new File(System.getProperty("user.dir"), "src/system_lib");
        for (File systemLibFile : libDirectory.listFiles()) {
            if (systemLibFile.getName().matches("(\\w|\\-|_)+\\.t$")) {//扩展名是 .t 的文件
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(systemLibFile), systemLibCharset));
                try {
                    run(reader);
                } finally {
                    reader.close();
                }
            }
        }
    }

    private Value runCodeChunk(CodeChunk codeChunk) {
        Runtime runtime = new Runtime(System.in, System.out);
        Value result = runtime.run(codeChunk);
        if (runtime.isCatchError()) {
            String message = result.objValue.getProperty("message").strValue;
            throw new RuntimeException("exist with unhandled error: "+ message);
        }
        return result;
    }

}
