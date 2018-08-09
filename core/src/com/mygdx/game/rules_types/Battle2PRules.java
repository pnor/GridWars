package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.stm;

/**
 * Rules for regular battles with a total of 2 players. The win condition is to defeat everyone on the other team.
 * NOTE that the first 2 teams are considered the player's team.
 * @author Phillip O'Reggio
 */
public class Battle2PRules extends Rules {

    public Battle2PRules(BattleScreen s, Array<Team> teams, boolean survivalMode) {
       super(s, teams, survivalMode);
    }

    /**
     * Checks which team won by seeing if all entities on a team is dead. If If 60 turns has elapsed, a winner is chosen
     * based on the amount of health both teams have (if its survival, team 1 always wins)
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

        //if too many turns have passed, choose a team to win
        if (turnCount >= 60) {
            if (isSurvival) // if its survival always choose team 1 (computer)
                return teams.get(1);
            else { // Choose highest health team
                int team0health = 0, team1health = 0;
                for (Entity e : teams.get(0).getEntities())
                    team0health += stm.get(e).hp;
                for (Entity e : teams.get(1).getEntities())
                    team1health += stm.get(e).hp;
                if (team0health > team1health) return teams.get(0);
                else if (team0health < team1health) return teams.get(1);
                else return teams.get(MathUtils.random(1));
            }
        }

        return null;
    }
}
