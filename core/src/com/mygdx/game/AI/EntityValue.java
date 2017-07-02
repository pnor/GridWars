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
     * @param homeTeam value that is treated as the its own team. Being on the homeTeam is treated as addition, while not being on it is treated as
     *                 subtraction.
     *
     */
    public int getValue(int homeTeam) {
        int value = 0;
        if (hp > 0)
            value += (hp / maxHp) * 100;
        else
            value -= 50;
        value += sp * 5;
        value -= statusEffect * 10; //debug for now more is worse


        if (team == -1) //no team -> treat as weak enemy
            value /= 10;

        if (team != homeTeam)
            value *= -1;

        return value;
    }

    @Override
    public int compareTo(Object o) {
        if (getValue(-1) > ((EntityValue) o).getValue(-1))
            return 1;
        else if (getValue(-1) < ((EntityValue) o).getValue(-1))
            return -1;
        else
            return 0;
    }

    public EntityValue copy() {
        return new EntityValue(pos.copy(), team, hp, maxHp, sp, attack, defense, statusEffect);
    }

    @Override
    public String toString() {
        return "EntityValue{" +
                "team=" + team +
                ", hp=" + hp +
                ", maxHp=" + maxHp +
                ", sp=" + sp +
                ", attack=" + attack +
                ", defense=" + defense +
                ", statusEffect=" + statusEffect +
                ", pos=" + pos +
                '}';
    }
}
