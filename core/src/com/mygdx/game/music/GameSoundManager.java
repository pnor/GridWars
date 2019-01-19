package com.mygdx.game.music;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;


/**
 * Manages sounds played throughout the game.
 * @author Phillip O'Reggio
 */
public class GameSoundManager {

    private AssetManager assetManager;
    private ObjectMap<SoundInfo, Sound> loadedSounds;
    //private ObjectSet<SoundInfo> loadedSounds;
    private ObjectMap<Long, Sound> loopingSoundIDs;
    private Array<SoundInfo> coreSounds; /** Sounds used frequently enough to not be unloaded */
    private float volume = 1f;

    public GameSoundManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.loadedSounds = new ObjectMap();
        this.loopingSoundIDs = new ObjectMap();
        // Set coreSounds
        coreSounds = new Array(new SoundInfo[] {
            SoundInfo.CONFIRM, SoundInfo.BACK, SoundInfo.SELECT, SoundInfo.POWER,
            SoundInfo.TURN_SHIFT1, SoundInfo.TURN_SHIFT2
        });
        for (SoundInfo soundInfo : coreSounds) {
            this.assetManager.load(soundInfo.FILE_PATH, Sound.class);
        }
    }

    public void setVolume(float newVolume) {
        this.volume = MathUtils.clamp(newVolume, 0, 1);
    }

    /**
     * Plays a sound based on the provided {@link SoundInfo}
     */
    public void playSound(SoundInfo info) {
        Sound sound = getSoundFromInfo(info, true);
        if (sound != null) {
            sound.play(volume);
        }
    }
    /**
     * Plays a modified sound based on the provided {@link SoundInfo} with an adjustable pitch, pan, or volume.
     * @param sound The Sound Information
     * @param pitch pitch multiplier. a value of -999 means the sound will be played at a normal pitch
     * @param pan whether the sound will be played more to one side. A value of -999 means the sound will be played on both sides equally
     * @param volumeModifier a number between 0 and 1 that determines what fraction of the norma volume this sound will be played at. -999 means this will be ignored and played normally.
     */
    public void playSound(SoundInfo info, float pitch, float pan, float volumeModifier) {
        Sound sound = getSoundFromInfo(info, true);
        if (sound != null) {
            float effectivePitch = (int) pitch == -999? 1 : pitch;
            float effectiveVolume = (int) volumeModifier == -999 ? volume : MathUtils.clamp(volume * volumeModifier, 0, 1);
            float effectivePan = (int) pan == -999? 0 : pan;
            long id = sound.play(effectiveVolume);
            sound.setPitch(id, effectivePitch);
            sound.setPan(id, effectivePan, effectiveVolume);
        }
    }

    /**
     * Plays a sound based on the provided {@link SoundInfo} that can loop until terminated with {@link stopAllLoopingSounds} 
     */
    public void playSound(SoundInfo info, boolean looping) {
        Sound sound = getSoundFromInfo(info, true);
        if (sound != null) {
            long id = sound.play(volume);
            sound.setLooping(id, true);
            loopingSoundIDs.put(id, sound);
        }
    }

    /**
     * Stops all looping sound effects and clears the ObjectMap of looping Sounds.
     */
    public void stopAllLoopingSounds() {
        for (Long id : loopingSoundIDs.keys()) {
            loopingSoundIDs.get(id).setLooping(id, false);
        }
        loopingSoundIDs.clear();
    }

    /**
     * Makes the SoundManager unload all sounds not used in menus.
     */
    public void unloadSounds() {
        for (SoundInfo info : loadedSounds.keys()) {
            loadedSounds.get(info).stop();
            assetManager.unload(info.FILE_PATH);
        }
        loadedSounds.clear();
    }

    // Helper Methods
    /**
     * Gets the {@link Sound} class using the data in the SoundInfo. If checkIfLoaded is true, it will check
     * to make sure that the Sound is already loaded and stored in this class before requesting it from the assetManager.
     * If the sound is not already loaded, it will start loading it and return null. 
     * @return Ths sound corresponding to the {@link SoundInfo}. Can be null if the sound is not loaded.
     */
    private Sound getSoundFromInfo(SoundInfo info, boolean checkIfLoaded) {
        if (checkIfLoaded) {
            if (assetManager.isLoaded(info.FILE_PATH)) {
                return loadedSounds.get(info);
            } else {
                assetManager.load(info.FILE_PATH, Sound.class);
                assetManager.finishLoading();
                Sound newSound =  assetManager.get(info.FILE_PATH, Sound.class);
                if (!loadedSounds.containsKey(info)) {
                    loadedSounds.put(info, newSound);
                }
                return newSound;
            }
        } else {
            return assetManager.get(info.FILE_PATH, Sound.class);
        }
    }
}