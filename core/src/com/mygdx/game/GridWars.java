package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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
	public static HighScoreManager highScoreManager;

	@Override
	public void create() {
		stage = new Stage();
		stage.setViewport(new FitViewport(1000, 900));
		stage.getViewport().setScreenSize(1000, 900);
		engine = new Engine();
		//set up assets
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		skin.addRegions( new TextureAtlas("uiskin.atlas"));
		atlas = new TextureAtlas(Gdx.files.internal("GDSprites.pack"));
		backAtlas = new TextureAtlas(Gdx.files.internal("BackPack.pack"));
		//set up options related things
		Background.setAnimateBackground(Gdx.app.getPreferences("Options").getBoolean("Animate Background"));
		//set up highscore things
		highScoreManager = new HighScoreManager();
		highScoreManager.prepopulate();
		highScoreManager.saveHighScores();
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
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && Gdx.input.isKeyJustPressed(Input.Keys.TAB) && Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT))
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
	 * Sets the game to shade in grayscale
	 */
	public void setGrayScale() {
		stage.getBatch().setShader(new ShaderProgram(Gdx.files.internal("GrayscaleVertexShader"), Gdx.files.internal("GrayScaleFragmentShader")));
	}

	/**
	 * Sets the game to shade all colors inverted
	 */
	public void setInvertColor() {
		stage.getBatch().setShader(new ShaderProgram(Gdx.files.internal("GrayscaleVertexShader"), Gdx.files.internal("InvertFragmentShader")));
	}

	/**
	 * Sets the game to shade normally
	 */
	public void removeShader() {
		stage.getBatch().setShader(null);
	}
}
