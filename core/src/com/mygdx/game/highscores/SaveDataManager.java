package com.mygdx.game.highscores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.rules_types.Team;

/**
 * Class that manages the save files. Is used to retrieve and change the data saved
 * @author Phillip O'Reggio
 */
public class SaveDataManager {
    private final FileHandle SAVE_DATA_FILE;
    private SaveData savedData;
    /**
     * Becomes false if any changes is made without saving it
     */
    private boolean sameAsFile;

    public SaveDataManager() {
        SAVE_DATA_FILE = Gdx.files.local("GridWarsSavedData/GridWarsSurvivalSave.json");
        if (SAVE_DATA_FILE.exists()) {
            try {
                updateSaveDataWithFile();
            } catch (SerializationException e) { //cant read file
            //replace it with defaults
            prepopulate();
            saveSavedData();
            }
        } else {
            prepopulate();
            saveSavedData();
        }
    }

    /**
     * @return True if the file saved is loadable. False otherwise
     */
    public boolean fileIsLoadable() {
        if (sameAsFile)
            return savedData.canLoadFile();
        else {
            System.out.println("Called fileIsLoadable and this hasn't updated yet; must update");
            saveSavedData();
            return savedData.canLoadFile();
        }
    }

    /**
     * Makes the save file unloadable
     */
    public void makeFileUnloadable() {
        savedData.setLoadable(false);
        sameAsFile = false;
    }

    /**
     * Updates the high score objects stored in this file with the data in GridWarsSavedData/GridWarsSurvivalSave.json
     */
    public void updateSaveDataWithFile() {
        Json json = new Json();
        String jsonData = SAVE_DATA_FILE.readString();
        savedData = json.fromJson(SaveData.class, jsonData);
        sameAsFile = true;
    }

    /**
     * Saves the scores stored in this object to GridWarsHighScores.json
     */
    public void saveSavedData() {
        Json json = new Json();
        SAVE_DATA_FILE.writeString(json.prettyPrint(savedData), false);
        sameAsFile = true;
    }

    /**
     * Sets the saved data with a new {@link SaveData} object
     */
    public void setSavedData(SaveData data) {
        savedData = data;
        sameAsFile = false;
    }

    /**
     * @return Team that the saved data is storing
     */
    public Team getTeamFromData() {
        return savedData.createTeam();
    }

    /**
     * Prepopulates the high score file with a dummy file representing nothing being saved.
     */
    public void prepopulate() {
        Team team = new Team("Shouldn't be loadable", Color.BLACK);
        team.addEntity(EntityConstructor.canman(0));
        savedData = new SaveData(team, 0, 0, 0, 0, 1);
        savedData.setLoadable(false);
        sameAsFile = false;
    }

    public int getHealthPowerUps() {
        return savedData.getHPPower();
    }

    public int getSPPowerUps() {
        return savedData.getSPPower();
    }

    public int getPoints() {
        return savedData.getPoints();
    }

    public int getTurns() {
        return savedData.getTotalTurns();
    }

    public int getFloor() {
        return savedData.getFloor();
    }

    /**
     * @return whether the Array stored in this object and the Array represented by the json file are the same
     */
    public boolean isSynced() {
        return sameAsFile;
    }

    public SaveData getSavedData() {
        return savedData;
    }

    /**
     * @return whether the high score file exists (if it doesn't that means this it was deleted or this is their first time playing)
     */
    public boolean fileHandleExists() {
        return SAVE_DATA_FILE.exists();
    }
}
