package com.mygdx.game.boards;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.screens_ui.BattleScreen;

import static com.mygdx.game.ComponentMappers.state;

/**
 * Class for the rules of the game. Handles turns, and win criteria.
 * @author Phillip O'Reggio
 */
public class Rules {

    private BattleScreen screen;
    private Array<Array<Entity>> entities;
    private int currentTeamTurn;
    private int totalTeams;

    public Rules(BattleScreen s, Array<Array<Entity>> teams) {
        screen = s;
        entities = teams;
        for (int i = 0; i< teams.size; i++)
            totalTeams += 1;
    }

    public void nextTurn() {
        currentTeamTurn = (currentTeamTurn + 1) % totalTeams;
        if (screen.getSelectedEntity() != null)
            try {
                screen.removeMovementTiles();
            } catch (IndexOutOfBoundsException e) {}


        for (Array<Entity> t : entities) {
            for (Entity e : t)
            if (state.has(e)) {
                state.get(e).canAttack = true;
                state.get(e).canMove = true;
                if (!screen.checkShading(e));
                    screen.shadeBasedOnState(e);
            }
        }
        System.out.println("Current Turn : " + currentTeamTurn);
        System.out.println("Total team turn : " + totalTeams);
        System.out.println("--------------");
    }

    /**
     * process computer controlled teams
     */
    public void processAiTeam() {
        //...?
    }

    public int getCurrentTeam() {
        return currentTeamTurn;
    }

    public int getTotalTeams() {
        return totalTeams;
    }

    public void calculateTotalTeams() {
        totalTeams = 0;
        for (int i = 0; i < entities.size; i++)
            totalTeams += 1;
    }
}
