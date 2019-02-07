package com.mygdx.game.music;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;
import java.lang.Thread;

/**
 * Manages sounds played throughout the game.
 * @author Phillip O'Reggio
 */
public class GameSoundManager implements Runnable {

    private AssetManager assetManager;
    private ObjectSet<SoundInfo> loadedSounds;
    private Queue<PlayedSoundConfig> queuedSounds;

    private ObjectMap<Long, Sound> loopingSoundIDs;
    private Array<SoundInfo> coreSounds; /** Sounds used frequently enough to not be unloaded */
    private float volume = 1f;

    // Handling the Queue
    private boolean useQueue = false;
    private boolean queueThreadRunning = false;
    private boolean forceStop = false;
    private final long QUEUE_TIME = 60;
    private final long QUEUE_DOWNTIME = 500; // How many milliseconds should queue wait till ending the thread
    private long lastTime = 0;


    public GameSoundManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.loadedSounds = new ObjectSet();
        this.loopingSoundIDs = new ObjectMap();
        this.queuedSounds = new Queue();
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
        if (useQueue) {
            queuedSounds.addLast(new PlayedSoundConfig(info));
            if (!queueThreadRunning) {
                startQueueThread();
            }
        } else {
            makeSound(info);
        }
    }

    /**
     * Plays a sound based on the provided {@link SoundInfo} that can loop until terminated with {@link stopAllLoopingSounds} 
     */
    public void playSound(SoundInfo info, boolean looping) {
        if (useQueue) {
            queuedSounds.addLast(new PlayedSoundConfig(info, looping));
            if (!queueThreadRunning) {
                startQueueThread();
            }
        } else {
            makeSound(info, looping);
        }
    }

    /**
     * Plays a modified sound based on the provided {@link SoundInfo} with an adjustable pitch, pan, or volume.
     * @param sound The Sound Information
     * @param pitch pitch multiplier. Values outside of [0.5, 2] are changed to 1
     * @param pan whether the sound will be played more to one side. values outside of [-1, 1] are set to 0
     * @param volumeModifier a number between 0 and 1 that determines what fraction of the norma volume this sound will be played at. Applied multiplicatively.
     */
    public void playSound(SoundInfo info, float pitch, float pan, float volumeModifier) {
        if (useQueue) {
            queuedSounds.addLast(new PlayedSoundConfig(info, pitch, pan, volumeModifier));
            if (!queueThreadRunning) {
                startQueueThread();
            }
        } else {
            makeSound(info, pitch, pan, volumeModifier);
        }
    }

    /**
     * Plays a sound based on the provided {@link SoundInfo}. Called internally by this class
     */
    private void makeSound(SoundInfo info) {
        Sound sound = getSoundFromInfo(info, true);
        if (sound != null) {
            sound.play(volume);
        }
    }

    /**
     * Plays a modified sound based on the provided {@link SoundInfo}. Called internally by this class
     */
    private void makeSound(SoundInfo info, float pitch, float pan, float volumeModifier) {
        Sound sound = getSoundFromInfo(info, true);
        if (sound != null) {
            float effectivePitch = pitch < 0.5 || pitch > 2 ? 1 : pitch;
            float effectiveVolume = (int) volumeModifier == -999 ? volume : MathUtils.clamp(volume * volumeModifier, 0, 1);
            float effectivePan = pan > 1 || pan < -1 ? 0 : pan;
            long id = sound.play(effectiveVolume);
            sound.setPitch(id, effectivePitch);
            sound.setPan(id, effectivePan, effectiveVolume);
        }
    }

    /**
     * Plays a sound based on the provided {@link SoundInfo} that can loop until terminated with {@link stopAllLoopingSounds} . Called internally by this class.
     */
    private void makeSound(SoundInfo info, boolean looping) {
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
        for (SoundInfo info : loadedSounds) {
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
                return assetManager.get(info.FILE_PATH, Sound.class);
            } else {
                assetManager.load(info.FILE_PATH, Sound.class);
                assetManager.finishLoading();
                if (!loadedSounds.contains(info)) {
                    loadedSounds.add(info);
                }
                return assetManager.get(info.FILE_PATH, Sound.class);
            }
        } else {
            return assetManager.get(info.FILE_PATH, Sound.class);
        }
    }

    // Managing Queue + Multithreading
    /**
     * Checks to see if enough time has elapsed before playing another sound.
     */
    @Override
    public void run() {
        while (true) {
            // Check if queue thread has been stopped OR if the queue is empty and enough time has passed.
            synchronized(this) {
                if (forceStop || (System.currentTimeMillis() - lastTime >= QUEUE_DOWNTIME && queuedSounds.size == 0)) {
                    queueThreadRunning = false;
                    forceStop = false;
                    return;
                }
            }
 
            // If enough time has passed play the next sound effect
            if (System.currentTimeMillis() - lastTime >= QUEUE_TIME && queuedSounds.size != 0) {
                playSoundInQueue(queuedSounds.first());
                queuedSounds.removeFirst();
                lastTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * Starts a new thread to play sounds using the queue.
     */
    private void startQueueThread() {
        lastTime = System.currentTimeMillis();
        queueThreadRunning = true;
        lastTime = -QUEUE_TIME;
        Thread queueThread = new Thread(this);
        queueThread.start();
    }

    /**
     * Stops the queue thread. (will finish playing sounds if it was about to)
     */
    private void stopQueueThread() {
        forceStop = true;
    }

    /**
     * Plays a sound effect using the info in the queue.
     */
    private void playSoundInQueue(PlayedSoundConfig soundConfig) {
        if (soundConfig.loops) {
            makeSound(soundConfig.info, true);
        } else {
            makeSound(soundConfig.info, soundConfig.pitch, soundConfig.pan, soundConfig.volume);
        }
    }

    /**
     * Starts queue mode. Later sound effects will be played in a queue
     */
    public void startQueueMode() {
        useQueue = true;
    }

    /**
     * Clears all queued sound effects.
     */
    public void endQueueMode() {
        stopQueueThread();
        while (queueThreadRunning) { } // Block thread until queue thread ends
        queuedSounds.clear();
        useQueue = false;
    }

    public boolean isQueueThreadRunning() {
        return queueThreadRunning;
    }

    public void printQueuedSounds() {
        System.out.println(queuedSounds);
    }

    /**
     * Stores what sound is to be played in the qeueue and with what modifications it should be played at.
     */
    private class PlayedSoundConfig {
        public SoundInfo info;
        public float pitch;
        public float pan; 
        public float volume;
        public boolean loops;

        public PlayedSoundConfig(SoundInfo info, float pitch, float pan, float volume) {
            this.info = info;
            this.pitch = pitch;
            this.pan = pan;
            this.volume = volume;
        }

        public PlayedSoundConfig(SoundInfo info, boolean loops) {
            this.info = info;
            this.pitch = -999;
            this.pan = -999;
            this.volume = -999;
            this.loops = loops;
        }

        public PlayedSoundConfig(SoundInfo info) {
            this.info = info;
            this.pitch = -999;
            this.pan = -999;
            this.volume = -999;
            this.loops = false;
        }
    }
}