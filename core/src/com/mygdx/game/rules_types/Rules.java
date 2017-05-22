package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.components.StatusEffectComponent;
import com.mygdx.game.screens_ui.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.state;
import static com.mygdx.game.ComponentMappers.status;
import static com.mygdx.game.ComponentMappers.stm;

/**
 * Class for the rules of the game. Handles turns, and win criteria.
 * @author Phillip O'Reggio
 */
public abstract class Rules {

    protected BattleScreen screen;
    protected Array<Team> teams;
    protected int currentTeamTurn;
    protected int totalTeams;

    protected int turnCount = 1;

    public Rules(BattleScreen s, Array<Team> t) {
        screen = s;
        teams = t;
        for (int i = 0; i< teams.size; i++)
            totalTeams += 1;
    }

    /**
     * @return the winning team. null if there is none.
     */
    public abstract Team checkWinConditions();

    /**
     * Changes the turn to the next player. Applies effects cuased by turn shift as well.
     */
    public void nextTurn() {
        currentTeamTurn = (currentTeamTurn + 1) % totalTeams;
        turnCount = currentTeamTurn == 0 ? turnCount + 1 : turnCount;
        if (screen.getSelectedEntity() != null)
            if (screen.getSelectedEntity() != null && stm.has(screen.getSelectedEntity()) && stm.get(screen.getSelectedEntity()).getModSpd(screen.getSelectedEntity()) > 0)
                try {
                    screen.removeMovementTiles();
                } catch (IndexOutOfBoundsException e) {}

        //skip turn if al entities are dead
        if (teams.get(currentTeamTurn).allDead()) {
            currentTeamTurn = (currentTeamTurn + 1) % totalTeams;
            turnCount = currentTeamTurn == 0 ? turnCount + 1 : turnCount;
        }

        //toggle states
        for (Team t : teams) {
            for (Entity e : t.getEntities())
            if (state.has(e)) {
                state.get(e).canAttack = true;
                state.get(e).canMove = true;
                if (!screen.checkShading(e));
                    screen.shadeBasedOnState(e);
            }
        }
        //do affects and stats
        for (Entity e : teams.get(currentTeamTurn).getEntities()) {
            if (stm.has(e) && !(stm.get(e).getModSp(e) >= stm.get(e).getModMaxSp(e)) && !(status.has(e) && status.get(e).isStill()))
                stm.get(e).sp += 1;

            //status effects
            if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
                if (status.get(e).isPoisoned())
                    StatusEffectComponent.poisonTurnEffect(e);
                if (status.get(e).isBurned())
                    StatusEffectComponent.burnTurnEffect(e);
                if (status.get(e).isParalyzed())
                    StatusEffectComponent.paralyzeTurnEffect(e);
                if (status.get(e).isPetrified())
                    StatusEffectComponent.petrifyTurnEffect(e);
                if (status.get(e).isStill())
                    StatusEffectComponent.stillnessTurnEffect(e);
                if (status.get(e).isCursed())
                    StatusEffectComponent.curseTurnEffect(e);
            }
        }
        //update the team bar
        screen.updateTeamBar();

        System.out.println("Current Turn : " + currentTeamTurn);
        System.out.println("Total team turn : " + totalTeams);
        System.out.println("--------------");
    }

    /**
     * Process computer controlled teams.
     */
    public void processAiTeam() {
        //...?
    }

    public int getCurrentTeamNumber() {
        return currentTeamTurn;
    }

    public Team getCurrentTeam() {
        return teams.get(currentTeamTurn);
    }

    public int getTurnCount() {
        return turnCount;
    }

    public int getTotalTeams() {
        return totalTeams;
    }
/*
    public void calculateTotalTeams() {
        totalTeams = 0;
        for (int i = 0; i < teams.size; i++)
            totalTeams += 1;
    }*/
}
