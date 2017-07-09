package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.mygdx.game.move_related.StatusEffect;

import static com.mygdx.game.ComponentMappers.status;

/**
 * Component representing the stats of an Entity on the board.
 * @author pnore_000
 */
public class StatComponent implements Component {

    public boolean alive = true;

    /**
     * health
     */
    public int hp;
    /**
     * max health
     */
    public int maxHP;
    /**
     * skill points
     */
    public int sp;
    /**
     * max skill points
     */
    public int maxSP;
    /**
     * attack
     */
    public int atk;
    /**
     * defense
     */
    public int def;
    /**
     * speed
     */
    public int spd;

    /**
     * Creates Component representing stats
     * @param health ability to take hits. Max health
     * @param skill used to use special attacks. Max skill points that can be held
     * @param attack attacking power
     * @param defense defending power
     * @param speed mobility on board
     */
    public StatComponent(int health, int skill, int attack, int defense, int speed) {
        maxHP = health;
        hp = health;
        maxSP = skill;
        atk = attack;
        def = defense;
        spd = speed;
    }

    /**
     * Max Hp value after status effects and other effects are applied
     */
    public int getModMaxHp(Entity e) {
        int newMaxHp = maxHP;
        if (!status.has(e))
            return maxHP;

        for (StatusEffect status : status.get(e).statusEffects.values()) {
            if (status.getStatChanges() == null) continue;
            newMaxHp = (int) (newMaxHp * status.getStatChanges().maxHP);
        }

        return newMaxHp;
    }

    /**
     * Sp value after status effects and other effects are applied
     */
    public int getModSp(Entity e) {
        int newSp = sp;
        if (!status.has(e))
            return sp;

        for (StatusEffect status : status.get(e).statusEffects.values()) {
            if (status.getStatChanges() == null) continue;
            newSp = (int) (newSp * status.getStatChanges().sp);
        }

        return newSp;
    }

    /**
     * Max Sp value after status effects and other effects are applied
     */
    public int getModMaxSp(Entity e) {
        int newMaxSp = maxSP;
        if (!status.has(e))
            return maxSP;

        for (StatusEffect status : status.get(e).statusEffects.values()) {
            if (status.getStatChanges() == null) continue;
            newMaxSp = (int) (newMaxSp * status.getStatChanges().maxSP);
        }

        return newMaxSp;
    }


    /**
     * Attack value after status effects and other effects are applied
     */
    public int getModAtk(Entity e) {
        int newAtk = atk;
        if (!status.has(e))
            return atk;

        for (StatusEffect status : status.get(e).statusEffects.values()) {
            if (status.getStatChanges() == null) continue;
            newAtk = (int) (newAtk * status.getStatChanges().atk);
        }

        return newAtk;
    }

    /**
     * Defense value after status effects and other effects are applied
     */
    public int getModDef(Entity e) {
        int newDef = def;
        if (!status.has(e))
            return newDef;

        for (StatusEffect status : status.get(e).statusEffects.values()) {
            if (status.getStatChanges() == null) continue;
            newDef = (int) (newDef * status.getStatChanges().def);
        }

        return newDef;
    }

    /**
     * Speed value after status effects and other effects are applied
     */
    public int getModSpd(Entity e) {
        int newSpd = spd;
        if (!status.has(e))
            return spd;

        for (StatusEffect status : status.get(e).statusEffects.values()) {
            if (status.getStatChanges() == null) continue;
            newSpd = (int) (newSpd * status.getStatChanges().spd);
        }

        return newSpd;
    }
}
