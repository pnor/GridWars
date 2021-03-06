package com.mygdx.game.misc;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

/**
 * An event to be run in the EventSystem
 * @author Phillip O'Reggio
 */
public interface GameEvent {
    void event(Entity e, Engine engine);
}
