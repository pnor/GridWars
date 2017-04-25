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
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.screens_ui.BattleScreen;

import static com.mygdx.game.ComponentMappers.status;

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

		Array<Entity> a = new Array<Entity>();
		a.add(EntityConstructor.testerChessPiece(0, screen, screen.getEngine(), screen.getStage()));
		Team teamA = new Team(false, a);
		status.get(a.get(0)).petrify(a.get(0));

		Array<Entity> b = new Array<Entity>();
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), screen.getStage()));
		status.get(b.get(3)).curse(b.get(3));
		status.get(b.get(2)).still(b.get(2));
		Team teamB = new Team("Team Blue", Color.CYAN, false, b);

		Array<Entity> c = new Array<Entity>();
		c.add(EntityConstructor.testerHole(-1, screen, screen.getEngine(), screen.getStage()));
		Team teamC = new Team(false, c);

		screen.setTeams(teamA, teamB, teamC);

		BoardComponent.boards.add(a.get(0), new BoardPosition(0, 3));

		BoardComponent.boards.add(b.get(0), new BoardPosition(3, 2));
		BoardComponent.boards.add(b.get(1), new BoardPosition(3, 3));
		BoardComponent.boards.add(b.get(2), new BoardPosition(3, 1));
		BoardComponent.boards.add(b.get(3), new BoardPosition(2, 2));

		BoardComponent.boards.add(c.get(0), new BoardPosition(1, 0));
	}
}
