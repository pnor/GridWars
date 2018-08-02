package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.highscores.HighScoreManager;
import com.mygdx.game.highscores.SaveDataManager;
import com.mygdx.game.music.MusicManager;
import com.mygdx.game.screens.TitleScreen;
import com.mygdx.game.ui.Background;

public class GridWars extends Game {
	//public AssetManager assets = new AssetManager();
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

	@Override
	public void create() {
		stage = new Stage();
		stage.setViewport(new FitViewport(1000, 900));
		stage.getViewport().setScreenSize(1000, 900);
		engine = new Engine();
		// set up assets
		skin = new Skin(Gdx.files.internal("fonts/uiskin.json"));
		skin.addRegions( new TextureAtlas("fonts/uiskin.atlas"));
		atlas = new TextureAtlas(Gdx.files.internal("spritesAndBackgrounds/GDSprites.pack"));
		backAtlas = new TextureAtlas(Gdx.files.internal("spritesAndBackgrounds/BackPack.pack"));
		// set up options if its first time
		initializeOptions();
		// set up music
		musicManager = new MusicManager();
		musicManager.setMusicVolume(Gdx.app.getPreferences("GridWars Options").getFloat("Music Volume"));
		// animate background if options permit
		Background.setAnimateBackground(Gdx.app.getPreferences("GridWars Options").getBoolean("Animate Background"));
		// set up high scores
		highScoreManager = new HighScoreManager();
		if (!highScoreManager.fileHandleExists()) {
			highScoreManager.prepopulate();
			highScoreManager.saveHighScores();
		}
		// set up save data
		saveDataManager = new SaveDataManager();

		//region Set up crashlogs
		/*
		// Prints out the sources of game crashes.
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
		*/

		//endregion

		setScreen(new TitleScreen(this));
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getScreen().render(Gdx.graphics.getDeltaTime() * multiplier);

		//debug --
		/*
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) //escape to title
			setScreen(new TitleScreen(this));
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
		Preferences preferences = Gdx.app.getPreferences("GridWars Options");
		//put in option variables if this is the first time
		if (preferences.getBoolean("Not First Time") == false) {
			preferences.putBoolean("Not First Time", true);
			preferences.putBoolean("Move Animation", true);
			preferences.putInteger("AI Turn Speed", 1);
			preferences.putBoolean("Animate Background", true);
			preferences.putFloat("Music Volume", .5f);
			preferences.putBoolean("Beat the Game", false);
			preferences.flush();
		}
	}

	/**
	 * Sets the game to shade in grayscale
	 */
	public void setGrayScale() {
		stage.getBatch().setShader(new ShaderProgram(Gdx.files.internal("shaders/GrayscaleVertexShader"), Gdx.files.internal("shaders/GrayscaleFragmentShader")));
	}

	/**
	 * Sets the game to shade all colors inverted
	 */
	public void setInvertColor() {
		stage.getBatch().setShader(new ShaderProgram(Gdx.files.internal("shaders/GrayscaleVertexShader"), Gdx.files.internal("shaders/InvertFragmentShader")));
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
