package io.mattw.jpowder;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class Item {
    private int id = 0;
    private String name = "Item";
    private String description = "Description";
    private Color color = Color.GRAY;
}
