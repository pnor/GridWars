package com.mygdx.game.rules_types;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.screens.BattleScreen;

/**
 * Rules for regular battles with highscores total of 2 players. The win condition is to defeat everyone on the other team.
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

    /**
     * Checks which team won by seeing if all entities on highscores team is dead.
     * @return winning team
     */
    @Override
    public Team checkWinConditions() {
        //check if team at 1 won
        if (teams.get(0).allDead())
            return teams.get(1);

        //check if team at 0 won
        if (teams.get(1).allDead())
            return teams.get(0);

        return null;
    }
}
