package com.taozeyu.taolan.intermediatecode;

import java.util.HashMap;
import java.util.Map;

class PositionPlaceholder {

    private int nextPosition = -1;
    private Map<Integer, Integer> placeholderMapper = new HashMap<>();

    int createPosition() {
        int position = nextPosition;
        nextPosition--;
        return position;
    }

    void setPosition(int placeholder, int position) {
        placeholderMapper.put(placeholder, position);
    }

    int getPosition(int placeholder) {
        return placeholderMapper.get(placeholder);
    }
}
