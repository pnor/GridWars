package music;

/**
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
     * Plays the menu theme
     */
    public void makeSongAndPlayIt() {
        currentSong = Song.PASSWORD;
        isPlaying = true;
        currentSong.play();
    }

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
        currentSong.stop();
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

    public boolean getIsPlaying() {
        return isPlaying;
    }

}
