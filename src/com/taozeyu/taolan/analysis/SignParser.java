package com.taozeyu.taolan.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

class SignParser {

    private final static List<HashSet<String>> signSetList;
    private final static HashSet<Character> signCharSet;
    private final static int MaxLength, MinLength;

    static {
        String[] signArray = new String[] {
            "+", "-", "*", "/", "%",
            ">", "<", ">=", "<=", "=", "!=", "==", "=~",
            "+=", "-=", "*=", "/=", "%=",
            "&&", "||", "!", "^",
            "&&=", "||=", "^=",
            "<<", ">>", "->", "<-",
            "?", ":",
            ".", ",", ";", "..",
            "(", ")", "[", "]", "{", "}", "|",
            "@", "@@", "$",
        };

        int maxLength = Integer.MIN_VALUE,
            minLength = Integer.MAX_VALUE;

        signCharSet = new HashSet<>();
        for(String sign:signArray) {
            int length = sign.length();
            if(length > maxLength) {
                maxLength = length;
            }
            if(length < minLength) {
                minLength = length;
            }
            for(int i=0; i<length; ++i) {
                signCharSet.add(sign.charAt(i));
            }
        }
        signSetList = new ArrayList<>(maxLength - minLength);
        for(int i=0; i< maxLength - minLength + 1; ++i) {
            signSetList.add(new HashSet<>());
        }
        for(String sign:signArray) {
            int length = sign.length();
            HashSet<String> signSet = signSetList.get(length - minLength);
            signSet.add(sign);
        }
        MaxLength = maxLength;
        MinLength = minLength;
    }

    static boolean inCharSet(char c) {
        return signCharSet.contains(c);
    }

    static List<String> parse(String str) throws LexicalAnalysisException {
        LinkedList<String> rsContainer = new LinkedList<>();
        int startIndex = 0;
        while(startIndex < str.length()) {
            String matchStr = match(startIndex, str);
            if(matchStr == null) {
                throw new LexicalAnalysisException(str.substring(startIndex));
            } else {
                rsContainer.add(matchStr);
                startIndex += matchStr.length();
            }
        }
        return rsContainer;
    }

    private static String match(int startIndex, String str) {
        String matchStr = null;
        int length = str.length() - startIndex;
        length = Math.min(length, MaxLength);
        if(length >= MinLength) {
            for(int i=length - MinLength; i>=0; i--) {
                int matchLength = i + MinLength;
                HashSet<String> signSet = signSetList.get(i);
                matchStr = str.substring(startIndex, startIndex + matchLength);
                if(signSet.contains(matchStr)) {
                    break;
                }
                matchStr = null;
            }
        }
        return matchStr;
    }
}
