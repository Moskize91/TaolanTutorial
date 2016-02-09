package com.taozeyu.taolan.virtualMachine;

import com.taozeyu.taolan.intermediatecode.CodeChunk;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Value {

    public enum Type {
        Integer, Number, Boolean, String, Object, Null,
    }

    private static final Map<CodeChunk.ImmediateType, Type> typeMap = new EnumMap<CodeChunk.ImmediateType, Type>(CodeChunk.ImmediateType.class) {{
        put(CodeChunk.ImmediateType.Integer, Type.Integer);
        put(CodeChunk.ImmediateType.Number, Type.Number);
        put(CodeChunk.ImmediateType.Boolean, Type.Boolean);
        put(CodeChunk.ImmediateType.String, Type.String);
        put(CodeChunk.ImmediateType.Null, Type.Null);
    }};

    public static final Value NULL = new Value(Type.Null, 0, 0.0, false, null, null);

    public final Type type;
    public final int intValue;
    public final double numValue;
    public final boolean bolValue;
    public final String strValue;
    public final TaolanObject objValue;

    private Value(Type type, int intValue, double numValue, boolean bolValue, String strValue, TaolanObject objValue) {
        this.type = type;
        this.intValue = intValue;
        this.numValue = numValue;
        this.bolValue = bolValue;
        this.strValue = strValue;
        this.objValue = objValue;
    }

    public Value(CodeChunk.ImmediateNumber immediateNumber) {
        this(typeMap.get(immediateNumber.getType()), immediateNumber.getIntegerValue(),
             immediateNumber.getNumberValue(), immediateNumber.isBooleanValue(), immediateNumber.getStringValue(), null);
    }

    public Value(int value) {
        this(Type.Integer, value, 0.0, false, null, null);
    }

    public Value(double value) {
        this(Type.Number, 0, value, false, null, null);
    }

    public Value(boolean value) {
        this(Type.Boolean, 0, 0.0, value, null, null);
    }

    public Value(String str) {
        this(str == null? Type.Null: Type.String, 0, 0.0, false, str, null);
    }

    public Value(TaolanObject objValue) { this(Type.Object, 0, 0.0, false, null, objValue); }

    public boolean convertToBoolean() {
        return this.type == Type.Boolean? bolValue: this.type != Type.Null;
    }

    public boolean isInstanceOf(Value other) {
        if (this.type != Type.Object || other.type != Type.Object) {
            return false;
        }
        TaolanObject thisObj = this.objValue;
        TaolanObject otherObj = other.objValue;

        while (thisObj != null) {
            if (thisObj == otherObj) {
                return true;
            }
            thisObj = thisObj.getPrototype();
        }
        return false;
    }

    public boolean isSameTypeOf(Value other) {
        if (this.type != Type.Object || other.type != Type.Object) {
            return false;
        }
        TaolanObject thisObj = this.objValue;
        TaolanObject otherObj = other.objValue;

        return thisObj.getConstructor() == otherObj;
    }

    public static Value add(Value v1, Value v2) {
        if (v1.type == Type.String) {
            return new Value(v1.strValue + v2.toString());
        }
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue + v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue + v2.numValue);
            }
        }
        return null;
    }

    public static Value sub(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue - v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue - v2.numValue);
            }
        }
        return null;
    }

    public static Value mul(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue * v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue * v2.numValue);
            }
        }
        return null;
    }

    public static Value div(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue / v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue / v2.numValue);
            }
        }
        return null;
    }

    public static Value gt(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue > v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue > v2.numValue);
            }
        }
        return null;
    }

    public static Value lt(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue < v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue < v2.numValue);
            }
        }
        return null;
    }

    public static Value gte(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue >= v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue >= v2.numValue);
            }
        }
        return null;
    }

    public static Value lte(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue <= v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue <= v2.numValue);
            }
        }
        return null;
    }

    public static Value checkEquals(Value v1, Value v2) {
        v1 = convertNumberToHeightTypeLevel(v1, v2);
        v2 = convertNumberToHeightTypeLevel(v2, v1);

        if (v1.type == v2.type) {
            if (v1.type == Type.Integer) {
                return new Value(v1.intValue == v2.intValue);
            } else if (v1.type == Type.Number) {
                return new Value(v1.numValue == v2.numValue);
            } else if (v1.type == Type.Boolean) {
                return new Value(v1.bolValue == v2.bolValue);
            } else if (v1.type == Type.String) {
                return new Value(v1.strValue.equals(v2.strValue));
            } else if (v1.type == Type.Object) {
                List<Value> params = new LinkedList<>();
                params.add(v2);
                return NativeFunction.Equals.invoke(v1.objValue, params);
            } else if (v1.type == Type.Null) {
                return new Value(true);
            }
        } else {
            return new Value(false);
        }
        return null;
    }

    public static Value checkNotEquals(Value v1, Value v2) {
        return new Value(!checkEquals(v1, v2).bolValue);
    }

    public static Value convertNumberToHeightTypeLevel(Value convertedValue, Value comparedValue) {
        if (convertedValue.type == Type.Integer && comparedValue.type == Type.Number) {
            convertedValue = new Value((double)convertedValue.intValue);
        }
        return convertedValue;
    }

    @Override
    public String toString() {
        if (type == Type.Integer) {
            return String.valueOf(intValue);
        } else if (type == Type.Number) {
            return String.valueOf(numValue);
        } else if (type == Type.Boolean) {
            return String.valueOf(bolValue);
        } else if (type == Type.String) {
            return strValue;
        } else if (type == Type.Object) {
            return objValue.toString();
        } else if (type == Type.Null) {
            return "NULL";
        }
        throw new RuntimeException();
    }
}
