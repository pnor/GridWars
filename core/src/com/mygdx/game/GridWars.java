package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.screens_ui.BattleScreen;

public class GridWars extends Game {
	public SpriteBatch batch;
	public Texture img;
	public static TextureAtlas atlas;


	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		atlas = new TextureAtlas(Gdx.files.internal("GDSprites.pack"));
		setScreen(new BattleScreen());
	}

	@Override
	public void render () {
		/*
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
		*/
		getScreen().render(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}
}
