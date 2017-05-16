package com.mygdx.game.rules_types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

/**
 * Class that represents a team before it is built in the {@code BattleScreen}. Strings are used as identifiers fpr entities.
 * @author Phillip O'Reggio
 */
public class TeamBuilder {
    private Array<String> strings;
    private Color teamColor;
    private String teamName;
    private boolean AIControlled;

    public TeamBuilder(Color color, String name, boolean isAIControlled) {
        teamColor = color;
        teamName = name;
        strings = new Array<String>();
        AIControlled = isAIControlled;
    }

    public void addEntity(String string) {
        strings.add(string);
    }

    public void removeEntity(int i) {
        strings.removeIndex(i);
    }
    public void setTeamName(String name) {
        teamName = name;
    }

    public Array<String> getStrings() {
        return strings;
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
        return "" + teamName + "(" + teamColor + ", AIControlled: " + AIControlled + ", Entities : " + strings;
    }
}
