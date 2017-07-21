package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.move_related.Attack;

import static com.mygdx.game.ComponentMappers.nm;

/**
 * @author Phillip O'Reggio
 */
public class Turn {
    /** Entity moving */
    public Entity entity;
    /** New Position on Board */
    public BoardPosition pos;
    /** Which attack from the {@link com.mygdx.game.components.MovesetComponent} it will use*/
    public int attack;
    /** orientation of the attack */
    public int direction;

    public Turn(Entity e, BoardPosition newPosition, int attackNo, int directionNo) {
        entity = e;
        pos = newPosition;
        attack = attackNo;
        direction = directionNo;
    }

    public String toString() {
        return "Turn :\t \n" +
                        "\t Entity : " + nm.get(entity).name + "  || " + entity.toString() + "\n" +
                        "\t BoardPosition : " + pos + "\n" +
                        "\t Attack Index : " + attack +
                        "\t Direction of Attack : " + direction + " (from last attack)";
    }

    public String toStringCondensed() {
        return "Position: " + pos + " || Attack# " + attack + " || Direction: " + direction;
    }
}
