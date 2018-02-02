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
import com.mygdx.game.screens.TitleScreen;
import com.mygdx.game.ui.Background;
import music.MusicManager;

public class GridWars extends Game {
	//public AssetManager assets = new AssetManager();
	public static Stage stage;
	public static Engine engine;
	public static Skin skin;
	public static TextureAtlas atlas;
	public static TextureAtlas backAtlas;

	//debug variables
	public static boolean DEBUG_halfSpeed;
	public static boolean DEBUG_doubleSpeed;

	//high scores
	public HighScoreManager highScoreManager;

	//Music
	public MusicManager musicManager;

	@Override
	public void create() {
		stage = new Stage();
		stage.setViewport(new FitViewport(1000, 900));
		stage.getViewport().setScreenSize(1000, 900);
		engine = new Engine();
		//set up assets
		skin = new Skin(Gdx.files.internal("fonts/uiskin.json"));
		skin.addRegions( new TextureAtlas("fonts/uiskin.atlas"));
		atlas = new TextureAtlas(Gdx.files.internal("spritesAndBackgrounds/GDSprites.pack"));
		backAtlas = new TextureAtlas(Gdx.files.internal("spritesAndBackgrounds/BackPack.pack"));
		//set up options if its first time
		initializeOptions();
		//set up music
		musicManager = new MusicManager();
		musicManager.setMusicVolume(Gdx.app.getPreferences("GridWars Options").getFloat("Music Volume"));
		//animate background if options permit
		Background.setAnimateBackground(Gdx.app.getPreferences("GridWars Options").getBoolean("Animate Background"));
		//prepopulate high scores if its the first time
			highScoreManager = new HighScoreManager();
		if (!highScoreManager.fileHandleExists()) {
			highScoreManager.prepopulate();
			highScoreManager.saveHighScores();
		}
		setScreen(new TitleScreen(this));
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (DEBUG_halfSpeed || DEBUG_doubleSpeed) {
			if (DEBUG_doubleSpeed && DEBUG_halfSpeed)
				getScreen().render(Gdx.graphics.getDeltaTime() * 1.5f);
			else if (DEBUG_doubleSpeed)
				getScreen().render(Gdx.graphics.getDeltaTime() * 2);
			else if (DEBUG_halfSpeed)
				getScreen().render(Gdx.graphics.getDeltaTime() * .5f);
		} else
			getScreen().render(Gdx.graphics.getDeltaTime());
		//debug --
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) //escape to title
			setScreen(new TitleScreen(this));
		if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
			DEBUG_halfSpeed = !DEBUG_halfSpeed;
			System.out.println("---HALF SPEED = " + DEBUG_halfSpeed + "---");
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
			DEBUG_doubleSpeed = !DEBUG_doubleSpeed;
			System.out.println("+++DOUBLE SPEED = " + DEBUG_doubleSpeed + "+++");
		}
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
			preferences.flush();
		}
	}

	/**
	 * Sets the game to shade in grayscale
	 */
	public void setGrayScale() {
		stage.getBatch().setShader(new ShaderProgram(Gdx.files.internal("shaders/GrayscaleVertexShader"), Gdx.files.internal("shaders/GrayScaleFragmentShader")));
	}

	/**
	 * Sets the game to shade all colors inverted
	 */
	public void setInvertColor() {
		stage.getBatch().setShader(new ShaderProgram(Gdx.files.internal("shaders/GrayscaleVertexShader"), Gdx.files.internal("shaders/InvertFragmentShader")));
	}

	/**
	 * Sets the game to shade normally
	 */
	public void removeShader() {
		stage.getBatch().setShader(null);
	}
}
