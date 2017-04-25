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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.actors.UIActor;
import com.mygdx.game.boards.Board;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.boards.CodeBoard;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.components.MovesetComponent;
import com.mygdx.game.components.StatComponent;
import com.mygdx.game.components.StatusEffectComponent;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.move_related.Visuals;
import com.mygdx.game.rules_types.Battle2PRules;
import com.mygdx.game.rules_types.Rules;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.systems.*;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;

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

    //rules
    private static Rules rules;
    private boolean gameHasEnded;

    //Entities
    private Array<Team> teams = new Array<Team>();

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
    /**
     * Table with board tiles
     */
    private Table boardTable;
    /**
     * Table that displays stat values and names
     */
    private Table statsTable;
    private Label nameLbl,
            hpLblID, hpLbl,
            spLblID, spLbl,
            atkLblID, atkLbl,
            defLblID, defLbl,
            spdLblID, spdLbl,
            statusLbl;

    /**
     * Table with attack buttons
     */
    private Table attackTable;
    private Label attackTitleLabel;
    private HoverButton attackBtn1;
    private HoverButton attackBtn2;
    private HoverButton attackBtn3;
    private HoverButton attackBtn4;
    /**
     * Table that shows the info label
     */
    private Table infoTable;
    private Label infoLbl;
    /**
     * Has data for the team. Has end turn button, and will have a icon of all the entities on a team
     */
    private Table teamTable;
    private Image member1;
    private Image member2;
    private Image member3;
    private Image member4;
    private HoverButton endTurnBtn;
    /**
     * The box that says the next team's turn has started.
     */
    private Table endTurnMessageTable;
    private Label endTurnMessageLbl;
    private Label turnCountLbl;


    //debug values
    private float deltaTimeSums;
    private final int deltatimeIntervals = 10000;
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
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        //set up assets
        stage = new Stage();
        stage.getViewport().setWorldSize(1000, 900);
        stage.getViewport().setScreenSize(1000, 900);
        boardTable = new Table();
        statsTable = new Table();
        attackTable = new Table();
        infoTable = new Table();
        teamTable = new Table();
        endTurnMessageTable = new Table();
        stage.addActor(boardTable);
        stage.addActor(statsTable);
        stage.addActor(attackTable);
        stage.addActor(infoTable);
        stage.addActor(teamTable);
        stage.addActor(endTurnMessageTable);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        backAtlas = new TextureAtlas(Gdx.files.internal("BackPack.pack"));
        uiatlas = new TextureAtlas("uiskin.atlas");
        skin.addRegions(uiatlas);
        battleInputProcessor = new BattleInputProcessor(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, battleInputProcessor));
        rules = new Battle2PRules(this, teams);

        //Set up Engine
        engine = new Engine();
        engine.addSystem(new DrawingSystem(stage.getBatch()));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new DamageDeathSystem(BoardComponent.boards));

        //set up Background
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(new Color(121f / 255, 121f / 255f, 19f / 255f, 1));
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        topLayer.setColor(new Color(181f / 255, 181f / 255f, 79f / 255f, 1));
        background = new Background(backgroundLay,
                new Sprite[]{new Sprite(backAtlas.findRegion("DiagStripeOverlay")), topLayer},
                new BackType[]{BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL},
                Color.DARK_GRAY, Color.WHITE);

        //add to Engine
        for (Team t : teams)
            for (Entity e : t.getEntities())
                engine.addEntity(e);

        BoardComponent.setBoardManager(new BoardManager(board, codeBoard));
        Visuals.boardManager = BoardComponent.boards;
        Visuals.engine = engine;
        Visuals.stage = stage;

        //set up Board ui -----
        for (int i = 0; i < board.getRowSize(); i++) {
            for (int j = 0; j < board.getColumnSize(); j++) {
                if (i == 0)
                    boardTable.add().width(board.getTiles().get(0).getWidth()).height(board.getTiles().get(0).getHeight());
                else
                    boardTable.add().height(board.getTiles().get(0).getHeight());
            }
            boardTable.row();
        }
        boardTable.setPosition(stage.getWidth() / 2.5f, stage.getHeight() / 2);
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("TableBackground.png")), 33, 33, 33, 33);
        NinePatchDrawable tableBackground = new NinePatchDrawable(tableBack);

        //set up stats ui
        param.size = 20; //name
        nameLbl = new Label("---", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        nameLbl.setColor(Color.YELLOW);
        param.size = 16; //hp
        param.borderColor = Color.GREEN;
        hpLblID = new Label("Health", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        hpLbl = new Label("-", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        hpLblID.setColor(Color.GREEN);
        param.borderColor = Color.ORANGE; //skill
        spLblID = new Label("Skill", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        spLbl = new Label("-", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        spLblID.setColor(Color.ORANGE);
        atkLblID = new Label("Attack", skin); //attack
        atkLbl = new Label("-", skin);
        atkLblID.setColor(Color.RED);
        defLblID = new Label("Defense", skin); //defense
        defLbl = new Label("-", skin);
        defLblID.setColor(Color.BLUE);
        spdLblID = new Label("Speed", skin); //speed
        spdLbl = new Label("-", skin);
        spdLblID.setColor(Color.PINK);
        statsTable.add().size(70, 0); statsTable.add().size(70, 0).row();
        statsTable.add(nameLbl).colspan(2).padBottom(10f).row(); //set up table
        nameLbl.setAlignment(Align.center);
        statsTable.add(hpLblID).height(40);
        statsTable.add(hpLbl).row();
        statsTable.add(spLblID).height(40);
        statsTable.add(spLbl).row();
        statsTable.add(atkLblID).height(40);
        statsTable.add(atkLbl).row();
        statsTable.add(defLblID).height(40);
        statsTable.add(defLbl).row();
        statsTable.add(spdLblID).height(40);
        statsTable.add(spdLbl).row();
            //statsTable.debug();
        statsTable.setBackground(tableBackground);
        statsTable.pack();
        statsTable.setPosition(stage.getWidth() * .875f - (statsTable.getWidth() / 2), stage.getHeight() * .75f - (statsTable.getHeight() / 2));

        //set up attack menu ui
        param.size = 20;
        param.borderWidth = 0;
        attackTitleLabel = new Label("Actions", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        attackTitleLabel.setColor(Color.YELLOW);
        attackBtn1 = new HoverButton("---", skin);
        attackBtn2 = new HoverButton("---", skin);
        attackBtn3 = new HoverButton("---", skin);
        attackBtn4 = new HoverButton("---", skin);
        ChangeListener attackSelector = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (mayAttack(selectedEntity)) {
                        if (actor == attackBtn1) {
                            if (mvm.get(selectedEntity).moveList.get(0).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(0).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(0);
                        } else if (actor == attackBtn2) {
                            if (mvm.get(selectedEntity).moveList.get(1).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(1).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(1);
                        } else if (actor == attackBtn3) {
                            if (mvm.get(selectedEntity).moveList.get(2).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(2).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(2);
                        } else if (actor == attackBtn4) {
                            if (mvm.get(selectedEntity).moveList.get(3).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(3).useAttack();
                            currentMove = mvm.get(selectedEntity).moveList.get(3);
                        }

                        state.get(selectedEntity).canAttack = false;
                        currentMove.getVisuals().setPlaying(true, false);
                        disableUI();
                        if (nm.has(selectedEntity))
                            if (currentMove.getAttackMessage() != null)
                                infoLbl.setText(currentMove.getAttackMessage());
                            else
                                infoLbl.setText(nm.get(selectedEntity).name + " used " + currentMove.getName() + "!");
                        else
                            if (currentMove.getAttackMessage() != null)
                                infoLbl.setText(currentMove.getAttackMessage());
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
        attackTable.add(attackTitleLabel).center().size(140, 50).row();
        attackTable.add(attackBtn1).size(140, 50).padBottom(15f).row();
        attackTable.add(attackBtn2).size(140, 50).padBottom(15f).row();
        attackTable.add(attackBtn3).size(140, 50).padBottom(15f).row();
        attackTable.add(attackBtn4).size(140, 50).padBottom(15f).row();
        attackTable.setBackground(tableBackground);
        attackTable.pack();
        attackTable.setPosition(stage.getWidth() * .875f - (attackTable.getWidth() / 2), stage.getHeight() * .25f - (attackTable.getWidth() / 2));

        //set up infoTable
        infoLbl = new GradualLabel(.001f, "---", skin);
        infoTable.add(infoLbl).height(25).center();
        infoTable.setBackground(tableBackground);
        infoTable.pack();
        infoTable.setPosition(boardTable.getX() - 350, stage.getHeight() * .9f);
        infoTable.setSize(700, 80);

        //set up team table
        endTurnBtn = new HoverButton("End Turn", skin, new Color(200f / 255f, 200f / 255f, 255f / 255f, 1), new Color(.8f, 1f, 1f, 1));
        endTurnBtn.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
           if (((Button) actor).isPressed()) {
               rules.nextTurn();
               endTurnMessageLbl.setText("" + rules.getCurrentTeam().getTeamName() + " turn!");
               turnCountLbl.setText("Turn " + rules.getTurnCount());
               turnCountLbl.setColor(new Color(1,1,1,1).lerp(Color.ORANGE, (float) rules.getTurnCount() / 100f));
               endTurnMessageTable.setColor(rules.getCurrentTeam().getTeamColor());
               endTurnMessageTable.clearActions();
               SequenceAction sequence = new SequenceAction();
               sequence.addAction(Actions.fadeIn(.2f));
               sequence.addAction(Actions.delay(1f));
               sequence.addAction(Actions.fadeOut(.2f));
               endTurnMessageTable.addAction(sequence);
           }
           }
        });
        member1 = new Image(atlas.findRegion("Hole2"));
        member2 = new Image(atlas.findRegion("Hole2"));
        member3 = new Image(atlas.findRegion("Hole2"));
        member4 = new Image(atlas.findRegion("Hole2"));
        teamTable.add(endTurnBtn).height(40).width(120);
        teamTable.add(member1).size(48).padLeft(60f);
        teamTable.add(member2).size(48).padLeft(60f);
        teamTable.add(member3).size(48).padLeft(60f);
        teamTable.add(member4).size(48).padLeft(60f);
        teamTable.setBackground(tableBackground);
        teamTable.pack();
        teamTable.setSize(600, 90);
        teamTable.setPosition(teamTable.getX() + teamTable.getOriginX() + 40, stage.getHeight() * .01f);
        //teamTable.debug();

        //set up endTurnMessageTable
        param.size = 25;
        endTurnMessageLbl = new Label("Team <Unset> Turn", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        turnCountLbl = new Label("Turn 1", skin);
        endTurnMessageTable.setBackground(tableBackground);
        endTurnMessageTable.pack();
        endTurnMessageTable.setSize(380, 120);
        endTurnMessageTable.setPosition(stage.getWidth() / 2 - endTurnMessageTable.getWidth() / 2, (stage.getHeight() / 2 - endTurnMessageTable.getHeight() / 2));
        endTurnMessageTable.add(endTurnMessageLbl).padTop(20f).row();
        endTurnMessageTable.add(turnCountLbl);
        endTurnMessageTable.setColor(Color.WHITE);
        endTurnMessageTable.addAction(Actions.fadeOut(0f));

        fontGenerator.dispose();
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
        Array<Cell> cells = boardTable.getCells();

        for (int i = 0; i < board.getRowSize() * board.getColumnSize(); i++) {
            cells.get(i).setActor(board.getTile(curRow, cur % rowSize));
            cur += 1;
            curRow = cur / colSize;
        }
        boardTable.getCells();

        if (!gameHasEnded) {
            //update last ENTITY selected ---
            for (Entity e : BoardComponent.boards.getCodeBoard().getEntities()) {
                //if for actor component? if throwing ERRORS
                if (am.get(e).actor.getLastSelected()) {
                    try {   //removes previously highlighted
                        if (selectedEntity != null && stm.has(selectedEntity) && stm.get(selectedEntity).getModSpd(selectedEntity) > 0)
                            removeMovementTiles();
                    } catch (IndexOutOfBoundsException exc) {
                    }

                    if (Visuals.visualsArePlaying == 0 && selectedEntity != null) //stop orange highlight

                        shadeBasedOnState(selectedEntity);

                    selectedEntity = e; //selectedEntity changes to new entity here on

                    if (Visuals.visualsArePlaying == 0)
                        am.get(selectedEntity).actor.shade(Color.ORANGE);
                    if (mayAttack(selectedEntity) && !attacksEnabled)
                        enableAttacks();

                    //check if has a speed > 0, and can move. Also if it is not on another team/has no team
                    if (mayMove(selectedEntity)) {
                        try { // newly highlights spaces
                            showMovementTiles();
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
                            removeMovementTiles();
                        } catch (IndexOutOfBoundsException exc) {
                        }

                        //move Entity location
                        bm.get(selectedEntity).boards.move(selectedEntity, new BoardPosition(t.getRow(), t.getColumn()));
                        state.get(selectedEntity).canMove = false;
                    }
                }
            }

            //updating attack squares
            if (!mayAttack(selectedEntity) && attacksEnabled)
                disableAttacks();
            else if (mayAttack(selectedEntity) && !attacksEnabled)
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
                    updateStatsAndMoves();
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
        }

        //update everything. (Graphics, engine, stage)
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

        //check win conditions
        if (rules.checkWinConditions() != null && currentMove == null) {
            if (!gameHasEnded) {
                infoLbl.setText("" + rules.checkWinConditions().getTeamName() + " has won!");
                endTurnBtn.setDisabled(true);
            }
            gameHasEnded = true;
        }

        //debug
        if (Visuals.visualsArePlaying < 0)
            throw (new IndexOutOfBoundsException("Visuals.visualsArePlaying is < 0"));
        for (Team t : teams)
            for (Entity e : t.getEntities()) {
            if (status.has(e))
                if (status.get(e).getTotalStatusEffects() < 0) {
                    if (nm.has(e) && team.has(e))
                        throw (new IndexOutOfBoundsException("An Entity, " + nm.get(e).name + ", on Team " + team.get(e).teamNumber + " has less than 0 status effects!"));
                    else if (nm.has(e))
                        throw (new IndexOutOfBoundsException("An Entity, " + nm.get(e).name + ", has less than 0 status effects!"));
                    else
                        throw (new IndexOutOfBoundsException("An unnamed Entity has less than 0 status effects!"));
                }
            }

    }

    public void showMovementTiles() {
        for (Tile t : getMovableSquares(selectedEntity)) {
            if (t.isOccupied())
                continue;
            if (t != null && !t.getIsListening()) {
                t.shadeTile(Color.CYAN);
                t.startListening();
            }
        }
    }

    /**
     * Removes the movement tiles of the current selected entity. Can throw IndexOutOfBoundsExceptions if it goes outside
     * the board, so a try catch loop should be written around it.
     */
    public void removeMovementTiles() {
        for (Tile t : getMovableSquares(selectedEntity))
            if (t != null) {
                t.revertTileColor();
                t.stopListening();
            }
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
        if (!stm.has(e) || stm.get(e).getModSpd(e) == 0)
            return null;

        int spd = stm.get(e).getModSpd(e);
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
        //Normal Shading restrictions
        if (!state.get(e).canMove && !state.get(e).canAttack)
            am.get(e).actor.shade(Color.DARK_GRAY);
        else if (!state.get(e).canMove || !state.get(e).canAttack)
            am.get(e).actor.shade(Color.GRAY);
        //status effects
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            if (status.get(e).isPoisoned())
                am.get(e).actor.shade(StatusEffectComponent.poisonColor);
            else if (status.get(e).isBurned())
                am.get(e).actor.shade(StatusEffectComponent.burnColor);
            else if (status.get(e).isParalyzed())
                am.get(e).actor.shade(StatusEffectComponent.paralyzeColor);
            else if (status.get(e).isPetrified())
                am.get(e).actor.shade(StatusEffectComponent.petrifyColor);
            else if (status.get(e).isStill())
                am.get(e).actor.shade(StatusEffectComponent.stillnessColor);
            else if (status.get(e).isCursed())
                am.get(e).actor.shade(StatusEffectComponent.curseColor);
        } else { //defaults
            if (team.get(e).teamNumber == rules.getCurrentTeamNumber())
                am.get(e).actor.shade(rules.getCurrentTeam().getTeamColor());
            else
                am.get(e).actor.shade(Color.WHITE);
        }
    }

    /**
     * Checks if the entity is shaded the color that it would be if it was not selected
     * @param e Entity
     * @return true if the entity is correctly shaded. false otherwise. Also returns false if the entity has no state
     */
    public boolean checkShading(Entity e) {
        if (!state.has(e))
            return false;


        if (!(state.get(e).canMove || state.get(e).canAttack)) //shading to show cant attack/move
            return am.get(e).actor.getColor() == Color.GRAY;
        else if (!state.get(e).canMove && !state.get(e).canAttack)
            return am.get(e).actor.getColor() == Color.DARK_GRAY;
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) { //status effect
            if (status.get(e).isPoisoned())
                return am.get(e).actor.getColor() instanceof LerpColor && am.get(e).actor.getColor().equals(StatusEffectComponent.poisonColor);
            else if (status.get(e).isBurned())
                return am.get(e).actor.getColor() instanceof LerpColor && am.get(e).actor.getColor().equals(StatusEffectComponent.burnColor);
            else if (status.get(e).isParalyzed())
                return am.get(e).actor.getColor() instanceof LerpColor && am.get(e).actor.getColor().equals(StatusEffectComponent.paralyzeColor);
            else if (status.get(e).isPetrified())
                return am.get(e).actor.getColor() instanceof LerpColor && am.get(e).actor.getColor().equals(StatusEffectComponent.petrifyColor);
            else if (status.get(e).isStill())
                return am.get(e).actor.getColor() instanceof LerpColor && am.get(e).actor.getColor().equals(StatusEffectComponent.stillnessColor);
            else if (status.get(e).isCursed())
                return am.get(e).actor.getColor() instanceof LerpColor && am.get(e).actor.getColor().equals(StatusEffectComponent.curseColor);

        } else if (team.get(e).teamNumber == rules.getCurrentTeamNumber()) //defualts
            return am.get(e).actor.getColor() == rules.getCurrentTeam().getTeamColor();
        else
            return am.get(e).actor.getColor() == Color.WHITE;

        return false;
    }

    /**
     * Returns the color an Entity would be if it was not selected
     * @param e Entity
     * @return Color that it would be. Can be a {@code LerpColor}
     */
    public static Color getShadeColorBasedOnState(Entity e) {
        if (!state.has(e)) {
            return Color.WHITE;
        }
        //Normal Shading restrictions
        if (!state.get(e).canMove && !state.get(e).canAttack)
            return Color.DARK_GRAY;
        else if (!state.get(e).canMove || !state.get(e).canAttack)
            return Color.GRAY;
            //status effects
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            if (status.get(e).isPoisoned())
                return StatusEffectComponent.poisonColor;
            else if (status.get(e).isBurned())
                return StatusEffectComponent.burnColor;
            else if (status.get(e).isParalyzed())
                return StatusEffectComponent.paralyzeColor;
            else if (status.get(e).isPetrified())
                return StatusEffectComponent.petrifyColor;
            else if (status.get(e).isStill())
                return StatusEffectComponent.stillnessColor;
            else if (status.get(e).isCursed())
                return StatusEffectComponent.curseColor;

        } else { //defaults
            if (team.get(e).teamNumber == rules.getCurrentTeamNumber())
                return rules.getCurrentTeam().getTeamColor();
            else
              return Color.WHITE;
        }

        return null;
    }

    /**
     * Updates the HUD display that shows the information for the stats and attacks of an entity.
     */
    public void updateStatsAndMoves() {
        if (stm.has(selectedEntity)) {
            StatComponent stat = stm.get(selectedEntity);
            hpLbl.setText("" + stat.hp + " / " + stat.getModMaxHp(selectedEntity));
            spLbl.setText("" + stat.getModSp(selectedEntity) + " / " + stat.getModMaxSp(selectedEntity));
            atkLbl.setText("" + stat.getModAtk(selectedEntity));
            defLbl.setText("" + stat.getModDef(selectedEntity));
            spdLbl.setText("" + stat.getModSpd(selectedEntity));

            if (status.has(selectedEntity) && status.get(selectedEntity).getTotalStatusEffects() > 0) {
                nameLbl.setColor(new Color(Color.PINK));
                if (status.get(selectedEntity).isBurned()) {
                    nameLbl.setColor(Color.YELLOW);
                    hpLbl.setColor(Color.WHITE);
                    atkLbl.setColor(Color.RED);
                    defLbl.setColor(Color.WHITE);
                    spdLbl.setColor(Color.WHITE);
                }
                if (status.get(selectedEntity).isParalyzed()) {
                    nameLbl.setColor(Color.YELLOW);
                    hpLbl.setColor(Color.WHITE);
                    spLbl.setColor(Color.WHITE);
                    atkLbl.setColor(Color.WHITE);
                    defLbl.setColor(Color.WHITE);
                    spdLbl.setColor(Color.RED);
                }
                if (status.get(selectedEntity).isPetrified()) {
                    nameLbl.setColor(Color.YELLOW);
                    hpLbl.setColor(Color.WHITE);
                    spLbl.setColor(Color.WHITE);
                    atkLbl.setColor(Color.WHITE);
                    defLbl.setColor(Color.CYAN);
                    spdLbl.setColor(Color.RED);
                }
                if (status.get(selectedEntity).isStill()) {
                    nameLbl.setColor(Color.YELLOW);
                    hpLbl.setColor(Color.WHITE);
                    spLbl.setColor(Color.RED);
                    atkLbl.setColor(Color.WHITE);
                    defLbl.setColor(Color.WHITE);
                    spdLbl.setColor(Color.WHITE);
                }
                if (status.get(selectedEntity).isCursed()) {
                    nameLbl.setColor(Color.YELLOW);
                    hpLbl.setColor(Color.WHITE);
                    spLbl.setColor(Color.WHITE);
                    atkLbl.setColor(Color.RED);
                    defLbl.setColor(Color.RED);
                    spdLbl.setColor(Color.RED);
                }
            } else {
                nameLbl.setColor(Color.YELLOW);
                hpLbl.setColor(Color.WHITE);
                spLbl.setColor(Color.WHITE);
                atkLbl.setColor(Color.WHITE);
                defLbl.setColor(Color.WHITE);
                spdLbl.setColor(Color.WHITE);
            }
        } else {
            hpLbl.setText("-- / --");
            spLbl.setText("-- / --");
            atkLbl.setText("--");
            defLbl.setText("--");
            spdLbl.setText("--");
        }

        if (mvm.has(selectedEntity)) {
            MovesetComponent moves = mvm.get(selectedEntity);
            if (moves.moveList.size > 0 && moves.moveList.get(0) != null) {
                attackBtn1.setText(moves.moveList.get(0).getName() + " (" + moves.moveList.get(0).spCost() + ")");
            } else
                attackBtn1.setText("---");
            if (moves.moveList.size > 1 && moves.moveList.get(1) != null)
                attackBtn2.setText(moves.moveList.get(1).getName() + " (" + moves.moveList.get(1).spCost() + ")");
            else
                attackBtn2.setText("---");
            if (moves.moveList.size > 2 && moves.moveList.get(2) != null)
                attackBtn3.setText(moves.moveList.get(2).getName() + " (" + moves.moveList.get(2).spCost() + ")");
            else
                attackBtn3.setText("---");
            if (moves.moveList.size > 3 && moves.moveList.get(3) != null)
                attackBtn4.setText(moves.moveList.get(3).getName() + " (" + moves.moveList.get(0).spCost() + ")");
            else
                attackBtn4.setText("---");
        } else {
            attackBtn1.setText("---");
            attackBtn2.setText("---");
            attackBtn3.setText("---");
            attackBtn4.setText("---");
        }
        if (nm.has(selectedEntity))
            nameLbl.setText(nm.get(selectedEntity).name);
        else
            nameLbl.setText("???");
    }

    /**
     * Updates the bar with the icons of all the entities on a team.
     */
    public void updateTeamBar() {
        UIActor actor;
        Entity entity;
        Sprite temp;
        Image member;
        for (int i = 1; i < 5; i++) {
            switch (i) {
                case 1:
                    member = member1;
                    break;
                case 2:
                    member = member2;
                    break;
                case 3:
                    member = member3;
                    break;
                case 4:
                    member = member4;
                    break;
                default:
                    member = null;
            }
            if (rules.getCurrentTeam().getEntities().size < i || rules.getCurrentTeam().getEntities().get(i - 1) == null) {
                member.setColor(Color.WHITE);
                member.setDrawable(new TextureRegionDrawable(atlas.findRegion("Hole2")));
                continue;
            }
            actor = am.get(rules.getCurrentTeam().getEntities().get(i - 1)).actor;
            entity = rules.getCurrentTeam().getEntities().get(i - 1);

            if (actor instanceof SpriteActor) {
                temp = new Sprite(((SpriteActor) actor).getSprite());
                temp.setColor(Color.WHITE);
                member.setDrawable(new SpriteDrawable(temp));
            } else if (actor instanceof AnimationActor)
                member.setDrawable(new SpriteDrawable(((AnimationActor) actor).getInitialFrame()));

            if (stm.has(entity) && !(status.has(entity) && status.get(entity).getTotalStatusEffects() > 0))
                if (stm.get(entity).hp == 0)
                    member.setColor(Color.BLACK);
                else
                    member.setColor(new Color(1, 1, 1, 1).lerp(Color.RED, 1f - (float) stm.get(entity).hp / (float) stm.get(entity).getModMaxHp(entity)));
        }

        /*
        if (rules.getCurrentTeam().getEntities().size >= 1 && rules.getCurrentTeam().getEntities().get(0) != null) {
            actor = am.get(rules.getCurrentTeam().getEntities().get(0)).actor;
            entity = rules.getCurrentTeam().getEntities().get(0);
            if (actor instanceof SpriteActor) {
                temp = new Sprite(((SpriteActor) actor).getSprite());
                temp.setColor(Color.WHITE);
                member1.setDrawable(new SpriteDrawable(temp));
            } else if (actor instanceof AnimationActor)
                member1.setDrawable(new SpriteDrawable(((AnimationActor) actor).getInitialFrame()));

            if (stm.has(entity) && !(status.has(entity) && status.get(entity).getTotalStatusEffects() > 0))
                if (stm.get(entity).hp == 0)
                    member1.setColor(Color.BLACK);
                else
                    member1.setColor(new Color(1, 1, 1, 1).lerp(Color.RED, 1f - (float) stm.get(entity).hp / (float) stm.get(entity).getModMaxHp(entity)));
        } else {
            member1.setColor(Color.WHITE);
            member1.setDrawable(new TextureRegionDrawable(atlas.findRegion("Hole2")));
        }

        if (rules.getCurrentTeam().getEntities().size >= 2 && rules.getCurrentTeam().getEntities().get(1) != null) {
            actor = am.get(rules.getCurrentTeam().getEntities().get(1)).actor;
            entity = rules.getCurrentTeam().getEntities().get(1);
            if (actor instanceof SpriteActor) {
                temp = new Sprite(((SpriteActor) actor).getSprite());
                temp.setColor(Color.WHITE);
                member2.setDrawable(new SpriteDrawable(temp));
            } else if (actor instanceof AnimationActor)
                member2.setDrawable(new SpriteDrawable(((AnimationActor) actor).getInitialFrame()));

            if (stm.has(entity) && !(status.has(entity) && status.get(entity).getTotalStatusEffects() > 0))
                if (stm.get(entity).hp == 0)
                    member2.setColor(Color.BLACK);
                else
                    member2.setColor(new Color(1, 1, 1, 1).lerp(Color.RED, 1f - (float) stm.get(entity).hp / (float) stm.get(entity).getModMaxHp(entity)));
        } else {
            member2.setColor(Color.WHITE);
            member2.setDrawable(new TextureRegionDrawable(atlas.findRegion("Hole2")));
        }

        if (rules.getCurrentTeam().getEntities().size >= 3 && rules.getCurrentTeam().getEntities().get(2) != null) {
            actor = am.get(rules.getCurrentTeam().getEntities().get(2)).actor;
            entity = rules.getCurrentTeam().getEntities().get(2);
            if (actor instanceof SpriteActor) {
                temp = new Sprite(((SpriteActor) actor).getSprite());
                temp.setColor(Color.WHITE);
                member3.setDrawable(new SpriteDrawable(temp));
            } else if (actor instanceof AnimationActor)
                member3.setDrawable(new SpriteDrawable(((AnimationActor) actor).getInitialFrame()));

            if (stm.has(entity) && !(status.has(entity) && status.get(entity).getTotalStatusEffects() > 0))
                if (stm.get(entity).hp == 0)
                    member3.setColor(Color.BLACK);
                else
                    member3.setColor(new Color(1, 1, 1, 1).lerp(Color.RED, 1f - (float) stm.get(entity).hp / (float) stm.get(entity).getModMaxHp(entity)));
        } else {
            member3.setColor(Color.WHITE);
            member3.setDrawable(new TextureRegionDrawable(atlas.findRegion("Hole2")));
        }

        if (rules.getCurrentTeam().getEntities().size >= 4 && rules.getCurrentTeam().getEntities().get(3) != null) {
            actor = am.get(rules.getCurrentTeam().getEntities().get(3)).actor;
            entity = rules.getCurrentTeam().getEntities().get(3);
            if (actor instanceof SpriteActor) {
                temp = new Sprite(((SpriteActor) actor).getSprite());
                temp.setColor(Color.WHITE);
                member4.setDrawable(new SpriteDrawable(temp));
            } else if (actor instanceof AnimationActor)
                member4.setDrawable(new SpriteDrawable(((AnimationActor) actor).getInitialFrame()));

            if (stm.has(entity) && !(status.has(entity) && status.get(entity).getTotalStatusEffects() > 0))
                if (stm.get(entity).hp == 0)
                    member4.setColor(Color.BLACK);
                else
                    member4.setColor(new Color(1, 1, 1, 1).lerp(Color.RED, 1f - (float) stm.get(entity).hp / (float) stm.get(entity).getModMaxHp(entity)));
        } else {
            member4.setColor(Color.WHITE);
            member4.setDrawable(new TextureRegionDrawable(atlas.findRegion("Hole2")));
        }
        */
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

    /**
     * Checks if an entity has the criteria to use attacks.
     * Criteria : not null, has {@code MovesetComponent}, has {@code StateComponent}, can attack,
     * has {@code TeamComponent}, the current team turn is the same as the entity's team
     * @param e Entity
     * @return true if it may attack. False otherwise
     */
    public boolean mayAttack(Entity e) {
        return e != null && mvm.has(e) && state.has(e) && state.get(e).canAttack && team.has(e) &&
                team.get(e).teamNumber == rules.getCurrentTeamNumber() && !(status.has(selectedEntity) && status.get(selectedEntity).isPetrified());
    }

    /**
     * Checks if the entity has the criteria to move.
     * Criteria : has {@code StatComponent}, speed is > 0, can move, and current team turn is the same as the entity's team
     * @param e Entity
     * @return true if it can move, false otherwise.
     */
    public boolean mayMove(Entity e) {
        return stm.has(e) && stm.get(e).getModSpd(e) > 0 && state.has(e) &&
                state.get(e).canMove && team.has(e) && team.get(e).teamNumber == rules.getCurrentTeamNumber();
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

    public void setTeams(Team... team) {
        for (Team t : team) {
            teams.add(t);
        }

        for (Team t : teams) {
            for (Entity e : t.getEntities()) {
                engine.addEntity(e);
                if (am.has(e)) stage.addActor(am.get(e).actor);
            }
        }

        rules.calculateTotalTeams();
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

    public Rules getRules() {
        return rules;
    }
}
