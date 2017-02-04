package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

/**
 * Phillip O
 * What happens when a move is used. Is used mainly in the {@code Move} class.
 */
public interface Attack {
    public void effect(Entity e, Array<BoardPosition> range, BoardManager boards);
}
