package com.mygdx.game.music;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Class with enums representing each song. Songs with openers use {@link com.badlogic.gdx.audio.Music.OnCompletionListener} to switch to the rest of the song.
 * Has methods for switching stopping, starting, and resetting music.
 * @author Phillip O'Reggio
 */
public class Song {
    private boolean hasOpener;
    private boolean loops;
    private boolean playingOpener;
    private Music opener;
    private Music music;

    public final byte ID;

    /**
     * Creates a song from the provided information
     */
    public Song(SongInfo songInfo) {
        if (songInfo.INTRO_PATH != null) {
            hasOpener = true;
            playingOpener = true;
            opener = Gdx.audio.newMusic(Gdx.files.internal(songInfo.INTRO_PATH));
            opener.setOnCompletionListener(music1 -> {
                opener.stop();
                music.play();
            });
        } 
        music = Gdx.audio.newMusic(Gdx.files.internal(songInfo.MAIN_PATH));
        music.setLooping(songInfo.LOOPS);
        ID = songInfo.ID;
    }

    /**
     * Starts over the song, and plays it.
     */
    public void play() {
        startOver();
        if (hasOpener) {
            playingOpener = true;
            opener.setOnCompletionListener(music1 -> {
                opener.stop();
                music.play();
            });
            opener.play();
        } else {
            music.play();
        }
    }

    /**
     * Sets the song's playback time to 0.
     */
    public void startOver() {
        if (hasOpener) {
            opener.setPosition(0);
        }
        music.setPosition(0);
    }

    /**
     * Stops the song
     */
    public void stop() {
        if (playingOpener)
            opener.stop();
        else
            music.stop();
    }

    /**
     * Disposes of the song. This class should not be used after that happens.
     */
    public void dispose() {
        if (hasOpener) {
            opener.stop();
            opener.dispose();
        }
        music.stop();
        music.dispose();
        playingOpener = false;
    }

    public void setVolume(float volume) {
        if (hasOpener)
            opener.setVolume(volume);
        music.setVolume(volume);
    }

    public float getVolume() {
        return music.getVolume();
    }

    public boolean getLooping() {
        return loops;
    }

    public boolean getHasOpener() {
        return hasOpener;
    }

    public boolean getPlayingOpener() {
        return playingOpener;
    }

    public Music getMusic() {
        return music;
    }

    public Music getOpener() {
        return opener;
    }
}
