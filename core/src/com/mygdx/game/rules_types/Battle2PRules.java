package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.screens_ui.BattleScreen;

import static com.mygdx.game.ComponentMappers.stm;

/**
 * Rules for regular battles with a total of 2 players. The win condition is to defeat everyone on the other team.
 * NOTE that the first 2 teams are considered the player's team.
 * @author Phillip O'Reggio
 */
public class Battle2PRules extends Rules {

    public Battle2PRules(BattleScreen s, Array<Team> teams) {
       super(s, teams);
       /*
       if (teams.size < 2)
           throw new IllegalArgumentException("Class Battle2PRules requires at least 2 teams");
           */
    }

    @Override
    public Team checkWinConditions() {
        //check if team at 1 won
        boolean allDead = true;
        for (Entity e : entities.get(0).getEntities()) {
            if (stm.get(e).alive)
                allDead = false;
        }
        if (allDead)
            return entities.get(1);

        //check if team at 0 won
        allDead = true;
        for (Entity e : entities.get(0).getEntities()) {
            if (stm.get(e).alive)
                allDead = false;
        }
        if (allDead)
            return entities.get(2);

        return null;
    }
}
