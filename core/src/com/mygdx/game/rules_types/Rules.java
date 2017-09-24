package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.misc.Phase;
import com.mygdx.game.move_related.StatusEffect;
import com.mygdx.game.screens_ui.screens.BattleScreen;

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

    protected int turnCount = 0;

    public Rules(BattleScreen s, Array<Team> t) {
        screen = s;
        teams = t;
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
        currentTeamTurn = (currentTeamTurn + 1) % totalTeams;
        turnCount = currentTeamTurn == 0 ? turnCount + 1 : turnCount;
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
                //clamp sp and hp values to max
                if (stm.get(e).hp > stm.get(e).getModMaxHp(e))
                    stm.get(e).hp = stm.get(e).getModMaxHp(e);
                if (stm.get(e).sp > stm.get(e).getModMaxSp(e))
                    stm.get(e).sp = stm.get(e).getModMaxSp(e);

                //toggle states
                if (state.has(e)) {
                    state.get(e).canAttack = true; //TODO make it not redundant
                    if (status.has(e) && (status.get(e).statusEffects.containsKey("Freeze") || status.get(e).statusEffects.containsKey("Petrify")))
                        state.get(e).canAttack = false;
                    state.get(e).canMove = true;
                    if (!screen.checkShading(e))
                        screen.shadeBasedOnState(e);
                }
                //phase component
                if (phase.has(e) && stm.get(e).alive) {
                    for (int i = 0; i < phase.get(e).phases.size; i++) {
                        if (i == phase.get(e).currentPhaseIndex) //skip current phase
                            continue;
                        Phase curPhase = phase.get(e).phases.get(i);

                        if (curPhase.withinThreshold(stm.get(e).hp)) {
                            phase.get(e).currentPhaseIndex = i;
                            BoardComponent.boards.getBoard().remove(am.get(e).actor, bm.get(e).pos.r, bm.get(e).pos.c);
                            curPhase.applyPhase(e);
                            BoardComponent.boards.getBoard().add(am.get(e).actor, bm.get(e).pos.r, bm.get(e).pos.c);
                            break;
                        }
                    }
                }
            }
        }
        //do affects and stats (current Team only) --
        for (Entity e : teams.get(currentTeamTurn).getEntities()) { //increment sp
            if (stm.has(e) && !(stm.get(e).sp >= stm.get(e).getModMaxSp(e)) && !(status.has(e) && status.get(e).statusEffects.containsKey("stillness"))) //check if it can
                stm.get(e).sp += 1;

            //status effects
            if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
                for (StatusEffect effect : status.get(e).statusEffects.values().toArray()) {
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
