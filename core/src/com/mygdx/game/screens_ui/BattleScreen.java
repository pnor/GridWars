package com.mygdx.game.screens_ui;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.boards.Board;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.boards.CodeBoard;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.components.MovesetComponent;
import com.mygdx.game.components.StatComponent;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.move_related.Visuals;
import com.mygdx.game.systems.*;

import static com.mygdx.game.ComponentMappers.*;

/**
 * @author pnore_000
 */
public class BattleScreen implements Screen {

    private GridWars gridWars;
    private Engine engine;
    private Stage stage;
    private BattleInputProcessor battleInputProcessor;

    //Background
    Background background;

    //Board
    //private final Board board = new Board(7, 7, new Color(221f / 255, 221f / 255f, 119f / 255f, 1), new Color(1, 1, 102f / 255f, 1));
        /*7 is the basic size. Requires no scaling with anything below 7 size
        The math for scaling above 7 : 700 / size
        */
    private final Board board;
    private final CodeBoard codeBoard;

    //Entities
    private Array<Array<Entity>> teams = new Array<Array<Entity>>();

    //Selection and Hover
    private Entity selectedEntity;
    private boolean checkedStats;

    //Attack Processing
    private Move currentMove;
    /**
     * value represents which move to show. If -1, means its showing no moves.
     */
    private int moveHover = -1;
    private boolean hoverChanged;
    public boolean attacksEnabled = true;

    //Ui Elements
    private Skin skin;
    private TextureAtlas uiatlas;
    private TextureAtlas backAtlas;
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
    private HoverButton attackBtn1;
    private HoverButton attackBtn2;
    private HoverButton attackBtn3;
    private HoverButton attackBtn4;
    private Table infoTable;
    private Label infoLbl;

    //debug values
    private float deltaTimeSums;
    private final int deltatimeIntervals = 10;
    private int currentDeltaTime;


    public BattleScreen(GridWars game, int boardSize, Color darkBoardColor, Color lightBoardColor) {
        gridWars = game;
        if (boardSize <= 7) {
            board = new Board(boardSize, boardSize, darkBoardColor, lightBoardColor, 100);
            codeBoard = new CodeBoard(boardSize, boardSize);
        } else {
            board = new Board(boardSize, boardSize, darkBoardColor, lightBoardColor, 700 / boardSize);
            codeBoard = new CodeBoard(boardSize, boardSize);
        }
    }

    @Override
    public void show() {
        //set up assets
        stage = new Stage();
        stage.getViewport().setWorldSize(1000, 900);
        stage.getViewport().setScreenSize(1000, 900);
        table = new Table();
        statsTable = new Table();
        attackTable = new Table();
        infoTable = new Table();
        stage.addActor(table);
        stage.addActor(statsTable);
        stage.addActor(attackTable);
        stage.addActor(infoTable);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        backAtlas = new TextureAtlas(Gdx.files.internal("BackPack.pack"));
        uiatlas = new TextureAtlas("uiskin.atlas");
        skin.addRegions(uiatlas);
        battleInputProcessor = new BattleInputProcessor(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, battleInputProcessor));

