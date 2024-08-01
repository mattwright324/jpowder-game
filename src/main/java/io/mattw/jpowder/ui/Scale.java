package io.mattw.jpowder.ui;

import lombok.Getter;

@Getter
public enum Scale {
    SMALL(1),
    LARGE(2),
    TOGGLE(-1);

    private final int scale;

    Scale(int scale) {
        this.scale = scale;
    }
}
