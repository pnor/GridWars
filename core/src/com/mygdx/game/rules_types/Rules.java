package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.components.StatusEffectComponent;
import com.mygdx.game.screens_ui.BattleScreen;

import static com.mygdx.game.ComponentMappers.state;
import static com.mygdx.game.ComponentMappers.status;
import static com.mygdx.game.ComponentMappers.stm;

/**
 * Class for the rules of the game. Handles turns, and win criteria.
 * @author Phillip O'Reggio
 */
public abstract class Rules {

    protected BattleScreen screen;
    protected Array<Team> entities;
    protected int currentTeamTurn;
    protected int totalTeams;

    public Rules(BattleScreen s, Array<Team> teams) {
        screen = s;
        entities = teams;
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
        if (screen.getSelectedEntity() != null)
            if (screen.getSelectedEntity() != null && stm.has(screen.getSelectedEntity()) && stm.get(screen.getSelectedEntity()).getModSpd(screen.getSelectedEntity()) > 0)
                try {
                    screen.removeMovementTiles();
                } catch (IndexOutOfBoundsException e) {}

        //toggle states
        for (Team t : entities) {
            for (Entity e : t.getEntities())
            if (state.has(e)) {
                state.get(e).canAttack = true;
                state.get(e).canMove = true;
                if (!screen.checkShading(e));
                    screen.shadeBasedOnState(e);
            }
        }
        //do affects and stats
        for (Entity e : entities.get(currentTeamTurn).getEntities()) {
            if (stm.has(e) && !(stm.get(e).sp >= stm.get(e).maxSP))
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
            }

        }
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
        return entities.get(currentTeamTurn);
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
