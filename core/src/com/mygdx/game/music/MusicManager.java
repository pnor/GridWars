package com.mygdx.game.music;

/**
 * Class that managers music in the game. This controls what song is currently playing, and has methods for setting the volume for all songs in the game.
 * @author Phillip O'Reggio
 */
public class MusicManager {
    private Song currentSong;
    private boolean isPlaying;
    /**
     * Volume of the music played on a scale of 0 to 1 where 0 is muted and 1 is max volume
     */
    private float volume = 1;

    /**
     * Sets the current song and plays it
     */
    public void setSong(Song newSong) {
        if (currentSong != null) {
            currentSong.stop();
            currentSong.dispose();
        }
        currentSong = newSong;
        currentSong.setVolume(volume);
        currentSong.play();
        isPlaying = true;
    }

    /**
     * Stops the current song and disposes of it.
     */
    public void disposeSong() {
        currentSong.dispose();
        currentSong = null;
        isPlaying = false;
    }

    /**
     * Changes the volume of the music and changes the stored volume amount
     * @param newVolume new music volume
     */
    public void setMusicVolume(float newVolume) {
        volume = newVolume;
        if (currentSong != null)
            currentSong.setVolume(volume);
    }

    public Song getSong() {
        return currentSong;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

}
