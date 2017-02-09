package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.components.*;
import com.mygdx.game.systems.DrawingSystem;
import com.mygdx.game.systems.MovementSystem;

import java.awt.geom.Point2D;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;

/**
 * @author pnore_000
 */
public class BattleScreen implements Screen {


    private Stage stage;

    //Board
    private final Board board = new Board(7, 7, Color.LIGHT_GRAY, Color.DARK_GRAY);
    private final CodeBoard codeBoard = new CodeBoard(7, 7);

    //Selection ~~
    private Entity selectedEntity;
    private boolean checkedStats;

    //Ui Elements
    private Table table;
    private Table statsTable;
    private Skin skin;
    private TextureAtlas uiatlas;
    private Label NameLabel;
    private Label HpLabel;
    private Label SpLabel;
    private Label AtkLabel;
    private Label DefLabel;
    private Label SpdLabel;

    //Entities
    private Engine engine;
    private Entity tester;
    private Entity tester2;
    private Entity tester3;
    private Entity effect;
    private Entity effect2;

    //TEST VALUES
    private int t = 1; //x
    private int u = 1; //y
    public Actor TESTER;

    @Override
    public void show() {
        //AssetManager assets = new AssetManager();

        //set up assets
        stage = new Stage();
        stage.getViewport().setWorldSize(1000, 900);
        stage.getViewport().setScreenSize(1000, 900);
        table = new Table();
        statsTable = new Table();
        stage.addActor(table);
        stage.addActor(statsTable);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        uiatlas = new TextureAtlas("uiskin.atlas");
        skin.addRegions(uiatlas);
        Gdx.input.setInputProcessor(stage);

        //Set up Engine
        engine = new Engine();
        engine.addSystem(new DrawingSystem(stage.getBatch()));
        engine.addSystem(new MovementSystem());

        //set up Entity
        TESTER = new SpriteActor(atlas.createSprite("BluePiece"), 20, 20);

        BoardComponent.setBoardManager(new BoardManager(board, codeBoard));
        tester = new Entity();
        tester.add(new ActorComponent(new SpriteActor(atlas.createSprite("RedPiece"), true, true)));
        tester.add(new BoardComponent());
        tester.add(new StatComponent(10, 999, 7, 0, 1));

        tester2 = new Entity();
        tester2.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("Bot1"),
                atlas.findRegion("Bot2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        tester2.add(new BoardComponent());
        tester2.add(new StatComponent(5, 7, 2, 1, 3));
        tester2.add(new MovesetComponent(new Array<Move>(new Move[]{
                new Move("Tackle", tester2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0,1)}), new Attack() {
                    @Override
                    public void effect(Entity e, Array<BoardPosition> range, BoardManager boards) {

                    }
                }
        })));
        tester2.add(new NameComponent("Robo - Beta"));

        tester3 = new Entity();
        tester3.add(new ActorComponent(new AnimationActor(new TextureRegion[]{atlas.findRegion("Hole"),
                atlas.findRegion("Hole2"), atlas.findRegion("Hole3"), atlas.findRegion("Hole4")},
                Animation.PlayMode.LOOP_PINGPONG, 0.1f)));
        tester3.add(new BoardComponent());
        tester3.add(new NameComponent("Hole"));

        effect = new Entity();
        effect.add(new PositionComponent(new Point2D.Float(stage.getWidth() - 50, stage.getHeight() / 4), 100, 100, 0));
        effect.add(new SpriteComponent(atlas.findRegion("Star")));

        effect2 = new Entity();
        effect2.add(new PositionComponent(new Point2D.Float(stage.getWidth() / 3, stage.getHeight() / 3)
                , 100, 100, 0));
        effect2.add(new AnimationComponent(1, new TextureRegion[]{atlas.findRegion("Star1"),
                atlas.findRegion("Star2")}, Animation.PlayMode.LOOP));
        effect2.add(new MovementComponent(new Vector2(5, 1)));

        //put Entity on Board!
        BoardManager manage = bm.get(tester).boards;
        manage.add(tester, new BoardPosition(0, 0));
        manage.add(tester2, new BoardPosition(3, 3));
        manage.add(tester3, new BoardPosition(4, 0));
        engine.addEntity(effect);
        engine.addEntity(effect2);

        //set up MAIN ui -----
        for (int i = 0; i < board.getRowSize(); i++) {
            for (int j = 0; j < board.getColumnSize(); j++) {
                if (i == 0)
                    table.add().width(100).height(100);
                else
                    table.add().height(100);
            }
            table.row();
        }

