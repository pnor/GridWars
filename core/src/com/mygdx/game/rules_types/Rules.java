package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.components.StatusEffectComponent;
import com.mygdx.game.misc.Phase;
import com.mygdx.game.move_related.StatusEffect;
import com.badlogic.gdx.utils.OrderedMap;
import com.mygdx.game.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class for the rules of the game. Handles turns, and win criteria.
 * @author Phillip O'Reggio
 */
public abstract class Rules {

    protected BattleScreen screen;
    protected Array<Team> teams;
    protected int currentTeamTurn;
    protected int totalTeams;
    /**
     * In any case where the rules change due to it being in Survival mode
     */
    protected boolean isSurvival;

    protected int turnCount = 0;

    public Rules(BattleScreen s, Array<Team> t, boolean survivalMode) {
        screen = s;
        teams = t;
        isSurvival = survivalMode;
        totalTeams = teams.size;
        currentTeamTurn = totalTeams - 1;
    }

    /**
     * @return the winning team. null if there is none.
     */
    public abstract Team checkWinConditions();

    /**
     * Changes the turn to the next player. Applies effects caused by turn shift as well.
     */
    public void nextTurn() {
        //increment turn count
        currentTeamTurn = (currentTeamTurn + 1) % totalTeams;
        turnCount = currentTeamTurn == 0 ? turnCount + 1 : turnCount;
        //remove movement tiles
        if (screen.getSelectedEntity() != null)
            if (screen.getSelectedEntity() != null && stm.has(screen.getSelectedEntity()) && stm.get(screen.getSelectedEntity()).getModSpd(screen.getSelectedEntity()) > 0)
                try {
                    screen.removeMovementTiles();
                } catch (IndexOutOfBoundsException e) {}

        //skip turn if all entities are dead
        if (teams.get(currentTeamTurn).allDead()) {
            currentTeamTurn = (currentTeamTurn + 1) % totalTeams;
            turnCount = currentTeamTurn == 0 ? turnCount + 1 : turnCount;
        }

        //All entities in all teams --
        //toggle states + process PhaseComponent entities
        for (Team t : teams) {
            for (Entity e : t.getEntities()) {
                //phase component
                if (phase.has(e) && stm.get(e).alive) {
                    for (int i = 0; i < phase.get(e).phases.size; i++) {
                        if (i == phase.get(e).currentPhaseIndex) //skip current phase
                            continue;
                        Phase curPhase = phase.get(e).phases.get(i);

                        if (curPhase.withinThreshold(stm.get(e).hp)) { //change phase
                            phase.get(e).currentPhaseIndex = i;
                            BoardComponent.boards.getBoard().remove(am.get(e).actor, bm.get(e).pos.r, bm.get(e).pos.c);
                            curPhase.applyPhase(e);
                            BoardComponent.boards.scaleEntity(e);
                            BoardComponent.boards.getBoard().add(am.get(e).actor, bm.get(e).pos.r, bm.get(e).pos.c);
                            break;
                        }
                    }
                }

                //clamp sp and hp values to max
                if (stm.get(e).hp > stm.get(e).getModMaxHp(e))
                    stm.get(e).hp = stm.get(e).getModMaxHp(e);
                if (stm.get(e).sp > stm.get(e).getModMaxSp(e))
                    stm.get(e).sp = stm.get(e).getModMaxSp(e);

                //toggle states
                if (state.has(e)) {
                    state.get(e).canAttack = true;
                    // Disable attacking if those status effects have more than 1 turn
                    /* If it only has 1 turn, then the status effect will resolve while still
                    rendering the entity unable to attack */
                    if (status.has(e)) {
                        StatusEffectComponent statusEffectComp = status.get(e);
                        boolean freezeCriteria = statusEffectComp.contains("Freeze") && 
                            !statusEffectComp.getStatusEffect("Freeze").getIsCloseToFinshing();
                        boolean petrifyCriteria = statusEffectComp.contains("Petrify") && 
                            !statusEffectComp.getStatusEffect("Petrify").getIsCloseToFinshing();

                        if (freezeCriteria || petrifyCriteria) {
                            state.get(e).canAttack = false;
                        }
                    }
                    state.get(e).canMove = true;
                    if (!screen.checkShading(e))
                        screen.shadeBasedOnState(e);
                }
            }
        }
        //do affects and stats (current Team only) --
        for (Entity e : teams.get(currentTeamTurn).getEntities()) { //increment sp
            if (stm.has(e) && stm.get(e).alive && !(stm.get(e).sp >= stm.get(e).getModMaxSp(e)) && !(status.has(e) && status.get(e).contains("stillness"))) //check if it can
                stm.get(e).sp += 1;

            //status effects
            if (status.has(e) && stm.get(e).alive && status.get(e).getTotalStatusEffects() > 0) {
                for (StatusEffect effect : status.get(e).getStatusEffects()) {
                    effect.doTurnEffect(e);
                    if (effect.getIsFinished())
                        status.get(e).removeStatusEffect(e, effect.getName());
                }
            }
        }
        //update the team bar
        screen.updateTeamBar();
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
}
