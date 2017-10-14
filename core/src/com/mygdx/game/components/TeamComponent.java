package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Tells which team an entity is on. Entities with no team (doesn't have the component) move last in the turn order.
 * @author Phillip O'Reggio
 */
public class TeamComponent implements Component {
    /**
     * Playable teams shouldn't be less than 0.
     */
    public int teamNumber;

    public TeamComponent(int team) {
        teamNumber = team;
    }
}
