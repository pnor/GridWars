package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;

/**
 * Phillip O
 * What happens (unrelated to visuals) when a move is used. Is used mainly in the {@code Move} class.
 */
public interface Attack {
    /**
     * Represents effect the attack has on the stats of an entity
     * @param e user
     * @param bp position of where the effect will take place
     * @param boards {@code BoardManager}
     */
    void effect(Entity e, BoardPosition bp, BoardManager boards);
}
