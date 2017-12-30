package com.mygdx.game.highscores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/**
 * Class that manages the highscore table. Is used to retrieve and change values from the highscore table
 * @author Phillip O'Reggio
 */
public class HighScoreManager {
    //high score related
    private FileHandle highscoreFile;
    private Array<HighScore> highScores;
    /** Total amount of highscores this game will store */
    private final int maxEntries = 5;
    /** Is true if the Array is the same as the one stored in the High score json file */
    private boolean sameAsFile;

    public HighScoreManager() {
        highscoreFile = Gdx.files.local("GridWarsHighScores.json");
        updateHighScoresWithFile();
    }

    /**
     * Updates the high score objects stored in this file with the data in GridWarsHighScores.json
     */
    public void updateHighScoresWithFile() {
        Json json = new Json();
        highscoreFile = Gdx.files.local("GridWarsHighScores.json");
        String jsonScores = highscoreFile.readString();
        highScores = json.fromJson(Array.class, jsonScores);
        sameAsFile = true;
    }

    /**
     * Saves the scores stored in this object to GridWarsHighScores.json
     */
    public void saveHighScores() {
        Json json = new Json();
        highscoreFile.writeString(json.prettyPrint(highScores), false);
        sameAsFile = true;
    }

    /**
     * Adds a highscore if it is higher than or equal to a score currently in the list. Truncates the list to the size specified by max entries.
     * @param score being added
     */
    public void addHighScoreObject(HighScore score) {
        highScores.add(score);
        highScores.sort();
        highScores.truncate(maxEntries);
        /*
        for (int i = 0; i < highScores.size; i++) {
            if (highScores.get(i).getScore() <= score.getScore()) {
                highScores.set(i, score);
                sameAsFile = false;
            }
        }
        highScores.truncate(maxEntries);
        */
        highScores.add(score);
    }

    public void prepopulate() {
        highScores = new Array<>();
        highScores.add(new HighScore("Poor Party", 1, 0, 1, -2, -2, -2, -2));
        highScores.add(new HighScore("Mediocre Mashup", 10, 100, 1, -2, -2, -2, -2));
        highScores.add(new HighScore("Adequate Allies", 20, 500, 1, -2, -2, -2, -2));
        highScores.add(new HighScore("Good Group", 30, 1000, 1, -2, -2, -2, -2));
        highScores.add(new HighScore("Expert E", 51, 51, 51, -2, -2, -2, -2));
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
}
