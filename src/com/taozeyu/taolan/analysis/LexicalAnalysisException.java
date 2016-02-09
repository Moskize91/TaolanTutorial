package com.taozeyu.taolan.analysis;

public class LexicalAnalysisException extends Exception{

    private static final long serialVersionUID = -7651451345797000442L;

    public LexicalAnalysisException(char c) {
        super("unexpected '" + c + "'");
    }

    public LexicalAnalysisException(String msg) {
        super("unexpected \"" + msg + "\"");
    }
}
