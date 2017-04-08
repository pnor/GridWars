package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.screens_ui.BattleScreen;

public class GridWars extends Game {
	public AssetManager assets = new AssetManager();
	public SpriteBatch batch;
	public Texture img;
	public static TextureAtlas atlas;


	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		atlas = new TextureAtlas(Gdx.files.internal("GDSprites.pack"));
		createTesterBattleScreen();
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

	public void createTesterBattleScreen() {
		BattleScreen screen = new BattleScreen(this, 8,new Color(241f / 255, 201f / 255f, 119f / 255f, 1), new Color(1, 1, 102f / 255f, 1));
		setScreen(screen);

		Array<Entity> teamA = new Array<Entity>();
		teamA.add(EntityConstructor.testerChessPiece(0, screen, screen.getEngine(), screen.getStage()));

		Array<Entity> teamB = new Array<Entity>();
		teamB.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));
		teamB.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));
		teamB.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));
		teamB.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));

		Array<Entity> teamC = new Array<Entity>();
		teamC.add(EntityConstructor.testerHole(-1, screen, screen.getEngine(), screen.getStage()));

		screen.setTeams(teamA, teamB, teamC);

		BoardComponent.boards.add(teamA.get(0), new BoardPosition(0, 3));

		BoardComponent.boards.add(teamB.get(0), new BoardPosition(3, 2));
		BoardComponent.boards.add(teamB.get(1), new BoardPosition(3, 3));
		BoardComponent.boards.add(teamB.get(2), new BoardPosition(3, 1));
		BoardComponent.boards.add(teamB.get(3), new BoardPosition(2, 2));

		BoardComponent.boards.add(teamC.get(0), new BoardPosition(1, 0));
	}
}
