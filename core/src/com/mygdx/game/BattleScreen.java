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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.components.*;
import com.mygdx.game.systems.DrawingSystem;
import com.mygdx.game.systems.EventSystem;
import com.mygdx.game.systems.MovementSystem;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;

/**
 * @author pnore_000
 */
public class BattleScreen implements Screen {


    private Stage stage;

    //Board
    private final Board board = new Board(7, 7, new Color(221f/255, 221f/255f, 119f/255f, 1), new Color(1, 1, 102f/255f, 1));
    //private final Board board = new Board(7, 7, Color.BLUE, Color.SKY);
    private final CodeBoard codeBoard = new CodeBoard(7, 7);

    //Selection and Hover
    private Entity selectedEntity;
    private boolean checkedStats;
    /**
     * value represents which move to show. If -1, means its showing no moves.
     */
    private int moveHover = -1;
    private boolean showedMoveRange;
    private boolean clearMoveRange;

    //Ui Elements
    private Skin skin;
    private TextureAtlas uiatlas;
    private Table table;
    private Table statsTable;
        private Label hpLabel;
        private Label spLabel;
        private Label atkLabel;
        private Label defLabel;
        private Label spdLabel;
        private Label nameLabel;
    private Table attackTable;
        private Label attackTitleLabel;
        private TextButton attackBtn1;
        private TextButton attackBtn2;
        private TextButton attackBtn3;
        private TextButton attackBtn4;
        //maybe entites should rotate sperate from this menu

    //Entities
    private Engine engine;
    private Entity tester;
    private Entity tester2;
    private Entity tester3;
    /*
    private Entity effect;
    private Entity effect2;
    */

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
        attackTable = new Table();
        stage.addActor(table);
        stage.addActor(statsTable);
        stage.addActor(attackTable);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        uiatlas = new TextureAtlas("uiskin.atlas");
        skin.addRegions(uiatlas);
        Gdx.input.setInputProcessor(stage);

