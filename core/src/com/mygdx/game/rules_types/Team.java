package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import static com.mygdx.game.ComponentMappers.stm;

/**
 * Represents a team of entities.
 * @author Phillip O'Reggio
 */
public class Team {

    private Array<Entity> entities;
    private Color teamColor;
    private String teamName;

    //result info related
    private int totalAttacksUsed;

    public Team(String name, Color color, Array<Entity> e) {
        teamName = name;
        teamColor = color;
        entities = e;
    }

    public Team(String name, Color color) {
        teamName = name;
        teamColor = color;
        entities = new Array<Entity>();
    }

    public Team(Array<Entity> e) {
        teamColor = new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f);
        entities = e;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(int i) {
        entities.removeIndex(i);
    }

    /**
     * @return if all entities in a team's hp is <= 0. Entities with no StatComponent are considered dead
     */
    public boolean allDead() {
        for (int i = 0; i < entities.size; i++)
            if (stm.has(entities.get(i)) && !(stm.get(entities.get(i)).hp <= 0))
                return false;
        return true;
    }

    /**
     * @return the amount of entities on the team that are dead
     */
    public int getAmontDead() {
        int live = 0;
        for (Entity e : entities)
            if (stm.get(e).alive) live++;
        return live;
    }

    /**
     * @return the average max hp of all entities on the team
     */
    public float getAverageMaxHealth() {
        float health = 0;
        for (Entity e : entities)
            health += stm.get(e).maxHP;
        health /= entities.size;
        return health;
    }

    /**
     * @return returns the average hp of all entities on the team.
     */
    public float getAverageHealth() {
        float health = 0;
        for (Entity e : entities) {
            if (stm.get(e).alive)
                health += stm.get(e).hp;
        }
        health /= entities.size;
        return health;
    }

    /**
     * @return returns the average sp of all entities on the team.
     */
    public int getAverageSp() {
        int sp = 0;
        for (Entity e : entities) {
            if (stm.get(e).alive)
                sp += stm.get(e).sp;
        }
        sp /= entities.size;
        return sp;
    }

    public void incrementTotalAttacksUsed() {
        totalAttacksUsed ++;
    }

    public void setTeamName(String name) {
        teamName = name;
    }

    public void setTeamColor(Color color) {
        teamColor = color;
    }

    public void setTotalAttacksUsed(int i) {
        totalAttacksUsed = i;
    }

    public Array<Entity> getEntities() {
        return entities;
    }

    public Color getTeamColor() {
        return teamColor;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getTotalAttacksUsed() {
        return totalAttacksUsed;
    }

    public String toString() {
        return teamName + "(" + teamColor + ", Entities : " + entities + ")";
    }
}
