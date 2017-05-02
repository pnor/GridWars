package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.rules_types.ZoneRules;
import com.mygdx.game.screens_ui.screens.BattleScreen;
import com.mygdx.game.screens_ui.screens.TitleScreen;

import static com.mygdx.game.ComponentMappers.status;

public class GridWars extends Game {
	//public AssetManager assets = new AssetManager();
	public static Stage stage;
	public static Engine engine;
	public static Skin skin;
	public static TextureAtlas atlas;
	public static TextureAtlas backAtlas;

	@Override
	public void create () {
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
	public void render () {
		getScreen().render(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
	}
	
	@Override
	public void dispose () {
		stage.dispose();
		//img.dispose();
	}

	public void createTesterBattleScreen() {
		BattleScreen screen = new BattleScreen(this, 7, new Color(241f / 255, 201f / 255f, 119f / 255f, 1), new Color(1, 1, 102f / 255f, 1));
		setScreen(screen);

		Array<Entity> a = new Array<Entity>();
		a.add(EntityConstructor.testerChessPiece(0, screen, screen.getEngine(), stage));
		Team teamA = new Team("Star", Color.RED, false, a);

		Array<Entity> b = new Array<Entity>();
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), stage));
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), stage));
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), stage));
		b.add(EntityConstructor.testerRobot(1, screen, screen.getEngine(), stage));
		status.get(b.get(3)).burn(b.get(3));
		status.get(b.get(1)).petrify(b.get(1));
		status.get(b.get(0)).burn(b.get(0));
		status.get(b.get(0)).paralyze(b.get(0));
		status.get(b.get(0)).poison(b.get(0));
		status.get(b.get(0)).still(b.get(0));
		Team teamB = new Team("Team Blue", Color.CYAN, false, b);

		Array<Entity> c = new Array<Entity>();
		c.add(EntityConstructor.testerHole(-1, screen, screen.getEngine(), stage));
		Team teamC = new Team(false, c);

		screen.setTeams(teamA, teamB, teamC);

		BoardComponent.boards.add(a.get(0), new BoardPosition(0, 3));

		BoardComponent.boards.add(b.get(0), new BoardPosition(3, 2));
		BoardComponent.boards.add(b.get(1), new BoardPosition(3, 3));
		BoardComponent.boards.add(b.get(2), new BoardPosition(3, 1));
		BoardComponent.boards.add(b.get(3), new BoardPosition(2, 2));

		BoardComponent.boards.add(c.get(0), new BoardPosition(1, 1));
		if (screen.getRules() instanceof ZoneRules)
			((ZoneRules) screen.getRules()).colorZones();
	}

	public Batch getBatch() {
		return stage.getBatch();
	}
}
