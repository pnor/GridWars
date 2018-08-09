package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.screens.BattleScreen;
import com.mygdx.game.ui.LerpColor;
import com.mygdx.game.ui.LerpColorManager;

import static com.mygdx.game.ComponentMappers.bm;
import static com.mygdx.game.ComponentMappers.stm;

/**
 * Rules for a battle where each team tries to land on a space or kill the other team.
 * @author Phillip O'Reggio
 */
public class ZoneRules extends Rules {
    /** rows represent which team, columns represent which spaces */
    private Array<Array<BoardPosition>> zones;

    /**
     * Creates rule set where landing on zones will lead to victory
     * @param s {@link BattleScreen}
     * @param teams {@link Team}s
     * @param zone BoardPositions that are zones. Index the Array of positions corresponds to teams.
     *            (Ex. : zones.get(0) is team 0's zones)
     */
    public ZoneRules(BattleScreen s, Array<Team> teams, Array<Array<BoardPosition>> zone, boolean survivalMode) {
        super(s, teams, survivalMode);
        zones = zone;
    }

    /**
     * Checks the winning condition of the game. If a team is completely dead, the other team wins. If a team has a member on their
     * zone, they win. If 60 turns has elapsed, a winner is chosen based on the amount of health both teams have (if its survival, team 1 always wins)
     * @return winning team
     */
    @Override
    public Team checkWinConditions() {
        //Check if all other teams are dead
        for (int i = 0; i < teams.size; i++) {
            if (currentTeamTurn == i)
                continue;
            if (!teams.get(i).allDead())
                break;
            return teams.get(currentTeamTurn);
        }

        //if too many turns have passed, always choose team 1
        if (turnCount >= 60) {
            if (isSurvival) // if its survival, return team at index 1 (opponent)
                return teams.get(1);
            else { // if regular play, return team with highest health
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

        //Check if in target zone
        for (int i = 0; i < zones.size; i++) //teams with zones
            for (Entity e : teams.get(i).getEntities()) { //entity
                for (int j = 0; j < zones.get(i).size; j++) { //zone place
                    if (bm.get(e).pos.equals(zones.get(i).get(j)))
                        return teams.get(i);
                }
        }

        return null;
    }

    /**
     * Colors the zones on the boards
     */
    public void colorZones(LerpColorManager lerpColorManager) { //TODO make both sides flash zone color
        LerpColor lerpColor;
        for (int i = 0; i < zones.size; i++) {
            if (teams.get(i).getTeamColor() instanceof LerpColor) { //is already a lerpColor
                lerpColor = (LerpColor) teams.get(i).getTeamColor();
            } else {
                lerpColor = new LerpColor(teams.get(i).getTeamColor(), Color.WHITE, 2f, Interpolation.pow5In);
                lerpColorManager.registerLerpColor(lerpColor);
            }

            for (BoardPosition bp : zones.get(i)) { //set color of tiles
                BoardComponent.boards.getBoard().getTile(bp.r, bp.c).setColor(lerpColor);
            }
        }
    }

    public Array<Array<BoardPosition>> getZones() {
        return zones;
    }
}
