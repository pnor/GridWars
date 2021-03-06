package com.mygdx.game.highscores;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.GameUtil;
import com.mygdx.game.ui.LerpColor;
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
    private int powerPower;
    private int speedPower;
    private int points;
    private int totalTurns;
    private int floor;
    /** False if the player died in that file or if its deleted */
    private boolean canLoadThisFile;

    /**
     * No args constructor for serialization
     */
    public SaveData() {}

    public SaveData(Team team, int healthPowerUps, int SPPowerUps, int attackPowerUps, int speedPowerUps, int score, int turns, int level) {
        setEntityInfo(team);
        HPPower = healthPowerUps;
        SPPower = SPPowerUps;
        powerPower = attackPowerUps;
        speedPower = speedPowerUps;
        teamName = team.getTeamName();
        teamColor = team.getTeamColor();
        // Handle Lerpcolors
        if (teamColor instanceof LerpColor) {
            ((LerpColor) teamColor).readyForSerialization();
        }
        points = score;
        totalTurns = turns;
        floor = level;
        canLoadThisFile = true;
    }

    public Team createTeam() {
        // If it is a LerpColor, set up after being Serialized
        if (teamColor instanceof LerpColor) {
            ((LerpColor) teamColor).setInterpolationFromSerializationString();
        }
        Team team = new Team(teamName, teamColor);
        for (int i = 0; i < stats.length; i++) {
            Entity member = GameUtil.getEntityFromID(IDValues[i], altColors[i]);
            stm.get(member).hp = stats[i].getHp();
            stm.get(member).sp = stats[i].getSp();
            team.addEntity(member);
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
            index++;
        }
    }

    /**  Changes team color to a default (White). Used to prevent crashes due to potential <br>
     * Serialization errors cuased by LerpColors. (Like when force quitting in Survival)
    */
    public void clearColor() {
        teamColor = Color.WHITE;
    }

    public void setLoadable(boolean loadable) {
        canLoadThisFile = loadable;
    }

    public int getPoints() {
        return points;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public int getFloor() {
        return floor;
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


    public int getAttackPower() {
        return powerPower;
    }

    public int getSpeedPower() {
        return speedPower;
    }

    public boolean canLoadFile() {
        return canLoadThisFile;
    }

    /**
     * Small class for storing HP and SP values. Only used in {@link SaveData} for saving Survival mode info
     */
    public static class StatPair {
        private int hp;
        private int sp;

        /**
         * No args constructor for serialization
         */
        public StatPair() {}

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
