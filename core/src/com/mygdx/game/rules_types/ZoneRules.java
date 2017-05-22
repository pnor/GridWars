package com.mygdx.game.rules_types;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.screens_ui.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.bm;

/**
 * Rules for a battle where each team tries to land on a space or kill the other team.
 * @author Phillip O'Reggio
 */
public class ZoneRules extends Rules {
    /** rows represent which team, columns represent which spaces */
    private Array<Array<BoardPosition>> zones;

    public ZoneRules(BattleScreen s, Array<Team> teams, Array<Array<BoardPosition>> zone) {
        super(s, teams);
        zones = zone;
    }

    /**
     * Finds winning team by seeing if they are occupied a "zone" space.
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

        //Check if in target zone
        for (int i = 0; i < zones.size; i++) //teams with zones
            for (Entity e : teams.get(i).getEntities()) { //entity
                for (BoardPosition bp : zones.get(i)) { //zone place
                    if (bm.get(e).pos.equals(bp))
                        return teams.get(i);
                }
        }

        return null;
    }

    /**
     * Colors the zones on the boards
     */
    public void colorZones() {
        for (int i = 0; i < zones.size; i++)
            for (BoardPosition bp : zones.get(i))
                BoardComponent.boards.getBoard().getTile(bp.r, bp.c).setColor(teams.get(i).getTeamColor());
    }
}
