package io.mattw.jpowder.event;

import io.mattw.jpowder.game.ItemCategory;
import lombok.ToString;

@ToString
public class CategorySelectEvent {

    private final ItemCategory category;

    public CategorySelectEvent(ItemCategory category) {
        this.category = category;
    }

}
