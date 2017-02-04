package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

/**
 * @author Phillip O
 * Represents an attack used by an entity
 */
public class Move {
    private String name;

    private Attack attack;
    /**
     * Represents effected tiles relative to user's position
     */
    private Array<BoardPosition> range;

    private Entity user;
    private BoardManager boards;

    public Move(String n, Entity u, Array<BoardPosition> r, BoardManager b, Attack atk) {
        name = n;
        user = u;
        range = r;
        boards = b;
        attack  = atk;
    }

    public void useAttack() {
        attack.effect(user, range, boards);
    }

    public Array<BoardPosition> getRange() {
        return range;
    }
}
