package com.mygdx.game.highscores;

import com.mygdx.game.rules_types.Team;

import static com.mygdx.game.ComponentMappers.nm;

/**
 * Class containing information to be displayed in the Highscore table.
 * @author Phillip O'Reggio
 */
public class HighScore implements Comparable {
    private String teamName;
    private int score, turns, lastFloor;
    /** Array of integers representing team member images.*/
    private int[] teamSprites;

    public HighScore() {
        teamName = "No Arg";
        score = -1;
        turns = -1;
        teamSprites = new int[]{};
    }

    public HighScore(String name, int score, int turns, int lastFloor, int... spriteDrawables) {
        teamName = name;
        this.score = score;
        this.turns = turns;
        this.lastFloor = lastFloor;
        teamSprites = spriteDrawables;
    }

    /**
     * Compares in reverse order, since higher scores should be in the first indexes when Array.sort is called
     */
    public int compareTo(Object o) {
        if (o instanceof HighScore) {
            if (((HighScore) o).score < score)
                return -1;
            else if (((HighScore) o).score == score)
                return 0;
            else
                return 1;
        }
        return -1;
    }

    public void setTeamSprites(Team t) {
        teamSprites = new int[t.getEntities().size];
        for (int i = 0; i < t.getEntities().size; i++) {
            teamSprites[i] = nm.get(t.getEntities().get(i)).serializeID;
        }
    }

    public String getTeamName() {
        return teamName;
    }

    public int getScore() {
        return score;
    }

    public int getTurns() {
        return turns;
    }

    public int getLastFloor() {
        return lastFloor;
    }

    public int[] getTeamSprites() {
        return teamSprites;
    }
}
