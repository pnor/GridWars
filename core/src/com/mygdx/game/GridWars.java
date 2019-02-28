package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.GridWarsPreferences;
import com.mygdx.game.highscores.HighScoreManager;
import com.mygdx.game.highscores.SaveDataManager;
import com.mygdx.game.music.GameSoundManager;
import com.mygdx.game.music.MusicManager;
import com.mygdx.game.screens.TitleScreen;
import com.mygdx.game.ui.Background;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GridWars extends Game {
	public AssetManager assetManager;
	public static Stage stage;
	public static Engine engine;
	public static Skin skin;
	public static TextureAtlas atlas;
	public static TextureAtlas backAtlas;


	//debug variables
	/**
	 * 0 : .5x <p>
	 * 1 : normal <p>
	 * 2 : 1.5x <p>
	 * 3 : 2x <p>
	 * 4 : 3x <p>
	 */
	private static byte gameSpeed = 1;
	private float multiplier = 1;

	//high scores
	public HighScoreManager highScoreManager;
	//save data
	public SaveDataManager saveDataManager;

	//Music
	public MusicManager musicManager;

	//Sound FX
	public GameSoundManager soundManager;

	// File Paths
	final private static String UI_SKIN_JSON = "fonts/uiskin.json";
	final private static String UI_SKIN_ATLAS = "fonts/uiskin.atlas";
	final private static String SPRITE_SHEET = "spritesAndBackgrounds/GDSprites.pack";
	final private static String BACKGROUND_SPRITE_SHEET = "spritesAndBackgrounds/BackPack.pack";

	@Override
	public void create() {
		stage = new Stage();
		stage.setViewport(new FitViewport(1000, 900));
		stage.getViewport().setScreenSize(1000, 900);
		engine = new Engine();
		// set up Asset Manager
		assetManager = new AssetManager();
		assetManager.load(UI_SKIN_JSON, Skin.class);
		assetManager.load(UI_SKIN_ATLAS, TextureAtlas.class);
		assetManager.load(SPRITE_SHEET, TextureAtlas.class);
		assetManager.load(BACKGROUND_SPRITE_SHEET, TextureAtlas.class);
		assetManager.finishLoading();

		// set up assets
		skin = assetManager.get(UI_SKIN_JSON, Skin.class);
		skin.addRegions(assetManager.get(UI_SKIN_ATLAS, TextureAtlas.class));
		atlas = assetManager.get(SPRITE_SHEET, TextureAtlas.class);
		backAtlas = assetManager.get(BACKGROUND_SPRITE_SHEET, TextureAtlas.class);
		ShaderProgram.pedantic = false;
		// set up options if its first time
		initializeOptions();
		// set up music
		musicManager = new MusicManager();
		musicManager.setMusicVolume(Gdx.app.getPreferences(GridWarsPreferences.GRIDWARS_OPTIONS).getFloat(GridWarsPreferences.MUSIC_VOLUME));

		soundManager = new GameSoundManager(assetManager);
		soundManager.setVolume(Gdx.app.getPreferences(GridWarsPreferences.GRIDWARS_OPTIONS).getFloat(GridWarsPreferences.SOUND_FX_VOLUME));
		// animate background if options permit
		Background.setAnimateBackground(Gdx.app.getPreferences(GridWarsPreferences.GRIDWARS_OPTIONS).getBoolean(GridWarsPreferences.ANIMATE_BACKGROUND));
		// set up high scores
		highScoreManager = new HighScoreManager();
		if (!highScoreManager.fileHandleExists()) {
			highScoreManager.prepopulate();
			highScoreManager.saveHighScores();
		}
		// set up save data
		saveDataManager = new SaveDataManager();

		// Prompt Asset Manager to finish all loading (for menu sounds)
		assetManager.finishLoading();

		// Prints out the sources of game crashes.
		//enableCrashReports();

		setScreen(new TitleScreen(this));
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getScreen().render(Gdx.graphics.getDeltaTime() * multiplier);
		// Load Assets if not done yet
		assetManager.update();

		//region DEBUG
		/*
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) { //escape to title
			setScreen(new TitleScreen(this));
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) { //game speed
			setGameSpeed((byte) (gameSpeed + 1));
			System.out.println("Game Speed : " + gameSpeed);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) { //music info
			System.out.println("MUSIC DEBUG:" +
					"Music Volume : " + musicManager.getSong().getVolume() + "\n" +
					"Music loops? : " + musicManager.getSong().getLooping() + "\n" +
					"Music opener? : " + musicManager.getSong().getHasOpener() + "\n" +
					"Playing Opener? : " + musicManager.getSong().getPlayingOpener() + "\n" +
					"Music Position : " + musicManager.getSong().getMusic().getPosition());
		}
		if (Gdx.input.isKeyPressed(Input.Keys.F3)) { //print cursor location
			System.out.println("Mouse X : " + Gdx.input.getX());
			System.out.println("Mouse Y : " + Gdx.input.getY());
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
			System.out.println(Gdx.graphics.getFramesPerSecond());
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) { // throw an exception
			//region Divide by Zero
			//int inconceivable = 1 / 0;
			//endregion
			//region  Array out of Bounds
			int[] troublesome = new int[0];
			System.out.println(troublesome[1]);
			//endregion
		}
		*/
		//endregion
	}

	@Override
	public void setScreen(Screen screen) {
		stage.clear();
		engine.removeAllEntities();
		super.setScreen(screen);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void dispose () {
		stage.dispose();
	}

	public Batch getBatch() {
		return stage.getBatch();
	}

	/**
	 * Initializes the values of the options menu if this is the first time playing. Does nothing if this is not the first
	 * time playing.
	 */
	public void initializeOptions() {
		Preferences preferences = Gdx.app.getPreferences(GridWarsPreferences.GRIDWARS_OPTIONS);
		//put in option variables if this is the first time
		if (preferences.getBoolean(GridWarsPreferences.NOT_FIRST_TIME) == false) {
			resetOptions();
			preferences.putBoolean(GridWarsPreferences.BEAT_THE_GAME, false);
			preferences.flush();
		}
	}

	/**
	 * Resets the values of the options menu to default values. Does not change BEAT_THE_GAME. (NOT_FIRST_TIME will be true though)
	 */
	public void resetOptions() {
		Preferences preferences = Gdx.app.getPreferences(GridWarsPreferences.GRIDWARS_OPTIONS);

		preferences.putBoolean(GridWarsPreferences.NOT_FIRST_TIME, true);
		preferences.putBoolean(GridWarsPreferences.MOVE_ANIMATION, GridWarsPreferences.DEFAULT_MOVE_ANIMATION);
		preferences.putInteger(GridWarsPreferences.AI_TURN_SPEED, GridWarsPreferences.DEFAULT_AI_TURN_SPEED);
		preferences.putBoolean(GridWarsPreferences.ANIMATE_BACKGROUND, GridWarsPreferences.DEFAULT_ANIMATE_BACKGROUND);
		preferences.putFloat(GridWarsPreferences.MUSIC_VOLUME, GridWarsPreferences.DEFAULT_MUSIC);
		preferences.putFloat(GridWarsPreferences.SOUND_FX_VOLUME, GridWarsPreferences.DEFAULT_SOUND_FX);
		preferences.flush();
	}

	/** Calling this will make the stack trace saved to assets/GWcrashlogs/ */
	private void enableCrashReports() {
		FileHandle crashDirectory = new FileHandle("GWcrashlogs");
		if (!crashDirectory.exists()) {
			FileHandle newCrashDirectory = new FileHandle("GWcrashlogs/info");
			newCrashDirectory.writeString("This file was created so GridWars can write errors to this directory.", false);
		}
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

				String filename = "GWcrashlogs/"+sdf.format(cal.getTime())+".txt";

				PrintStream writer;
				try {
					writer = new PrintStream(filename, "UTF-8");
					writer.println(e.getClass() + ": " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						writer.println(e.getStackTrace()[i].toString());
					}

				} catch (FileNotFoundException | UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}

	/**
	 * Sets the game to shade in grayscale
	 */
	public void setGrayScale() {
		stage.getBatch().setShader(Shaders.GRAYSCALE.getShaderProgram());
	}

	/**
	 * Sets the game to shade all colors inverted
	 */
	public void setInvertColor() {
		stage.getBatch().setShader(Shaders.INVERT.getShaderProgram());
	}

	/**
	 * Sets the multiplier to render speed. Restrains the value within 0 to 4
	 * @param speed new speed <p>
	 * 0 : .5x <p>
	 * 1 : normal <p>
	 * 2 : 1.5x <p>
	 * 3 : 2x <p>
	 * 4 : 3x <p>
	 */
	public void setGameSpeed(byte speed) {
		gameSpeed = (byte) (speed % 5);
		if (gameSpeed == 0)
			multiplier = .5f;
		else if (gameSpeed == 1)
			multiplier = 1f;
		else if (gameSpeed == 2)
			multiplier = 1.5f;
		else if (gameSpeed == 3)
			multiplier = 2f;
		else if (gameSpeed == 4)
			multiplier = 3f;
	}

	/**
	 * Sets the game to shade normally
	 */
	public void removeShader() {
		stage.getBatch().setShader(null);
	}

	public byte getGameSpeed() {
		return gameSpeed;
	}
}

enum Shaders {
	GRAYSCALE("shaders/GrayscaleVertexShader", "shaders/GrayscaleFragmentShader"),
	INVERT("shaders/GrayscaleVertexShader", "shaders/InvertFragmentShader");

	private String vertexFilePath;
	private String fragmentFilePath;

	Shaders(String vertexFilePath, String fragmentFilePath) {
		this.vertexFilePath = vertexFilePath;
		this.fragmentFilePath = fragmentFilePath;
	}

	public String getVertexFilePath() {
		return vertexFilePath;
	}

	public String getFragmentFilePath() {
		return fragmentFilePath;
	}

	public ShaderProgram getShaderProgram() {
		return new ShaderProgram(Gdx.files.internal(vertexFilePath), Gdx.files.internal(fragmentFilePath));
	}
}
