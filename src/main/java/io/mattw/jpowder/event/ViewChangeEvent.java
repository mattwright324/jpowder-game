package io.mattw.jpowder.event;

import io.mattw.jpowder.game.ViewType;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ViewChangeEvent {

    private final ViewType viewType;

    public ViewChangeEvent(ViewType viewType) {
        this.viewType = viewType;
    }

}
