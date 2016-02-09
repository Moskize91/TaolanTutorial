package com.taozeyu.taolan.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.taozeyu.taolan.analysis.Token.Type;

public class LexicalAnalysis {

    private static enum State {
        Normal, Identifier, Sign, Annotation, String, RegEx, Space;
    }

    private static final char[] FilterChar = new char[] {
        '\b', '\f', '\r'
    };
    private static final char[] IdentifierSign = new char[] {'_'};
    private static final char[] IdentifierRearSign = new char[] {'?', '!'};

    private static final char[] Space = new char[] {' ', '\t'};

    private static final HashMap<Character, Character> StringTMMap = new HashMap<>();

    static {
        StringTMMap.put('\"', '\"');
        StringTMMap.put('\'', '\'');
        StringTMMap.put('\\', '\\');
        StringTMMap.put('b', '\b');
        StringTMMap.put('f', '\f');
        StringTMMap.put('t', '\t');
        StringTMMap.put('r', '\r');
        StringTMMap.put('n', '\n');
    }

    private final Reader reader;

    private Token endToken = null;
    private final LinkedList<Token> tokenBuffer = new LinkedList<>();

    private State state;
    private StringBuilder readBuffer = null;
    private boolean transferredMeaningSign = false;

    public LexicalAnalysis(Reader reader) {
        this.reader = reader;
        this.state = State.Normal;
    }

    Token read() throws IOException, LexicalAnalysisException {

        if(endToken != null) {
            return endToken;
        }
        while(tokenBuffer.isEmpty()) {
            int read = reader.read();
            char c = (read == -1 ? '\0' : (char) read);
            while(!readChar(c)) {}
        }
        Token token = tokenBuffer.removeLast();
        if(token.type == Type.EndSymbol) {
            endToken = token;
        }
        return token;
    }

    private void refreshBuffer(char c) {
        readBuffer = new StringBuilder();
        readBuffer.append(c);
    }

    private void createToken(Type type) {
        Token token = new Token(type, readBuffer.toString());
        tokenBuffer.addFirst(token);
        readBuffer = null;
    }

    private void createToken(Type type, String value) {
        Token token = new Token(type, value);
        tokenBuffer.addFirst(token);
        readBuffer = null;
    }

    private boolean readChar(char c) throws LexicalAnalysisException {

        boolean moveCursor = true;
        Type createType = null;

        if(!include(FilterChar, c)) {

            if(state == State.Normal) {

                if(inIdentifierSetButNotRear(c)) {
                    state = State.Identifier;
                }
                else if(SignParser.inCharSet(c)) {
                    state = State.Sign;
                }
                else if(c == '#') {
                    state = State.Annotation;
                }
                else if(c == '\"' | c == '\'') {
                    state = State.String;
                    transferredMeaningSign = false;
                }
                else if(c == '`') {
                    state = State.RegEx;
                    transferredMeaningSign = false;
                }
                else if(include(Space, c)) {
                    state = State.Space;
                }
                else if(c == '\n') {
                    createType = Type.NewLine;
                }
                else if(c == '\0') {
                    createType = Type.EndSymbol;
                }
                else {
                    throw new LexicalAnalysisException(c);
                }
                refreshBuffer(c);

            } else if(state == State.Identifier) {

                if(inIdentifierSetButNotRear(c)) {
                    readBuffer.append(c);

                } else if(include(IdentifierRearSign, c)) {
                    createType = Type.Identifier;
                    readBuffer.append(c);
                    state = State.Normal;

                } else {
                    createType = Type.Identifier;
                    state = State.Normal;
                    moveCursor = false;
                }

            } else if(state == State.Sign) {

                if(SignParser.inCharSet(c)) {
                    readBuffer.append(c);

                } else {
                    List<String> list = SignParser.parse(readBuffer.toString());
                    for(String signStr:list) {
                        createToken(Type.Sign, signStr);
                    }
                    createType = null;
                    state = State.Normal;
                    moveCursor = false;
                }

            } else if(state == State.Annotation) {

                if(c != '\n' & c != '\0') {
                    readBuffer.append(c);

                } else {
                    createType = Type.Annotation;
                    state = State.Normal;
                    moveCursor = false;

                }
            } else if(state == State.String) {

                if(c == '\n') {
                    throw new LexicalAnalysisException(c);

                } else if(c == '\0') {
                    throw new LexicalAnalysisException(c);

                } else if(transferredMeaningSign) {

                    Character tms = StringTMMap.get(c);
                    if(tms == null) {
                        throw new LexicalAnalysisException(c);
                    }
                    readBuffer.append(tms);
                    transferredMeaningSign = false;

                } else if(c == '\\') {
                    transferredMeaningSign = true;

                } else {
                    readBuffer.append(c);
                    char firstChar = readBuffer.charAt(0);
                    if(firstChar == c) {
                        createType = Type.String;
                        state = State.Normal;
                    }
                }
            } else if(state == State.RegEx) {

                if(transferredMeaningSign) {

                    if(c != '`') {
                        throw new LexicalAnalysisException(c);
                    }
                    readBuffer.append(c);
                    transferredMeaningSign = false;

                } else if(c =='\\') {
                    transferredMeaningSign = true;

                } else if(c == '\0') {
                    throw new LexicalAnalysisException(c);

                } else if(c == '`') {
                    readBuffer.append(c);
                    createType = Type.RegEx;
                    state = State.Normal;

                } else {
                    readBuffer.append(c);
                }

             } else if(state == State.Space) {

                if(include(Space, c)) {
                    readBuffer.append(c);

                } else {
                    createType = Type.Space;
                    state = State.Normal;
                    moveCursor = false;
                }
            }
        }
        if(createType != null) {
            createToken(createType);
        }
        return moveCursor;
    }

    private boolean inIdentifierSetButNotRear(char c) {
        return (c >= 'a' & c <= 'z' ) | (c >='A' & c <= 'Z') | (c >= '0' & c <= '9')|| include(IdentifierSign, c);
    }

    private boolean include(char[] range, char c) {
        boolean include = false;
        for(int i=0; i<range.length; ++i) {
            if(range[i] == c) {
                include = true;
                break;
            }
        }
        return include;
    }
}
