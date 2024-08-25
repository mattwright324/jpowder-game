package io.mattw.jpowder.event;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class HelpChangeEvent {

    private final boolean display;

    public HelpChangeEvent(boolean display) {
        this.display = display;
    }

}
