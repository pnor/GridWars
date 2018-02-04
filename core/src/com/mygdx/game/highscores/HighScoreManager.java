package com.mygdx.game.highscores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/**
 * Class that manages the high score table. Is used to retrieve and change values from the highscore table
 * @author Phillip O'Reggio
 */
public class HighScoreManager {
    //high score related
    private final FileHandle HIGH_SCORE_FILE;
    private Array<com.mygdx.game.highscores.HighScore> highScores;
    /** Total amount of high scores this game will store */
    private final int maxEntries = 5;
    /** Is true if the Array is the same as the one stored in the High score json file */
    private boolean sameAsFile;

    public HighScoreManager() {
        HIGH_SCORE_FILE = Gdx.files.local("highscores/GridWarsHighScores.json");
        if (HIGH_SCORE_FILE.exists()) {
            updateHighScoresWithFile();
        }
    }

    /**
     * Updates the high score objects stored in this file with the data in highscores/GridWarsHighScores.json
     */
    public void updateHighScoresWithFile() {
        Json json = new Json();
        String jsonScores = HIGH_SCORE_FILE.readString();
        highScores = json.fromJson(Array.class, jsonScores);
        sameAsFile = true;
    }

    /**
     * Saves the scores stored in this object to GridWarsHighScores.json
     */
    public void saveHighScores() {
        Json json = new Json();
        HIGH_SCORE_FILE.writeString(json.prettyPrint(highScores), false);
        sameAsFile = true;
    }

    /**
     * Adds a high score if it is higher than or equal to high scores score currently in the list. Truncates the list to the size specified by max entries.
     * @param score being added
     */
    public void addHighScoreObject(HighScore score) {
        highScores.add(score);
        highScores.sort();
        //remove excess
        while (highScores.size > maxEntries) {
            highScores.removeIndex(maxEntries);
        }
        sameAsFile = false;
    }

    public void prepopulate() {
        highScores = new Array<>();
        highScores.add(new HighScore("Poor Party", 1, 0, 1, -2, -2, -2, -2));
        highScores.add(new HighScore("Mediocre Mashup", 100, 10, 5, -2, -2, -2, -2));
        highScores.add(new HighScore("Adequate Allies", 500, 20, 10, -2, -2, -2, -2));
        highScores.add(new HighScore("Good Group", 1000, 30, 15, -2, -2, -2, -2));
        highScores.add(new HighScore("Expert E", 5500, 40, 50, -2, -2, -2, -2));
        highScores.sort();
        sameAsFile = false;
    }

    public boolean checkHighScoreCanBeAdded(HighScore score) {
        return score.compareTo(getLowestScore()) == -1;
    }

    public HighScore getLowestScore() {
        return highScores.get(highScores.size - 1);
    }

    public HighScore getHighestScore() {
        return highScores.get(0);
    }

    public Array<HighScore> getHighScores() {
        return highScores;
    }

    /**
     * @return whether the Array stored in this object and the Array represented by the json file are the same
     */
    public boolean isSynced() {
        return sameAsFile;
    }

    /**
     * @return whether the high score file exists (if it doesn't that means this it was deleted or this is their first time playing)
     */
    public boolean fileHandleExists() {
        return HIGH_SCORE_FILE.exists();
    }
}
