package com.taozeyu.taolan.intermediatecode;

class JumpStack {

    private Node topNode = null;

    void push(int breakLocation, int continueLocation) {
        Node node = new Node();
        node.breakLocation = breakLocation;
        node.continueLocation = continueLocation;
        node.next = topNode;
        topNode = node;
    }

    void pop() {
        topNode = topNode.next;
    }

    boolean isNotInLoop() {
        return topNode == null;
    }

    int getCurrentBreakLocation() {
        return topNode.breakLocation;
    }

    int getCurrentContinueLocation() {
        return topNode.continueLocation;
    }

    void setCurrentBreakLocation(int currentBreakLocation) {
        this.topNode.breakLocation = currentBreakLocation;
    }

    void setCurrentContinueLocation(int currentContinueLocation) {
        this.topNode.continueLocation = currentContinueLocation;
    }

    private static class Node {
        private int breakLocation = -1, continueLocation = -1;
        private Node next;
    }
}
