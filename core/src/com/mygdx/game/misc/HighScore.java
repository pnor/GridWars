package com.mygdx.game.misc;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Class containing information to be displayed in the Highscore table.
 * @author Phillip O'Reggio
 */
public class HighScore implements Comparable {
    private String teamName;
    private int score, turns;
    private SpriteDrawable[] sprites;

    public HighScore() {
        teamName = "No Arg";
        score = -1;
        turns = -1;
        sprites = new SpriteDrawable[]{};
    }

    public HighScore(String name, int score, int turns, SpriteDrawable... spriteDrawables) {
        teamName = name;
        this.score = score;
        this.turns = turns;
        sprites = spriteDrawables;
    }

    public int compareTo(Object o) {
        if (o instanceof HighScore) {
            if (((HighScore) o).score < score)
                return 1;
            else if (((HighScore) o).score == score)
                return 0;
            else
                return -1;
        }
        return -1;
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

    public SpriteDrawable[] getSprites() {
        return sprites;
    }
}