        final TextButton act = new TextButton("ACT!", skin, "toggle");
        final TextButton move = new TextButton("Move to...", skin, "toggle");
        final TextField rowField = new TextField("row", skin);
        final TextField colField = new TextField("col", skin);
        ChangeListener change = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == act) {
                        bm.get(tester).boards.move(tester, new BoardPosition(t, u));
                        t++;
                        u++;
                        if (t + 1 > board.getRowSize() || u + 1 > board.getColumnSize()) {
                            t = 0;
                            u = 0;
                        }
                    }
                    if (actor == move) {
                        int r, c;
                        try {
                            r = Integer.parseInt(rowField.getText());
                            c = Integer.parseInt(colField.getText());
                        } catch(NumberFormatException e) {
                            return;
                        }
                        if (!((r >= board.getRowSize() || r < 0) || (c >= board.getColumnSize() || c < 0))) {
                            bm.get(tester2).boards.move(tester2, new BoardPosition(r, c));
                        }
                    }
                }
            }
        };
        act.addListener(change);
        move.addListener(change);

        table.add(act).colspan(2).height(80).width(150);
        table.add(move).height(80).width(100);
        table.add(rowField).height(30).width(100);
        table.add(colField).height(30).width(100);

        //table.center();
        table.setPosition(stage.getWidth() / 2.5f, stage.getHeight() / 2);
        table.debug();

        //set up stats ui
        NameLabel = new Label("---", skin);
        NameLabel.setColor(Color.YELLOW);
        HpLabel = new Label("-", skin);
        HpLabel.setColor(Color.GREEN);
        SpLabel = new Label("-", skin);
        SpLabel.setColor(Color.ORANGE);
        AtkLabel = new Label("-", skin);
        DefLabel = new Label("-", skin);
        SpdLabel = new Label("-", skin);
        statsTable.add(NameLabel).size(125, 50).row();
        NameLabel.setAlignment(Align.center);
        statsTable.add(HpLabel).center().size(125, 50).row();
        HpLabel.setAlignment(Align.center);
        statsTable.add(SpLabel).center().size(125, 50).row();
        SpLabel.setAlignment(Align.center);
        statsTable.add(AtkLabel).center().size(125, 50).row();
        AtkLabel.setAlignment(Align.center);
        statsTable.add(DefLabel).center().size(125, 50).row();
        DefLabel.setAlignment(Align.center);
        statsTable.add(SpdLabel).center().size(125, 50).row();
        SpdLabel.setAlignment(Align.center);
        statsTable.debug();
        statsTable.setPosition(stage.getWidth() * .85f, stage.getHeight() * .6666f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //sync code board and ui board ---
        int rowSize = board.getRowSize();
        int colSize = board.getColumnSize();
        int curRow = 0;
        int cur = 0;
        Array<Cell> cells = table.getCells();

        for (int i = 0; i < board.getRowSize() * board.getColumnSize(); i++) {
            cells.get(i).setActor(board.getTile(curRow, cur % rowSize));
            cur += 1;
            curRow = cur / colSize;
        }
        table.getCells();

        //update last ENTITY selected ---
        for (Entity e : BoardComponent.boards.getCodeBoard().getEntities()) {
            //if for actor component? if throwing ERRORS
            if (am.get(e).actor.getLastSelected()) {
                try {   //removes previously highlighted
                    if (am.get(e).actor.getLastSelected())
                        if (selectedEntity != null)
                            for (Tile t : getMovableSquares(selectedEntity))
                                if (t != null) {
                                    t.revertTileColor();
                                    t.stopListening();
                                }
                } catch (Exception exc) { }

                selectedEntity = e;
                am.get(selectedEntity).actor.shade(Color.ORANGE);

                try { // newly highlights spaces
                    for (Tile t : getMovableSquares(selectedEntity))
                        if (t != null) {
                            t.shadeTile(Color.BLUE);
                            t.startListening();
                        }
                } catch (Exception exc) { }

                checkedStats = false;
                am.get(e).actor.setLastSelected(false);
            }

            if (selectedEntity != e && am.get(e).actor.getColor() != Color.WHITE)
                am.get(e).actor.shade(Color.WHITE);
        }

        //update last TILE selected ---
        if (selectedEntity != null) {
            for (Tile t : BoardComponent.boards.getBoard().getTiles()) {
                if (t.getLastSelected()) {
                    t.setLastSelected(false);
                    try {   //removes previously highlighted
                        for (Tile tl : getMovableSquares(selectedEntity))
                            if (tl != null) {
                                tl.revertTileColor();
                                tl.stopListening();
                            }
                    } catch (Exception exc) { }

                    bm.get(selectedEntity).boards.move(selectedEntity, new BoardPosition(t.getRow(), t.getColumn()));
                }
            }
        }

        if (selectedEntity != null) {
        //show selected stats ---
            if (!checkedStats) {
                if (stm.has(selectedEntity)) {
                    StatComponent stat = stm.get(selectedEntity);
                    HpLabel.setText("Health : " + stat.hp + " / " + stat.maxHP);
                    SpLabel.setText("Skill : " + stat.sp + " / " + stat.maxSP);
                    AtkLabel.setText("Attack : " + stat.atk);
                    DefLabel.setText("Defense : " + stat.def);
                    SpdLabel.setText("Speed : " + stat.spd);
                } else {
                    HpLabel.setText("Health : -- / --");
                    SpLabel.setText("Skill : -- / --");
                    AtkLabel.setText("Attack : --");
                    DefLabel.setText("Defense : --");
                    SpdLabel.setText("Speed : --");
                }
                if (nm.has(selectedEntity))
                    NameLabel.setText(nm.get(selectedEntity).name);
                else
                    NameLabel.setText("???");

                checkedStats = true;
            }
        }

        stage.act(delta);
        stage.draw();
        engine.update(delta);
    }

    /**
     * Returns an array of all tiles that an entity can move on. Returns null if the entity doesn't have a {@code StatComponent}
     * or its speed is = 0.
     * @param e entity
     * @return null if it can't move or an {@code Array} of [@code Tile}s.
     */
    private Array<Tile> getMovableSquares(Entity e) {
        if (!stm.has(e) || stm.get(e).spd == 0)
            return null;

        int spd = stm.get(e).spd;
        int newR; int newC;
        int entityRow = bm.get(e).pos.r;
        int entityCol = bm.get(e).pos.c;
        Array<Tile> tiles = new Array<Tile>();

        for (int i = -spd; i <= spd; i++) {
            for (int j = -(spd - Math.abs(i)); j <= (spd - Math.abs(i)); j++) {
                newR = i + entityRow;
                newC = j + entityCol;
                if ((i == 0 && j == 0) || (newR < 0 || newC < 0 || newR >= codeBoard.getRows() || newC >= codeBoard.getColumns()))
                    continue;
                tiles.add(board.getTile(newR, newC));
            }
        }

        return tiles;
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
