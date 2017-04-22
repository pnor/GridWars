package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

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

    public Team(boolean isAIControlled, Array<Entity> e) {
        teamColor = new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f);
        AIControlled = isAIControlled;
        entities = e;
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
