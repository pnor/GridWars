package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ComponentMappers;
import com.mygdx.game.boards.BoardPosition;

/**
 * Class representing a simplified form of entities on the board. Only contains values relating to an Entity's value, such
 * as stats, status effects, etc.
 *
 * @author Phillip O'Reggio
 */
public class EntityValue implements Comparable {
    public int team;
    public final int BOARD_ENTITY_ID;

    public int hp;
    public int maxHp;
    public int sp;
    public int maxSp;
    public int attack;
    public int defense;

    /**
     * Points representing nothing. Used to fine tune moves with effects that cannot be expressed in raw stats, status conditions or position.
     * For example, a move that copies another move or to discourage using damaging moves on allies or empty spaces
     */
    public int arbitraryValue;

    public boolean acceptsStatusEffects;
    public Array<StatusEffectInfo> statusEffectInfos;

    public BoardPosition pos;

    /**
     * Creates an {@link EntityValue} based off an Entity that cannot receive a status effect.
     * @param position position of the Entity
     * @param teamNo Entity's team number
     * @param health Entity's health
     * @param maxHealth Entity's max health
     * @param skill Entity's skill points
     * @param atk Entity's attack value
     * @param def Entity's defense value
     */
    public EntityValue(BoardPosition position, int teamNo, int boardEntityID, int health, int maxHealth, int skill, int maxSp, int atk, int def, int arbitrary) {
        pos = position;
        team = teamNo;
        BOARD_ENTITY_ID = boardEntityID;

        hp = health;
        sp = skill;
        maxHp = maxHealth;
        attack = atk;
        defense = def;
        acceptsStatusEffects = false;

        arbitraryValue = arbitrary;
    }

    public EntityValue(BoardPosition position, int teamNo, int boardEntityID, int health, int maxHealth, int skill, int maxSp, int atk, int def, StatusEffectInfo[] statusEffects, int arbitrary) {
        pos = position;
        team = teamNo;
        BOARD_ENTITY_ID = boardEntityID;

        hp = health;
        sp = skill;
        maxHp = maxHealth;
        attack = atk;
        defense = def;

        acceptsStatusEffects = true;
        statusEffectInfos = new Array<>(statusEffects);

        arbitraryValue = arbitrary;
    }

    /**
     * Gets the value of the {@link com.badlogic.ashley.core.Entity}.
     * @param homeTeam value that is treated as the its own team. Being on the homeTeam is treated as addition, while not being on it is treated as
     *                 subtraction.
     */
    public int getValue(int homeTeam) {
        int value = 0;
        value += arbitraryValue;

        if (hp > 0 && team != -1) {
            value += 300 + (hp * 30);
        }

        // Slightly encourage not using SP for similar situations
        value += sp;

        if (team != homeTeam) {
            value *= -1;
        }

        return value;
    }

    /**
     * Checks whether this {@link EntityValue} is the one generated from the {@link Entity} parameter.
     * @param e Entity being compared
     * @return True, if they represent the same Entity. False otherwise.
     */
    public boolean checkIdentity(Entity e) {
        return ComponentMappers.bm.get(e).BOARD_ENTITY_ID == BOARD_ENTITY_ID;
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
        if (statusEffectInfos == null) {
            return new EntityValue(pos.copy(), team, BOARD_ENTITY_ID, hp, maxHp, sp, maxSp, attack, defense, arbitraryValue);
        } else {
            //copy status effects
            StatusEffectInfo[] copyStatus = new StatusEffectInfo[statusEffectInfos.size];
            for (int i = 0; i < statusEffectInfos.size; i++)
                copyStatus[i] = statusEffectInfos.get(i).copy();
            return new EntityValue(pos.copy(), team, BOARD_ENTITY_ID, hp, maxHp, sp, maxSp, attack, defense, copyStatus, arbitraryValue);
        }
    }

    /**
     * Attack value after status effects and other effects are applied
     */
    public int getModAtk() {
        int atk = attack;
        if (statusEffectInfos == null)
            return atk;

        for (StatusEffectInfo status : statusEffectInfos) {
            if (status.statChanges == null) continue;
            atk = (int) (atk * status.statChanges.atk);
        }

        return atk;
    }

    /**
     * Defense value after status effects and other effects are applied
     */
    public int getModDef() {
        int def = defense;
        if (statusEffectInfos == null)
            return def;

        for (StatusEffectInfo status : statusEffectInfos) {
            if (status.statChanges == null) continue;
            def = (int) (def * status.statChanges.def);
        }

        return def;
    }

    /**
     * Max HP value after status effects and other effects are applied
     */
    public int getModMaxHp() {
        int maxHP = maxHp;
        if (statusEffectInfos == null)
            return maxHP;

        for (StatusEffectInfo status : statusEffectInfos) {
            if (status.statChanges == null) continue;
            maxHP = (int) (maxHP * status.statChanges.maxHP);
        }

        return maxHP;
    }

    /**
     * Max SP value after status effects and other effects are applied
     */
    public int getModMaxSp() {
        int maxSP = maxSp;
        if (statusEffectInfos == null)
            return maxSP;

        for (StatusEffectInfo status : statusEffectInfos) {
            if (status.statChanges == null) continue;
            maxSP = (int) (maxSP * status.statChanges.maxSP);
        }

        return maxSP;
    }

    @Override
    public String toString() {
        return "EntityValue{" +
                "team= " + team +
                ", board Entity ID= " + BOARD_ENTITY_ID +
                ", hp= " + hp +
                ", maxHp= " + maxHp +
                ", sp= " + sp +
                ", attack= " + attack +
                ", defense= " + defense +
                ", accepts Status Effect= " + acceptsStatusEffects +
                ", pos= " + pos +
                '}';
    }
}
