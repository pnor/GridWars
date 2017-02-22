package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

/**
 * @author Phillip O'Reggio
 */
public interface GameEvent {
    void event(Entity e, Engine engine);
}
