package com.taozeyu.taolan.intermediatecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CodeChunk implements Iterable<CodeChunk.Code>{

    private final List<Code> container = new ArrayList<>();

    public Code getCodeByLine(int line) {
        return container.get(line);
    }

    @Override
    public Iterator<Code> iterator() {
        return container.iterator();
    }

    public enum Command {
        Set, //私有变量->私有变量
        SetImmediate, //立即数->私有变量
        GetThis, //this->私有变量
        GetError, //error -> 私有变量
        Rel, //this变量->私有变量
        RelStatic, //constructor变量->私有变量
        Move, //私有变量->this变量
        MoveStatic, //私有变量->constructor变量

        Add, Sub, Mul, Div, Mod, Insert, Range,
        Gt, Gte, Lt, Lte, Equal, NotEqual, Match,
        And, Or, Not, Xor, Opposite,
        InstanceOf, Is,

        Jump, JumpWhen, JumpUnless,
        PushTryBlock, PopTryBlock, JumpFinally,
        Throw, Return,

        Push, Pop, Take, Clear,
        Invoke, InvokeVirtual, InvokeLambda,

        DefFunction,

        NewArray, NewContainer, NewRegEx,
    }

    public enum ImmediateType {
        Integer, Number, Boolean, String, Null,
    }

    public static class ImmediateNumber {
        ImmediateType type;
        int integerValue;
        float numberValue;
        boolean booleanValue;
        String stringValue;

        @Override
        public String toString() {
            if(type == ImmediateType.Boolean) {
                return String.valueOf(booleanValue);
            } else if(type == ImmediateType.Integer) {
                return String.valueOf(integerValue);
            } else if(type == ImmediateType.Number) {
                return String.valueOf(numberValue);
            } else if(type == ImmediateType.String) {
                return String.valueOf(stringValue);
            } else {
                return "NULL";
            }
        }

        public ImmediateType getType() {
            return type;
        }

        public int getIntegerValue() {
            return integerValue;
        }

        public float getNumberValue() {
            return numberValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public String getStringValue() {
            return stringValue;
        }
    }

    public static class Code {
        Command command;
        int number1;
        int number2;
        int number3;
        ImmediateNumber immediateNumber;

        @Override
        public String toString() {
            String str = String.format("%s    %s, %s, %s", command, number1, number2, number3);
            if(immediateNumber != null) {
                if (immediateNumber.type == ImmediateType.String) {
                    str += "    >> \"" + immediateNumber+"\"";
                } else {
                    str += "    >> " + immediateNumber;
                }
            }
            return str;
        }

        public Command getCommand() {
            return command;
        }

        public int getNumber1() {
            return number1;
        }

        public int getNumber2() {
            return number2;
        }

        public int getNumber3() {
            return number3;
        }

        public ImmediateNumber getImmediateNumber() {
            return immediateNumber;
        }
    }

    int getCurrentPostion() {
        return container.size();
    }

    void push(Command command, int number1, int number2, int number3, ImmediateNumber immediateNumber) {
        Code code = new Code();
        code.command = command;
        code.number1 = number1;
        code.number2 = number2;
        code.number3 = number3;
        code.immediateNumber = immediateNumber;
        container.add(code);
    }

    void push(Command command, int number1, int number2, int number3) {
        push(command, number1, number2, number3, null);
    }

    void push(Command command, int number1, int number2) {
        push(command, number1, number2, 0, null);
    }

    void push(Command command, int number1) {
        push(command, number1, 0, 0, null);
    }

    void push(Command command) {
        push(command, 0, 0, 0, null);
    }

    void push(Command command, int number1, ImmediateNumber immediateNumber) {
        push(command, number1, 0, 0, immediateNumber);
    }

    void push(Code code) {
        container.add(code);
    }

    @Override
    public String toString() {
        String str = "";
        int lineNumber = 0;
        for(Code code:container) {
            str += lineNumber + "\t";
            str += code + "\n";
            lineNumber++;
        }
        return str;
    }
}
