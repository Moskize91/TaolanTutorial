package com.taozeyu.taolan.analysis;

import java.util.HashSet;

public class Token {

    private static final HashSet<String> keywordsSet = new HashSet<>();

    static {
        keywordsSet.add("true");
        keywordsSet.add("false");
        keywordsSet.add("if");
        keywordsSet.add("when");
        keywordsSet.add("elsif");
        keywordsSet.add("else");
        keywordsSet.add("while");
        keywordsSet.add("begin");
        keywordsSet.add("until");
        keywordsSet.add("for");
        keywordsSet.add("do");
        keywordsSet.add("try");
        keywordsSet.add("catch");
        keywordsSet.add("finally");
        keywordsSet.add("end");
        keywordsSet.add("def");
        keywordsSet.add("var");
        keywordsSet.add("this");
        keywordsSet.add("null");
        keywordsSet.add("throw");
        keywordsSet.add("break");
        keywordsSet.add("continue");
        keywordsSet.add("return");
        keywordsSet.add("operator");
        keywordsSet.add("instanceof");
        keywordsSet.add("is");
    }

    public static enum Type {
        Keyword, Number, Identifier, Sign, Annotation, String, RegEx, Space, NewLine, EndSymbol;
    }

    final Type type;
    final String value;

    Token(Type type, String value) {

        if(type == Type.Identifier) {
            char firstChar = value.charAt(0);
            if(firstChar >= '0' & firstChar < '9') {
                type = Type.Number;

            } else if(keywordsSet.contains(value)){
                type = Type.Keyword;
            }
        }
        else if(type == Type.Annotation) {
            value = value.substring(1);
        }
        else if(type == Type.String) {
            value = value.substring(1, value.length() - 1);
        }
        else if(type == Type.RegEx) {
            value = value.substring(1, value.length() - 1);
        }
        else if(type == Type.EndSymbol) {
            value = null;
        }
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s(%s) ", type, value);
    }
}
