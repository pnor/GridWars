package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Component for the name of an Entity.
 * @author pnore_000
 */
public class NameComponent implements Component {
    public String name;
    /**
     * Unique Identification ID between different types of entities. This is used to save what entity was used to display in the high score table.
     */
    public int serializeID = -2;

    public NameComponent(String n) {
        name = n;
    }
}
