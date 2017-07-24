package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.game.screens_ui.screens.TitleScreen;

public class GridWars extends Game {
	//public AssetManager assets = new AssetManager();
	public static Stage stage;
	public static Engine engine;
	public static Skin skin;
	public static TextureAtlas atlas;
	public static TextureAtlas backAtlas;

	@Override
	public void create() {
		stage = new Stage();
		stage.getViewport().setWorldSize(1000, 900);
		stage.getViewport().setScreenSize(1000, 900);
		engine = new Engine();
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		skin.addRegions( new TextureAtlas("uiskin.atlas"));
		atlas = new TextureAtlas(Gdx.files.internal("GDSprites.pack"));
		backAtlas = new TextureAtlas(Gdx.files.internal("BackPack.pack"));
		setScreen(new TitleScreen(this));
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getScreen().render(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void setScreen(Screen screen) {
		stage.clear();
		engine.removeAllEntities();
		super.setScreen(screen);
	}
	
	@Override
	public void dispose () {
		stage.dispose();
	}

	public Batch getBatch() {
		return stage.getBatch();
	}
}
