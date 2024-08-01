package io.mattw.jpowder.event;

import lombok.Getter;

@Getter
public class PauseChangeEvent {

    private final boolean paused;

    public PauseChangeEvent(boolean paused) {
        this.paused = paused;
    }

}
