package com.taozeyu.taolan.analysis;

import java.util.HashSet;

import com.taozeyu.taolan.analysis.Token.Type;

public class TerminalSymbol {

    @SuppressWarnings("serial")
    private final static HashSet<Type> careValueTypeSet = new HashSet<Type>() {{
        add(Type.Keyword);
        add(Type.Sign);
    }};

    static final TerminalSymbol Empty = new TerminalSymbol(null, null);

    public final Type type;
    public final String value;
    final boolean careValue;

    TerminalSymbol(Type type, String value) {
        this.type = type;
        this.value = value;
        this.careValue = careValueTypeSet.contains(type);
    }

    boolean isEmpty() {
        return this.type == null;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEquals = false;
        if(obj instanceof TerminalSymbol) {
            TerminalSymbol other = (TerminalSymbol) obj;
            isEquals = isEquals(this.type, other.type);
            if(isEquals & careValue) {
                isEquals = isEquals(this.value, other.value);
            }
        }
        return isEquals;
    }

    private boolean isEquals(Object o1, Object o2) {
        boolean isEquals = false;
        if(o1 == null & o2 == null) {
            isEquals = true;
        } else if(o1 != null & o2 != null) {
            isEquals = o1.equals(o2);
        }
        return isEquals;
    }

    @Override
    public int hashCode() {
        int hashCode = getHashCode(this.type);
        if(careValue) {
            hashCode ^= getHashCode(this.value);
        }
        return hashCode;
    }

    private int getHashCode(Object obj) {
        int hashCode = 0;
        if(obj != null) {
            hashCode = obj.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        String str;
        if(this.value != null) {
            str = " “" + this.value + "”";
        } else {
            if(this.type != null) {
                str = this.type.toString();
            } else {
                str = "ε";
            }
        }
        return str;
    }
}
