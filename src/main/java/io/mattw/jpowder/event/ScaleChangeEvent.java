package io.mattw.jpowder.event;

import io.mattw.jpowder.ui.Scale;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ScaleChangeEvent {

    private final Scale scale;

    public ScaleChangeEvent(Scale scale) {
        this.scale = scale;
    }

}
