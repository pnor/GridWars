package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Component for the name of an Entity.
 * @author pnore_000
 */
public class NameComponent implements Component {
    public String name;

    public NameComponent(String n) {
        name = n;
    }
}