        //Set up Engine
        engine = new Engine();
        engine.addSystem(new DrawingSystem(stage.getBatch()));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new EventSystem());

        //set up Entity
        TESTER = new SpriteActor(atlas.createSprite("BluePiece"), 20, 20);

        BoardComponent.setBoardManager(new BoardManager(board, codeBoard));
        tester = new Entity();
        tester.add(new ActorComponent(new SpriteActor(atlas.createSprite("RedPiece"), true, true)));
        tester.add(new BoardComponent());
        tester.add(new StatComponent(10, 999, 7, 0, 1));
        tester.add(new MovesetComponent(new Array<Move>(new Move[]{
                new Move("Chess Capture", tester2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0,1)}), engine, stage, BoardComponent.boards, new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk - stm.get(enemy).def, 0, 999);
                    }

                    @Override
                    public void doVisuals(Engine engine, Stage stage, BoardManager boardManager) {

                    }
        })})));

        tester2 = new Entity();
        tester2.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("Bot1"),
                atlas.findRegion("Bot2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        tester2.add(new BoardComponent());
        tester2.add(new StatComponent(5, 7, 2, 1, 3));
        tester2.add(new MovesetComponent(new Array<Move>(new Move[]{
                new Move("Tackle", tester2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0,1)}), engine, stage, BoardComponent.boards, new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk - stm.get(enemy).def, 0, 999);
                    }

                    @Override
                    public void doVisuals(Engine engine, Stage stage, BoardManager boardManager) {

                    }
                }),
                new Move("Full-Out Assault", tester2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1,1), new BoardPosition(0,1), new BoardPosition(1,1)}), engine, stage, BoardComponent.boards, new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk + 2 - stm.get(enemy).def, 0, 999);
                        stm.get(e).sp -= 1;
                    }

                    @Override
                    public void doVisuals(Engine engine, Stage stage, BoardManager boardManager) {

                    }
                }),
                new Move("Very Long-Named Raid Attack", tester2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1,2), new BoardPosition(0,1), new BoardPosition(1,2), new BoardPosition(0,2)}), engine, stage, BoardComponent.boards, new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk + 1 - stm.get(enemy).def, 0, 999);
                        stm.get(e).sp -= 1;
                    }

                    @Override
                    public void doVisuals(Engine engine, Stage stage, BoardManager boardManager) {

                    }
                }),
                new Move("Laser Beam Barrage", tester2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2,1), new BoardPosition(1,1), new BoardPosition(2,0),
                        new BoardPosition(-1,-1), new BoardPosition(0,-2), new BoardPosition(2,2), new BoardPosition(-1,1)}), engine, stage, BoardComponent.boards, new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).hp -= 1;
                        stm.get(e).sp -= 1;
                    }

                    @Override
                    public void doVisuals(Engine engine, Stage stage, BoardManager boardManager) {

                    }
                })
        })));
        tester2.add(new NameComponent("Robo - Beta"));

        tester3 = new Entity();
        tester3.add(new ActorComponent(new AnimationActor(new TextureRegion[]{atlas.findRegion("Hole"),
                atlas.findRegion("Hole2"), atlas.findRegion("Hole3"), atlas.findRegion("Hole4")},
                Animation.PlayMode.LOOP_PINGPONG, 0.1f)));
        tester3.add(new BoardComponent());
        tester3.add(new NameComponent("Hole"));

        /*
        effect = new Entity();
        effect.add(new PositionComponent(new Vector2(stage.getWidth() - 50, stage.getHeight() / 4), 100, 100, 0));
        effect.add(new SpriteComponent(atlas.findRegion("Star")));
        effect2 = new Entity();
        effect2.add(new PositionComponent(new Vector2(stage.getWidth() / 6, stage.getHeight() / 1.3f)
                , 100, 100, 0));
        effect2.add(new AnimationComponent(.3f, new TextureRegion[]{atlas.findRegion("Star1"),
                atlas.findRegion("Star2")}, Animation.PlayMode.LOOP));
        effect2.add(new MovementComponent(new Vector2(2, -1)));
        effect2.add(new EventComponent(.5f, 0, true, true, new GameEvent() {
            @Override
            public void event(Entity e, Engine engine) {
                mm.get(e).movement.add(MathUtils.sin(Math.abs(mm.get(e).movement.y)), -1);
            }
        }));
        */
        //put Entity on Board!
        BoardManager manage = bm.get(tester).boards;
        manage.add(tester, new BoardPosition(0, 0));
        manage.add(tester2, new BoardPosition(3, 3));
        manage.add(tester3, new BoardPosition(4, 0));
        /*
        engine.addEntity(effect);
        engine.addEntity(effect2);*/


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
        //table.debug();

        //set up stats ui
        nameLabel = new Label("---", skin);
        nameLabel.setColor(Color.YELLOW);
        hpLabel = new Label("-", skin);
        hpLabel.setColor(Color.GREEN);
        spLabel = new Label("-", skin);
        spLabel.setColor(Color.ORANGE);
        atkLabel = new Label("-", skin);
        defLabel = new Label("-", skin);
        spdLabel = new Label("-", skin);
        statsTable.add(nameLabel).size(125, 50).row();
        nameLabel.setAlignment(Align.center);
        statsTable.add(hpLabel).center().size(125, 50).row();
        hpLabel.setAlignment(Align.center);
        statsTable.add(spLabel).center().size(125, 50).row();
        spLabel.setAlignment(Align.center);
        statsTable.add(atkLabel).center().size(125, 50).row();
        atkLabel.setAlignment(Align.center);
        statsTable.add(defLabel).center().size(125, 50).row();
        defLabel.setAlignment(Align.center);
        statsTable.add(spdLabel).center().size(125, 50).row();
        spdLabel.setAlignment(Align.center);
        statsTable.debug();
        statsTable.setPosition(stage.getWidth() * .875f, stage.getHeight() * .8f);

        //set up attack menu ui
        attackTitleLabel = new Label("Actions", skin);
        attackTitleLabel.setColor(Color.YELLOW);
        attackBtn1 = new TextButton("---", skin, "toggle");
        attackBtn2 = new TextButton("---", skin, "toggle");
        attackBtn3 = new TextButton("---", skin, "toggle");
        attackBtn4 = new TextButton("---", skin, "toggle");
        ChangeListener attackSelector = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == attackBtn1) {
                        mvm.get(selectedEntity).moveList.get(0).useAttack();
                    } else if (actor == attackBtn2) {

                    } else if (actor == attackBtn3) {

                    } else if (actor == attackBtn4) {

                    }
                }
            }
        };
        ClickListener attackShower = new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1)
                    return;
                //System.out.println("ENTER METHOD start");

                if (fromActor == attackBtn1) {
                    moveHover = 0;
                    attackBtn1.setColor(Color.ORANGE);
                } else if (fromActor == attackBtn2) {
                    moveHover = 1;
                    attackBtn2.setColor(Color.ORANGE);
                } else if (fromActor == attackBtn3) {
                    moveHover = 2;
                    attackBtn3.setColor(Color.ORANGE);
                } else if (fromActor == attackBtn4) {
                    moveHover = 3;
                    attackBtn1.setColor(Color.ORANGE);
                } else if (fromActor == null) {
                    //moveHover = 0;
                    return;  //temporary "fix"
                }

               // System.out.println(fromActor);
                showedMoveRange = false;
                clearMoveRange = false;
                //System.out.println("(" + moveHover + ") ENTERED");
               // System.out.println("ENTER METHOD end \n");
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                //System.out.println("EXIT METHOD start");
                if (pointer != -1)
                    return;
                //revert button shading
                if (moveHover == 0)
                    attackBtn1.setColor(Color.WHITE);
                else if (moveHover == 1)
                    attackBtn2.setColor(Color.WHITE);
                else if (moveHover == 2)
                    attackBtn3.setColor(Color.WHITE);
                else if (moveHover == 3)
                    attackBtn1.setColor(Color.WHITE);

               // System.out.println(toActor);
                clearMoveRange = true;
                //System.out.println("(" + moveHover + ") EXITED");
               // System.out.println("EXIT METHOD end \n");
            }
        };
        attackBtn1.addListener(attackSelector);
        attackBtn1.setName("Attack1");
        attackBtn2.addListener(attackSelector);
        attackBtn2.setName("Attack2");
        attackBtn3.addListener(attackSelector);
        attackBtn3.setName("Attack3");
        attackBtn4.addListener(attackSelector);
        attackBtn4.setName("Attack4");
        attackBtn1.addListener(attackShower);
        attackBtn2.addListener(attackShower);
        attackBtn3.addListener(attackShower);
        attackBtn4.addListener(attackShower);
        attackTable.add(attackTitleLabel).center().size(175, 50).row();
        attackTable.add(attackBtn1).size(175, 50).padBottom(15f).row();
        attackTable.add(attackBtn2).size(175, 50).padBottom(15f).row();
        attackTable.add(attackBtn3).size(175, 50).padBottom(15f).row();
        attackTable.add(attackBtn4).size(175, 50).padBottom(15f).row();
        attackTable.debug();
        attackTable.setPosition(stage.getWidth() * .875f, stage.getHeight() * .3f);
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
                        if (t != null && !t.getIsListening()) {
                            t.shadeTile(Color.CYAN);
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

        //show Attack Squares visual ---
        if (selectedEntity != null && mvm.has(selectedEntity)) {
            if (!showedMoveRange && moveHover > -1) {
                //highlight attack squares
                if (mvm.get(selectedEntity).moveList.size > moveHover) {
                    for (BoardPosition pos : mvm.get(selectedEntity).moveList.get(moveHover).getRange()) {
                        try {
                            bm.get(selectedEntity).boards.getBoard().getTile(pos.r + bm.get(selectedEntity).pos.r, pos.c + bm.get(selectedEntity).pos.c).shadeTile(Color.RED);
                        } catch (Exception e) { }
                    }
                    showedMoveRange = true;
                }
            }
            //remove attack squares
            if (clearMoveRange && moveHover != -1) {
                if (mvm.get(selectedEntity).moveList.size > moveHover) {
                    boolean wasBlue = false;
                    for (BoardPosition pos : mvm.get(selectedEntity).moveList.get(moveHover).getRange()) {
                        try {
                        Tile currTile = board.getTile(pos.r + bm.get(selectedEntity).pos.r, pos.c + bm.get(selectedEntity).pos.c);
                        if (currTile.getIsListening()) {
                            for (Tile t : getMovableSquares(selectedEntity)) {
                                if (t == currTile)
                                    wasBlue = true;
                            }
                            if (wasBlue)
                                currTile.shadeTile(Color.CYAN);
                            else
                                currTile.revertTileColor();
                        }
                        else
                            currTile.revertTileColor();
                        } catch (Exception e) { }
                    }

                    clearMoveRange = false;
                    moveHover = -1;
                }
            }
        }


        if (selectedEntity != null) {
        //show selected stats ---
            if (!checkedStats) {
                if (stm.has(selectedEntity)) {
                    StatComponent stat = stm.get(selectedEntity);
                    hpLabel.setText("Health : " + stat.hp + " / " + stat.maxHP);
                    spLabel.setText("Skill : " + stat.sp + " / " + stat.maxSP);
                    atkLabel.setText("Attack : " + stat.atk);
                    defLabel.setText("Defense : " + stat.def);
                    spdLabel.setText("Speed : " + stat.spd);
                } else {
                    hpLabel.setText("Health : -- / --");
                    spLabel.setText("Skill : -- / --");
                    atkLabel.setText("Attack : --");
                    defLabel.setText("Defense : --");
                    spdLabel.setText("Speed : --");
                }
                if (mvm.has(selectedEntity)) {
                    MovesetComponent moves = mvm.get(selectedEntity);
                    if ( moves.moveList.size > 0 && moves.moveList.get(0) != null)
                        attackBtn1.setText(moves.moveList.get(0).getName());
                    else
                        attackBtn1.setText("---");
                    if (moves.moveList.size > 1 && moves.moveList.get(1) != null)
                        attackBtn2.setText(moves.moveList.get(1).getName());
                    else
                        attackBtn2.setText("---");
                    if (moves.moveList.size > 2 && moves.moveList.get(2) != null)
                        attackBtn3.setText(moves.moveList.get(2).getName());
                    else
                        attackBtn3.setText("---");
                    if (moves.moveList.size > 3 && moves.moveList.get(3) != null)
                        attackBtn4.setText(moves.moveList.get(3).getName());
                    else
                        attackBtn4.setText("---");
                } else {
                    attackBtn1.setText("---");
                    attackBtn2.setText("---");
                    attackBtn3.setText("---");
                    attackBtn4.setText("---");
                }
                if (nm.has(selectedEntity))
                    nameLabel.setText(nm.get(selectedEntity).name);
                else
                    nameLabel.setText("???");

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
