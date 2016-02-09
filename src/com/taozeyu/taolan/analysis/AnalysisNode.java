package com.taozeyu.taolan.analysis;

import java.io.PrintStream;
import java.util.Iterator;

import com.taozeyu.taolan.analysis.NonTerminalSymbol.Exp;

public abstract class AnalysisNode {

    private Exp exp = null;

    public abstract void match(AnalysisNode analysisNode) throws SyntacticAnalysisException;

    public abstract void match(TerminalSymbol token) throws SyntacticAnalysisException;

    public abstract void print(int retractNum, PrintStream out);

    public void finish() throws SyntacticAnalysisException {}

    public void printRetract(int retractNum, PrintStream out) {
        for(int i=0; i<retractNum; ++i) {
            out.print("    ");
        }
    }

    public void printParams(int retractNum, PrintStream out, Iterable<?> container) {
        Iterator<?> it = container.iterator();
        while(it.hasNext()) {
            Object target = it.next();
            if(target instanceof AnalysisNode) {
                ((AnalysisNode) target).print(retractNum, out);
            } else {
                out.print(target);
            }
            if(it.hasNext()) {
                out.print(", ");
            }
        }
    }

    Exp getExp() {
        return exp;
    }

    void setExp(Exp exp) {
        this.exp = exp;
    }
}
