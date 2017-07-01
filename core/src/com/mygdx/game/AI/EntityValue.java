package com.mygdx.game.AI;

import com.mygdx.game.boards.BoardPosition;

/**
 * Class representing a simplified form of entities on the board. Only contains values relating to an Entity's value, such
 * as stats, status effects, etc.
 *
 * @author Phillip O'Reggio
 */
public class EntityValue implements Comparable {
    public int team;

    public int hp;
    public int maxHp;
    public int sp;
    public int attack;
    public int defense;
    public int statusEffect;
    public BoardPosition pos;

    public EntityValue(BoardPosition position, int teamNo, int health, int maxHealth, int skill, int atk, int def, int effectValue) {
        pos = position;
        team = teamNo;

        hp = health;
        sp = skill;
        maxHp = maxHealth;
        attack = atk;
        defense = def;
        statusEffect = effectValue;
    }

    /**
     * Gets the value of the {@link com.badlogic.ashley.core.Entity}.
     */
    public int getValue() {
        int value = 100;
        value *= maxHp / hp;
        value -= statusEffect * 20; //debug for now more is worse
        return value;
    }

    @Override
    public int compareTo(Object o) {
        if (getValue() > ((EntityValue) o).getValue())
            return 1;
        else if (getValue() < ((EntityValue) o).getValue())
            return -1;
        else
            return 0;
    }

    public EntityValue copy() {
        return new EntityValue(pos.copy(), team, hp, maxHp, sp, attack, defense, statusEffect);
    }
}
