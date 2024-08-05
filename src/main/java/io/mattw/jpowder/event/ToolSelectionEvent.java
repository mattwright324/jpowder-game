package io.mattw.jpowder.event;

import io.mattw.jpowder.game.Item;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ToolSelectionEvent {

    private final Item selectedItem;
    private final int mouseButton; // BUTTON1=left BUTTON2=right

    public ToolSelectionEvent(Item selectedItem, int mouseButton) {
        this.selectedItem = selectedItem;
        this.mouseButton = mouseButton;
    }

}
