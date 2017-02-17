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

    /**
     * Creates a move that can be used
     * @param name2 name
     * @param usr user of move
     * @param rnge range
     * @param board {@code BoardManager}
     * @param atk effect of attack
     */
    public Move(String name2, Entity usr, Array<BoardPosition> rnge, BoardManager board, Attack atk) {
        name = name2;
        user = usr;
        range = rnge;
        boards = board;
        attack  = atk;
    }

    /**
     * Executes the effect of this object
     */
    public void useAttack() {
        attack.effect(user, range, boards);
    }

    public String getName() {
        return name;
    }

    public Array<BoardPosition> getRange() {
        return range;
    }
}
