package io.mattw.jpowder.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class NewGameEvent {

    private boolean initialPauseState;

}
