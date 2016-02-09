package com.taozeyu.taolan.analysis.node;

import java.io.PrintStream;

import com.taozeyu.taolan.analysis.AnalysisNode;


public abstract class ElementNode extends AnalysisNode {

    public enum ElementType {
        Integer, Number, Variable, String, RegEx, Array, Container, This, Null, Boolean,
    }

    public ElementType type;
    public String value;

    public boolean fromThis = false;
    public boolean fromConstructor = false;

    public ArrayNode array = null;
    public ContainerNode container = null;

    public boolean isLocalVariable() {
        return type == ElementType.Variable && !fromConstructor && !fromThis;
    }

    public boolean isAttributeVariable() {
        return type == ElementType.Variable && (fromConstructor || fromThis);
    }

    @Override
    public void print(int retractNum, PrintStream out) {
        if(type == ElementType.Integer) {
            out.print(value);

        } else if(type == ElementType.Number) {
            out.print(value);

        } else if(type == ElementType.Variable) {
            if(fromConstructor) {
                out.print("@@");
            } else if(fromThis) {
                out.print("@");
            }
            out.print(value);

        } else if(type == ElementType.String) {
            out.print("\"");
            out.print(value);
            out.print("\"");

        } else if(type == ElementType.RegEx) {
            out.print("`");
            out.print(value);
            out.print("`");

        } else if(type == ElementType.Array) {
            array.print(retractNum, out);

        } else if(type == ElementType.Container) {
            container.print(retractNum, out);

        } else if(type == ElementType.This) {
            out.print("this");

        } else if(type == ElementType.Null) {
            out.print("null");
        } else {
            out.print(value);
        }
    }
}
