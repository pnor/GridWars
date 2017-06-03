package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

import static com.mygdx.game.ComponentMappers.status;

/**
 * Component representing the stats of an Entity on the board.
 * @author pnore_000
 */
public class StatComponent implements Component {

    public boolean alive = true;
    public boolean canMove;

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
    //TODO no if statements for all these methods. StatusEffect object should have an object that tells it what stats it augments. Then this should cycle through those to compute final stat changes
    /**
     * Max Hp value after status effects and other effects are applied
     */
    public int getModMaxHp(Entity e) {
        int newMaxHp = maxHP;
        if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            //...
        }

        return newMaxHp;
    }

    /**
     * Sp value after status effects and other effects are applied
     */
    public int getModSp(Entity e) {
        int newSp = sp;
        if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            //...
        }

        return newSp;
    }

    /**
     * Max Sp value after status effects and other effects are applied
     */
    public int getModMaxSp(Entity e) {
        int newMaxSp = maxSP;
        if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            //...
        }

        return newMaxSp;
    }


    /**
     * Attack value after status effects and other effects are applied
     */
    public int getModAtk(Entity e) {
        int newAtk = atk;
        if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
           if (status.get(e).statusEffects.containsKey("Burn"))
               newAtk /= 2;
           if (status.get(e).statusEffects.containsKey("Curse"))
               newAtk /= 2;
        }

        return newAtk;
    }

    /**
     * Defense value after status effects and other effects are applied
     */
    public int getModDef(Entity e) {
        int newDef = def;
        if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            if (status.get(e).statusEffects.containsKey("Petrify"))
                newDef *= 2;
            if (status.get(e).statusEffects.containsKey("Curse"))
                newDef /= 2;
            if (status.get(e).statusEffects.containsKey("Defenseless"))
                newDef *= 0;
        }

        return newDef;
    }

    /**
     * Speed value after status effects and other effects are applied
     */
    public int getModSpd(Entity e) {
        int newSpd = spd;
        if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            if (status.get(e).statusEffects.containsKey("Paralyze"))
                newSpd /= 2;
            if (status.get(e).statusEffects.containsKey("Petrify"))
                newSpd *= 0;
            if (status.get(e).statusEffects.containsKey("Curse"))
                newSpd /= 2;
        }

        return newSpd;
    }
}
