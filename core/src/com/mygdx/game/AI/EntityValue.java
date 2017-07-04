package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.ComponentMappers;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.rules_types.Team;

/**
 * Class representing a simplified form of entities on the board. Only contains values relating to an Entity's value, such
 * as stats, status effects, etc.
 *
 * @author Phillip O'Reggio
 */
public class EntityValue implements Comparable {
    public int team;
    public int indexInTeam; //For identity purposes

    public int hp;
    public int maxHp;
    public int sp;
    public int attack;
    public int defense;
    public int statusEffect;
    public BoardPosition pos;

    public EntityValue(BoardPosition position, int teamNo, int indexWithinTeam, int health, int maxHealth, int skill, int atk, int def, int effectValue) {
        pos = position;
        team = teamNo;
        indexInTeam = indexWithinTeam;

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
            value += 200 + (hp / maxHp) * 150;

        //value += sp * 15;
        value -= statusEffect * 20; //debug for now more is worse


        if (team == -1) //no team -> treat as weak enemy
            value /= 25;

        if (team != homeTeam)
            value *= -1;

        return value;
    }

    /**
     * Checks whether this {@link EntityValue} is the one generated from the {@link Entity} parameter. Does this
     * by comparing team, and the Entity's index in its team.
     * @param e Entity being compared
     * @return True, if they represent the same Entity. False otherwise.
     */
    public boolean checkIdentity(Entity e, Team t) {
        return ComponentMappers.team.get(e).teamNumber == team && t.getEntities().indexOf(e, true) == indexInTeam;
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
        return new EntityValue(pos.copy(), team, indexInTeam, hp, maxHp, sp, attack, defense, statusEffect);
    }

    @Override
    public String toString() {
        return "EntityValue{" +
                "team=" + team +
                ", indexInTeam=" + indexInTeam +
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
