package com.mygdx.game.highscores;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.GameUtil;
import com.mygdx.game.rules_types.Team;

import static com.mygdx.game.ComponentMappers.nm;
import static com.mygdx.game.ComponentMappers.stm;

/**
 * Class containing data for an in-progress Survival run
 * @author Phillip O'Reggio
 */
public class SaveData {
    // things this stores
    private String teamName;
    private Color teamColor;
    private StatPair[] stats;
    private int[] IDValues;
    private int[] altColors;
    private int HPPower;
    private int SPPower;

    public SaveData(Team team, int healthPowerUps, int SPPowerUps) {
        setEntityInfo(team);
        HPPower = healthPowerUps;
        SPPower = SPPowerUps;
        teamName = team.getTeamName();
        teamColor = team.getTeamColor();
    }

    public Team createTeam() {
        Team team = new Team(teamName, teamColor);
        for (int i = 0; i < stats.length; i++) {
            team.addEntity(GameUtil.getEntityFromID(IDValues[i], altColors[i]));
            stm.get(team.getEntities().get(i)).hp = stats[i].hp;
            stm.get(team.getEntities().get(i)).sp = stats[i].sp;
        }
        return team;
    }

    private void setEntityInfo(Team team) {
        stats = new StatPair[team.getEntities().size];
        IDValues = new int[team.getEntities().size];
        altColors = new int[team.getEntities().size];
        int index = 0;
        for (Entity e : team.getEntities()) {
            stats[index] = new StatPair(stm.get(e).hp, stm.get(e).sp);
            IDValues[index] = nm.get(e).serializeID;
            altColors[index] = nm.get(e).altColor;
        }
    }

    public StatPair[] getStats() {
        return stats;
    }

    public int[] getIDValues() {
        return IDValues;
    }

    public int getHPPower() {
        return HPPower;
    }

    public int getSPPower() {
        return SPPower;
    }

    /**
     * Small class for storing HP and SP values
     */
    class StatPair {
        private int hp;
        private int sp;

        public StatPair(int health, int skill) {
            hp = health;
            sp = skill;
        }

        public int getHp() {
            return hp;
        }

        public int getSp() {
            return sp;
        }
    }
}
