package com.mygdx.game.highscores;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.rules_types.Team;

import static com.mygdx.game.ComponentMappers.nm;
import static com.mygdx.game.GridWars.atlas;

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

    /**
     * Static method that returns the sprite that is represented by the byte value passed in. Since Sprites cannot be stored in
     * the Json file, this is how the team images displayed on the High score are found.
     * @param index number corresponding to a sprite
     * @return Current sprite representing an entity's image
     */
    public static Sprite getSpriteFromNumber(int index) {
        switch (index) {
            //region all cases
            case -2 : //sprite that player cannot choose normally
                return atlas.createSprite("mystery");
            case -1 : //no team member
                return atlas.createSprite("cube");
            case 0 :
                return atlas.createSprite("Canight");
            case 1 :
                return atlas.createSprite("catdroid");
            case 2 :
                return atlas.createSprite("firebull");
            case 3 :
                return atlas.createSprite("icebird");
            case 4 :
                return atlas.createSprite("fish");
            case 5 :
                return atlas.createSprite("turtle");
            case 6 :
                return atlas.createSprite("fox");
            case 7 :
                return atlas.createSprite("thunderdog");
            case 8 :
                return atlas.createSprite("mummy");
            case 9 :
                return atlas.createSprite("squid");
            case 10 :
                return atlas.createSprite("steamdragon");
            case 11 :
                return atlas.createSprite("jellygirl");
            case 12 :
                return atlas.createSprite("mirrorman");
            case 13 :
                return atlas.createSprite("pheonix");
            case 14 :
                return atlas.createSprite("acidsnake");
            case 15 :
                return atlas.createSprite("dragonAlt");
            default:
                return atlas.createSprite("robot");
            //endregion
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
