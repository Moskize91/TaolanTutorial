package com.taozeyu.taolan.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import com.taozeyu.taolan.analysis.NonTerminalSymbol.Exp;

class FirstSetConstructor {

    private final HashMap<Exp, NonTerminalSymbol> expContainer;

    private LinkedHashSet<NonTerminalSymbol> visitedExpSet = null;
    private HashSet<NonTerminalSymbol> hasBuildExpSet = null;

    FirstSetConstructor(HashMap<Exp, NonTerminalSymbol> expContainer) {
        this.expContainer = expContainer;
    }

    void build() {
        visitedExpSet = new LinkedHashSet<>();
        hasBuildExpSet = new HashSet<>();
        for(NonTerminalSymbol node:searchAllNonTerminalSymbols()) {
            buildFirstSetIfNeed(node);
        }
    }

    private HashSet<NonTerminalSymbol> searchAllNonTerminalSymbols() {
        HashSet<NonTerminalSymbol> container = new HashSet<>();
        for(NonTerminalSymbol node:expContainer.values()) {
            searchNonTerminalSymbols(container, node);
        }
        return container;
    }

    private void searchNonTerminalSymbols(HashSet<NonTerminalSymbol> container, NonTerminalSymbol node) {
        boolean notAdded = container.add(node);
        if(notAdded) {
            for(Object[] expansion:node.expansionList) {
                for(Object obj:expansion) {
                    if(obj instanceof NonTerminalSymbol) {
                        NonTerminalSymbol childNode = (NonTerminalSymbol) obj;
                        searchNonTerminalSymbols(container, childNode);
                    }
                }
            }
        }
    }

    private void buildFirstSetIfNeed(NonTerminalSymbol node) {
        boolean success = visitedExpSet.add(node);
        if(!success) {
            printLeftRecursiveErrorMessage(node);
            throw new RuntimeException("left recursive");
        }
        boolean needHandle = hasBuildExpSet.add(node);
        if(needHandle) {
            buildFirstSetHandle(node);
        }
        visitedExpSet.remove(node);
    }

    private void printLeftRecursiveErrorMessage(NonTerminalSymbol node) {
        System.err.println("non-terminal symbol " + node.exp + " has left recursive.");
        for(NonTerminalSymbol childNode:visitedExpSet) {
            System.err.println(" --> " + childNode.exp);
        }
    }

    private void buildFirstSetHandle(NonTerminalSymbol node) {
        collectAllFirstSetTokens(node);
        removeBannedFirstSetTokens(node);
        checkAmbiguity(node);
        handleSigns(node);
    }

    private void collectAllFirstSetTokens(NonTerminalSymbol node) {
        for(Object[] expansion:node.expansionList) {
            HashSet<TerminalSymbol> firstSet = getExpansionFirstSet(expansion);
            node.firstSetList.add(firstSet);
            node.firstSet.addAll(firstSet);
        }
    }

    private void removeBannedFirstSetTokens(NonTerminalSymbol node) {
        for(TerminalSymbol banToken:node.banList) {
            node.firstSet.remove(banToken);
        }
    }

    private HashSet<TerminalSymbol> getExpansionFirstSet(Object[] expansion) {
        HashSet<TerminalSymbol> firstSet = new HashSet<>();
        boolean thisExpansionCanBeEmpty = true;
        for(Object obj:expansion) {
            NonTerminalSymbol node = tryGetNonTerminalSymbol(obj);
            boolean willContinue = false;
            if(node != null) {
                buildFirstSetIfNeed(node);
                // 非终结符的 first set 包含空，说明该非终结符可以展开为空。
                if(node.firstSet.contains(TerminalSymbol.Empty)) {
                    willContinue = true;
                } else {
                    thisExpansionCanBeEmpty = false;
                }
                for(TerminalSymbol token:node.firstSet) {
                    if(!token.isEmpty()) {
                        firstSet.add(token);
                    }
                }
            } else {
                TerminalSymbol token = (TerminalSymbol) obj;
                firstSet.add(token);
                thisExpansionCanBeEmpty = false;
            }
            if(!willContinue) {
                break;
            }
        }
        if(thisExpansionCanBeEmpty) {
            // 说明该展开式不包含任何终结符，并且其每一个非终结符都可以展开为空。
            firstSet.add(TerminalSymbol.Empty);
        }
        return firstSet;
    }

    private NonTerminalSymbol tryGetNonTerminalSymbol(Object obj) {
        NonTerminalSymbol node = null;
        if(obj instanceof NonTerminalSymbol) {
            node = (NonTerminalSymbol) obj;

        } else if(obj instanceof Exp){
            Exp exp = (Exp) obj;
            node = expContainer.get(exp);
        }
        return node;
    }

    private void checkAmbiguity(NonTerminalSymbol node) {
        HashSet<TerminalSymbol> union = new HashSet<>();
        boolean foundAnyConflicts = false;
        for(HashSet<TerminalSymbol> firstSet:node.firstSetList) {
            for(TerminalSymbol token:firstSet) {
                if(!token.isEmpty() && !union.add(token)) {
                    foundAnyConflicts = true;
                    break;
                }
            }
        }
        if(foundAnyConflicts) {
            printAmbiguityMessage(node);
            throw new RuntimeException("ambiguity");
        }
    }

    private void printAmbiguityMessage(NonTerminalSymbol node) {
        Object info = node.exp;
        if(info == null) {
            Exp exp = getTopExpFromVisitedSet();
            info = "" + exp + "'s node";
        }
        System.err.println("non-terminal symbol " + info + " has " + node.expansionList.size() + " expansions");
        for(int i=0; i<node.expansionList.size(); ++i) {
            System.err.println("expansion " + (i + 1) + " :");
            System.err.println(node.firstSetList.get(i));
        }
        System.err.println("first Set:");
        System.err.println(node.firstSet);
        if(!node.banList.isEmpty()) {
            System.err.println("ban list:");
            System.err.println(node.banList);
        }
        System.err.println();
    }

    private Exp getTopExpFromVisitedSet() {
        for (NonTerminalSymbol aVisitedExpSet : visitedExpSet) {
            Exp getExp = aVisitedExpSet.exp;
            if (getExp != null) {
                return getExp;
            }
        }
        return null;
    }

    private void handleSigns(NonTerminalSymbol node) {

        boolean canBeEmpty = false;
        for(HashSet<TerminalSymbol> firstSet:node.firstSetList) {
            // Empty 终结符是一个临时符号，仅仅存在于 first set constructor 的 build 阶段。
            // 这行代码既检测了空符的存在，又删除了 expansionList 空符。
            canBeEmpty |= firstSet.remove(TerminalSymbol.Empty);
        }

        if(canBeEmpty) {
            if(node.sign == null) {
                node.sign = '?';
            } else if(node.sign == '+') {
                node.sign = '*';
            }
        }
        if(node.sign != null && (node.sign == '?' || node.sign == '*')) {
            node.firstSet.add(TerminalSymbol.Empty);
        }
    }
}
