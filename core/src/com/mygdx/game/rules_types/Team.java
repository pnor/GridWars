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
    private boolean AIControlled;

    public Team(String name, Color color, boolean isAIControlled, Array<Entity> e) {
        teamName = name;
        teamColor = color;
        AIControlled = isAIControlled;
        entities = e;
    }

    public Team(String name, Color color, boolean isAIControlled) {
        teamName = name;
        teamColor = color;
        AIControlled = isAIControlled;
        entities = new Array<Entity>();
    }

    public Team(boolean isAIControlled, Array<Entity> e) {
        teamColor = new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f);
        AIControlled = isAIControlled;
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

    public void setTeamName(String name) {
        teamName = name;
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

    public boolean getAIControlled() {
        return AIControlled;
    }

    public String toString() {
        return "" + teamName + "(" + teamColor + ", AIControlled: " + AIControlled + ", Entities : " + entities;
    }
}
