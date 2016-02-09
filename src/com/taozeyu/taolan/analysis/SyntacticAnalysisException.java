package com.taozeyu.taolan.analysis;

public class SyntacticAnalysisException extends Exception {

    private static final long serialVersionUID = 5986954890480206628L;

    public SyntacticAnalysisException() {};

    public SyntacticAnalysisException(String msg) {
        super(msg);
    }

    public SyntacticAnalysisException(TerminalSymbol token) {
        super(token.toString());
    }
}
