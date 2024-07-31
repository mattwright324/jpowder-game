package io.mattw.jpowder.game;

import lombok.Getter;

@Getter
public enum ViewType {
    DEFAULT("Default"),
    TEMP("Temperature"),
    LIFE("Life Gradient"),
    FANCY("Fancy"),;

    final String displayName;

    ViewType(String displayName) {
        this.displayName = displayName;
    }

}