        //Set up Engine
        engine = new Engine();
        engine.addSystem(new DrawingSystem(stage.getBatch()));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new DamageDeathSystem(BoardComponent.boards));

        //set up Background
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.DARK_GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        topLayer.setColor(Color.GRAY);
        background = new Background(backgroundLay,
                new Sprite[]{new Sprite(backAtlas.findRegion("DiagStripeOverlay")), topLayer},
                new BackType[]{BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL},
                Color.CYAN, Color.RED);

        //set up Entity

        //add to Engine
        for (Array<Entity> t : teams)
            for (Entity e : t)
                engine.addEntity(e);

        BoardComponent.setBoardManager(new BoardManager(board, codeBoard));
        Visuals.boardManager = BoardComponent.boards;
        Visuals.engine = engine;
        Visuals.stage = stage;


        //set up Board ui -----
        for (int i = 0; i < board.getRowSize(); i++) {
            for (int j = 0; j < board.getColumnSize(); j++) {
                if (i == 0)
                    table.add().width(board.getTiles().get(0).getWidth()).height(board.getTiles().get(0).getHeight());
                else
                    table.add().height(board.getTiles().get(0).getHeight());
            }
            table.row();
        }
        table.setPosition(stage.getWidth() / 2.5f, stage.getHeight() / 2);
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("TableBackground.png")), 33, 33, 33, 33);
        NinePatchDrawable tableBackground = new NinePatchDrawable(tableBack);

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
        //statsTable.debug();
        statsTable.setBackground(tableBackground);
        statsTable.pack();
        statsTable.setPosition(stage.getWidth() * .875f - (statsTable.getWidth() / 2), stage.getHeight() * .75f - (statsTable.getHeight() / 2));

        //set up attack menu ui
        attackTitleLabel = new Label("Actions", skin);
        attackTitleLabel.setColor(Color.YELLOW);
        attackBtn1 = new HoverButton("---", skin);
        attackBtn2 = new HoverButton("---", skin);
        attackBtn3 = new HoverButton("---", skin);
        attackBtn4 = new HoverButton("---", skin);
        ChangeListener attackSelector = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (selectedEntity != null && mvm.has(selectedEntity)) {
                        if (actor == attackBtn1) {
                            mvm.get(selectedEntity).moveList.get(0).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(0);
                        } else if (actor == attackBtn2) {
                            mvm.get(selectedEntity).moveList.get(1).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(1);
                        } else if (actor == attackBtn3) {
                            mvm.get(selectedEntity).moveList.get(2).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(2);
                        } else if (actor == attackBtn4) {
                            mvm.get(selectedEntity).moveList.get(3).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(3);
                        }

                        state.get(selectedEntity).canAttack = false;
                        currentMove.getVisuals().setPlaying(true, false);
                        disableUI();
                        if (nm.has(selectedEntity))
                            infoLbl.setText(nm.get(selectedEntity).name + " used " + currentMove.getName() + "!");
                        else
                            infoLbl.setText(currentMove.getName() + " was used!");
                    }
                }
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

        attackTitleLabel.setAlignment(Align.center);
        attackTable.add(attackTitleLabel).center().size(175, 50).row();
        attackTable.add(attackBtn1).size(175, 50).padBottom(15f).row();
        attackTable.add(attackBtn2).size(175, 50).padBottom(15f).row();
        attackTable.add(attackBtn3).size(175, 50).padBottom(15f).row();
        attackTable.add(attackBtn4).size(175, 50).padBottom(15f).row();
        //attackTable.debug();
        attackTable.setBackground(tableBackground);
        attackTable.pack();
        attackTable.setPosition(stage.getWidth() * .875f - (attackTable.getWidth() / 2), stage.getHeight() * .25f - (attackTable.getWidth() / 2));

        //set up infoTable
        infoLbl = new GradualLabel(.001f, "---", skin);
        infoTable.add(infoLbl).height(25).center();
        infoTable.setBackground(tableBackground);
        infoTable.pack();
        infoTable.setPosition(table.getX() - 350, stage.getHeight() * .9f);
        infoTable.setSize(700, 80);

    }

    /**
     * !! WHEN I DO THIS, DESCRIBE THE RENDER LOOP PROCESS BY PROCESS !!
     * @param delta
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!(currentDeltaTime >= deltatimeIntervals)) {
            deltaTimeSums += delta;
            currentDeltaTime += 1;
        } else {
            currentDeltaTime = 0;
            System.out.println("Average delta time : " + deltaTimeSums / deltatimeIntervals + "  [interval:" + deltatimeIntervals + "]");
            deltaTimeSums = 0;
            System.out.println(Visuals.visualsArePlaying);
        }

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
                        if (selectedEntity != null && stm.has(selectedEntity) && stm.get(selectedEntity).spd > 0) {
                            for (Tile t : getMovableSquares(selectedEntity))
                                if (t != null) {
                                    t.revertTileColor();
                                    t.stopListening();
                                }
                        }
                } catch(IndexOutOfBoundsException exc){}
                //stop orange highlight
                if (Visuals.visualsArePlaying == 0 && selectedEntity != null)
                    shadeBasedOnState(selectedEntity);

                selectedEntity = e;
                if (Visuals.visualsArePlaying == 0)
                    am.get(selectedEntity).actor.shade(Color.ORANGE);

                if (stm.has(selectedEntity) && stm.get(selectedEntity).spd > 0 && state.has(selectedEntity) && state.get(selectedEntity).canMove) {
                    try { // newly highlights spaces
                        for (Tile t : getMovableSquares(selectedEntity))
                            if (t != null && !t.getIsListening()) {
                                t.shadeTile(Color.CYAN);
                                t.startListening();
                            }
                    } catch (IndexOutOfBoundsException exc) {
                    }
                }

                checkedStats = false;
                am.get(e).actor.setLastSelected(false);
            }
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
                    } catch (IndexOutOfBoundsException exc) { }

                    //move Entity location
                    bm.get(selectedEntity).boards.move(selectedEntity, new BoardPosition(t.getRow(), t.getColumn()));
                    state.get(selectedEntity).canMove = false;
                }
            }
        }

        //updating attack squares
        if (selectedEntity != null && state.has(selectedEntity) && !state.get(selectedEntity).canAttack && attacksEnabled)
            disableAttacks();
        else if (selectedEntity == null || (selectedEntity != null && state.has(selectedEntity) && state.get(selectedEntity).canAttack && !attacksEnabled))
            enableAttacks();

        if (!hoverChanged) {
            if (attackBtn1.getHover()) {
                moveHover = 0;
                showAttackTiles();
                hoverChanged = true;
            } else if (attackBtn2.getHover()) {
                moveHover = 1;
                showAttackTiles();
                hoverChanged = true;
            } else if (attackBtn3.getHover()) {
                moveHover = 2;
                showAttackTiles();
                hoverChanged = true;
            } else if (attackBtn4.getHover()) {
                moveHover = 3;
                showAttackTiles();
                hoverChanged = true;
            }
        }
        if (hoverChanged && !((attackBtn1.getHover() || attackBtn2.getHover() || attackBtn3.getHover() || attackBtn4.getHover()))) {
            removeAttackTiles();
            moveHover = -1;
            hoverChanged = false;
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
                    if (moves.moveList.size > 0 && moves.moveList.get(0) != null)
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

        //playing current move animation
        if (currentMove != null) {
            if (currentMove.getVisuals().getIsPlaying()) {
                currentMove.updateVisuals(delta);
                currentMove.getVisuals().play();
            } else {
                currentMove.getVisuals().reset();
                currentMove = null;
            }
        }

        background.update(delta);
        stage.act(delta);
        engine.getSystem(DrawingSystem.class).drawBackground(background, delta);
        stage.draw();
        engine.update(delta);

        //Handling dead entities
        for (Entity e : BoardComponent.boards.getCodeBoard().getEntities()) {
            if (stm.has(e) && !stm.get(e).alive) {
                BoardComponent.boards.remove(e);
                if (nm.has(e))
                    infoLbl.setText(nm.get(e).name + " has been defeated!");
            }
        }

        //debug
        if (Visuals.visualsArePlaying < 0)
            throw (new IndexOutOfBoundsException("Visuals.visualsArePlaying is < 0"));

    }

    public void showAttackTiles() {
        //show Attack Squares visual ---
        if (selectedEntity != null && mvm.has(selectedEntity)) {
            if (moveHover > -1) {
                //highlight attack squares
                if (mvm.get(selectedEntity).moveList.size > moveHover) {
                    for (BoardPosition pos :  mvm.get(selectedEntity).moveList.get(moveHover).getRange()) {
                        try {
                            bm.get(selectedEntity).boards.getBoard().getTile(pos.r + bm.get(selectedEntity).pos.r, pos.c + bm.get(selectedEntity).pos.c).shadeTile(Color.RED);
                        } catch (IndexOutOfBoundsException e) { }
                    }
                }
            }
        }
    }

    public void removeAttackTiles() {
        //remove attack squares
        if (moveHover != -1) {
            if (selectedEntity != null && mvm.has(selectedEntity) && mvm.get(selectedEntity).moveList.size > moveHover) {
                //boolean wasBlue = false;
                for (BoardPosition pos :  mvm.get(selectedEntity).moveList.get(moveHover).getRange()) {
                    try {
                        Tile currTile = board.getTile(pos.r + bm.get(selectedEntity).pos.r, pos.c + bm.get(selectedEntity).pos.c);
                        /*
                        if (currTile.getIsListening()) {
                            for (Tile t : getMovableSquares(selectedEntity)) {
                                if (t == currTile) {
                                    wasBlue = true;
                                    break;
                                }
                            }
                            if (wasBlue)
                                currTile.shadeTile(Color.CYAN);
                            else
                                currTile.revertTileColor();

                        } else
                            currTile.revertTileColor();
                            */
                        if (currTile.getIsListening())
                            currTile.shadeTile(Color.CYAN);
                        else
                            currTile.revertTileColor();
                    } catch (IndexOutOfBoundsException e) { }
                }
            }
        }
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

    /**
     * Shades an entity based on its current state
     * @param e entity being shaded
     */
    public void shadeBasedOnState(Entity e) {
        if (!state.has(e)) {
            am.get(e).actor.shade(Color.WHITE);
            return;
        }
        if (!state.get(e).canMove && !state.get(e).canAttack)
            am.get(e).actor.shade(Color.DARK_GRAY);
        else if (!state.get(e).canMove || !state.get(e).canAttack)
            am.get(e).actor.shade(Color.GRAY);
        else
            am.get(e).actor.shade(Color.WHITE);
    }

    /**
     * Checks if the entity is shaded the color that it would be if it was not selected
     * @param e Entity
     * @return true if the entity is correctly shaded. false otherwise. Also returns false if the entity has no state
     */
    public boolean checkShading(Entity e) {
        if (!state.has(e))
            return false;


        if (!(state.get(e).canMove || state.get(e).canAttack))
            return am.get(e).actor.getColor() == Color.GRAY;
        else if (!state.get(e).canMove && !state.get(e).canAttack)
            return am.get(e).actor.getColor() == Color.DARK_GRAY;
        else
            return am.get(e).actor.getColor() == Color.WHITE;
    }

    /**
     * Disables the user input of the battle screen.
     */
    public void disableUI() {
        battleInputProcessor.setDisabled(true);
        disableAttacks();
    }

    /**
     * Enables the user input of the battle screen.
     */
    public void enableUI() {
        battleInputProcessor.setDisabled(false);
        enableAttacks();
    }

    /**
     * Disables attack buttons
     */
    public void disableAttacks() {
        attackBtn1.setTouchable(Touchable.disabled);
        attackBtn1.setColor(Color.DARK_GRAY);
        attackBtn2.setTouchable(Touchable.disabled);
        attackBtn2.setColor(Color.DARK_GRAY);
        attackBtn3.setTouchable(Touchable.disabled);
        attackBtn3.setColor(Color.DARK_GRAY);
        attackBtn4.setTouchable(Touchable.disabled);
        attackBtn4.setColor(Color.DARK_GRAY);

        attacksEnabled = false;
    }

    public void enableAttacks() {
        attackBtn1.setTouchable(Touchable.enabled);
        attackBtn1.setColor(Color.WHITE);
        attackBtn2.setTouchable(Touchable.enabled);
        attackBtn2.setColor(Color.WHITE);
        attackBtn3.setTouchable(Touchable.enabled);
        attackBtn3.setColor(Color.WHITE);
        attackBtn4.setTouchable(Touchable.enabled);
        attackBtn4.setColor(Color.WHITE);
        attacksEnabled = true;
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

    public int getMoveHover() {
        return moveHover;
    }

    public void setTeams(Array<Entity>... team) {
        for (Array<Entity> t : team) {
            teams.add(t);
        }

        for (Array<Entity> t : teams)
            for (Entity e : t)
                engine.addEntity(e);
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public Engine getEngine() {
        return engine;
    }

    public Stage getStage() {
        return stage;
    }
}
