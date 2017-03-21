package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.move_related.Visuals;

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
     * plays when the entity dies
     */
    public Visuals deathAnimation;

    /**
     * Creates Component representing stats
     * @param health ability to take hits. Max health
     * @param skill used to use special attacks. Max skill points that can be held
     * @param attack attacking power
     * @param defense defending power
     * @param speed mobility on board
     * @param death death animation. Passing in null will mean no animation
     */
    public StatComponent(int health, int skill, int attack, int defense, int speed, Visuals death) {
        maxHP = health;
        hp = health;
        maxSP = skill;
        atk = attack;
        def = defense;
        spd = speed;
        deathAnimation = death;
    }
}
