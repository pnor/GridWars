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
import com.mygdx.game.screens_ui.Background;
import com.mygdx.game.screens_ui.screens.TitleScreen;

public class GridWars extends Game {
	//public AssetManager assets = new AssetManager();
	public static Stage stage;
	public static Engine engine;
	public static Skin skin;
	public static TextureAtlas atlas;
	public static TextureAtlas backAtlas;

	//debug variables
	public static boolean DEBUG_halfSpeed;

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

		setScreen(new TitleScreen(this));
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (DEBUG_halfSpeed)
			getScreen().render(Gdx.graphics.getDeltaTime() / 2);
		else
			getScreen().render(Gdx.graphics.getDeltaTime());
		//debug
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
			setScreen(new TitleScreen(this));
		if (Gdx.input.isKeyJustPressed(Input.Keys.TAB))
			DEBUG_halfSpeed = !DEBUG_halfSpeed;
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
