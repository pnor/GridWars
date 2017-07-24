package com.mygdx.game.screens_ui.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.utils.StringBuilder;
import com.mygdx.game.AI.BoardState;
import com.mygdx.game.AI.ComputerPlayer;
import com.mygdx.game.AI.Turn;
import com.mygdx.game.GridWars;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.actors.UIActor;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.*;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.creators.BoardAndRuleConstructor;
import com.mygdx.game.creators.MoveConstructor;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.move_related.StatusEffect;
import com.mygdx.game.move_related.Visuals;
import com.mygdx.game.rules_types.Rules;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.rules_types.ZoneRules;
import com.mygdx.game.screens_ui.*;
import com.mygdx.game.systems.*;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.*;

/**
 * @author pnore_000
 */
public class BattleScreen implements Screen {

    private final GridWars GRID_WARS;
    private BattleInputProcessor battleInputProcessor;

    //Background
    Background background;

    //Board
    //private final Board board = new Board(7, 7, new Color(221f / 255, 221f / 255f, 119f / 255f, 1), new Color(1, 1, 102f / 255f, 1));
        /*7 is the basic size. Requires no scaling with anything below 7 size
        The math for scaling above 7 : 700 / size
        */

    //rules
    private static Rules rules;
    private boolean gameHasEnded;
    private int winningTeamIndex;
    private float changeScreenTimer;

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

    //Computer Turn variables
    private final ComputerPlayer computer;
    private int[] computerControlledTeamsIndex;
    private boolean playingComputerTurn;
    private float timeAfterMove;
    private int currentComputerControlledEntity;
    /**
     * Waiting to... <p>
     * 0 : move   1 : attack 2 : time after attack* 3 : waiting to end turn */
    private int turnPhase;

    //Ui Elements
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
            spdLblID, spdLbl;
    private NewsTickerLabel statusLbl;

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
    /** Pop up message that says the effects of a move. */
    private Table helpTable;
    private Label moveDescriptionLbl;
    private HoverButton closeHelpMenuBtn;

    //debug values
    private float deltaTimeSums;
    private final int deltatimeIntervals = 10000;
    private int currentDeltaTime;

    public BattleScreen(Array<Team> selectedTeams, int boardIndex, int[] AIControlled, GridWars game) {
        GRID_WARS = game;
        teams = selectedTeams;
        if (BoardComponent.boards == null)
            BoardComponent.boards = new BoardManager();
        rules = BoardAndRuleConstructor.getBoardAndRules(boardIndex, this, teams, BoardComponent.boards);
        background = BackgroundConstructor.getBackground(boardIndex);

        MoveConstructor.initialize(BoardComponent.boards.getBoard().getScale(), BoardComponent.boards, engine, stage);

        if (rules instanceof ZoneRules)
            computer = new ComputerPlayer(BoardComponent.boards, teams, ((ZoneRules) rules).getZones(), 1, 4);
        else
            computer = new ComputerPlayer(BoardComponent.boards, teams, 1, 4);

        //for now
        computerControlledTeamsIndex = AIControlled;
    }

    @Override
    public void show() {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        stage.clear();

        for (EntitySystem system : engine.getSystems()) {
            engine.removeSystem(system);
        }

        //set up assets
        boardTable = new Table();
        statsTable = new Table();
        attackTable = new Table();
        infoTable = new Table();
        teamTable = new Table();
        endTurnMessageTable = new Table();
        //helpTable = new Table();
        stage.addActor(boardTable);
        stage.addActor(statsTable);
        stage.addActor(attackTable);
        stage.addActor(infoTable);
        stage.addActor(teamTable);
        stage.addActor(endTurnMessageTable);
        //stage.addActor(helpTable);
        battleInputProcessor = new BattleInputProcessor(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, battleInputProcessor));

        //Set up Engine
        engine.addSystem(new DrawingSystem(stage.getBatch()));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new DamageDeathSystem());

        //Add all things on board to engine
        for (Entity e : BoardComponent.boards.getAllEntities())
            engine.addEntity(e);

        //Set up visuals
        Visuals.engine = engine;
        Visuals.stage = stage;

        //set up Board ui -----
        for (int i = 0; i < BoardComponent.boards.getBoard().getRowSize(); i++) {
            for (int j = 0; j < BoardComponent.boards.getBoard().getColumnSize(); j++) {
                if (i == 0)
                    boardTable.add().width(BoardComponent.boards.getBoard().getTiles().get(0).getWidth()).height(BoardComponent.boards.getBoard().getTiles().get(0).getHeight());
                else
                    boardTable.add().height(BoardComponent.boards.getBoard().getTiles().get(0).getHeight());
            }
            boardTable.row();
        }
        boardTable.pack();
        boardTable.setPosition(stage.getWidth() / 2.5f - BoardComponent.boards.getBoard().getTiles().get(0).getWidth() * BoardComponent.boards.getBoard().getRowSize() / 2f,
                stage.getHeight() / 2f - BoardComponent.boards.getBoard().getTiles().get(0).getHeight() * BoardComponent.boards.getBoard().getColumnSize() / 2f);
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
        param.size = 17;
        statusLbl = new NewsTickerLabel(new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE), "Healthy", 12, .05f, .3f);
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
        statsTable.add(statusLbl).colspan(2).size(120, 40);
        statusLbl.setAlignment(Align.center);
            //statsTable.debug();
        statsTable.setBackground(tableBackground);
        statsTable.pack();
        statsTable.setPosition(stage.getWidth() * .875f - (statsTable.getWidth() / 2), stage.getHeight() * .725f - (statsTable.getHeight() / 2));

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
                    if (mayAttack(selectedEntity)) { //Use attack at button position or say they don't have enough SP
                        if (actor == attackBtn1) {
                            if (mvm.get(selectedEntity).moveList.get(0).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(0).useAttack();
                            stm.get(selectedEntity).sp -= mvm.get(selectedEntity).moveList.get(0).spCost();
                            currentMove = mvm.get(selectedEntity).moveList.get(0);
                        } else if (actor == attackBtn2) {
                            if (mvm.get(selectedEntity).moveList.get(1).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(1).useAttack();
                            stm.get(selectedEntity).sp -= mvm.get(selectedEntity).moveList.get(1).spCost();
                            currentMove = mvm.get(selectedEntity).moveList.get(1);
                        } else if (actor == attackBtn3) {
                            if (mvm.get(selectedEntity).moveList.get(2).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(2).useAttack();
                            stm.get(selectedEntity).sp -= mvm.get(selectedEntity).moveList.get(2).spCost();
                            currentMove = mvm.get(selectedEntity).moveList.get(2);
                        } else if (actor == attackBtn4) {
                            if (mvm.get(selectedEntity).moveList.get(3).spCost() > stm.get(selectedEntity).getModSp(selectedEntity)) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(3).useAttack();
                            stm.get(selectedEntity).sp -= mvm.get(selectedEntity).moveList.get(3).spCost();
                            currentMove = mvm.get(selectedEntity).moveList.get(3);
                        }
                        teams.get(team.get(selectedEntity).teamNumber).incrementTotalAttacksUsed();

                        //set canAttack and canMove state to false, and begin move's Visuals.
                        state.get(selectedEntity).canAttack = false;
                        state.get(selectedEntity).canMove = false;
                        //disable UI stuff to stop moving after attacking
                        removeMovementTiles();
                        disableUI();

                        showAttackMessage(nm.get(selectedEntity).name, currentMove);
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
        attackTable.setPosition(stage.getWidth() * .875f - (attackTable.getWidth() / 2), stage.getHeight() * .225f - (attackTable.getWidth() / 2));

        //set up infoTable
        infoLbl = new GradualLabel(.001f, "---", skin);
        infoTable.add(infoLbl).height(25).center();
        infoTable.setBackground(tableBackground);
        infoTable.pack();
        infoTable.setPosition(boardTable.getX(), stage.getHeight() * .9f);
        infoTable.setSize(700, 80);

        //set up team table
        endTurnBtn = new HoverButton("End Turn", skin, new Color(200f / 255f, 200f / 255f, 255f / 255f, 1), new Color(.8f, 1f, 1f, 1));
        endTurnBtn.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               if (((Button) actor).isPressed()) {
                   nextTurn();
               }
           }
        });
        member1 = new Image(GRID_WARS.atlas.findRegion("mystery"));
        member2 = new Image(GRID_WARS.atlas.findRegion("mystery"));
        member3 = new Image(GRID_WARS.atlas.findRegion("mystery"));
        member4 = new Image(GRID_WARS.atlas.findRegion("mystery"));
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
        endTurnMessageTable.add(endTurnMessageLbl).padBottom(5f).row();
        endTurnMessageTable.add(turnCountLbl);
        endTurnMessageTable.setColor(Color.WHITE);
        endTurnMessageTable.addAction(Actions.fadeOut(0f));

       /*
        moveDescriptionLbl = new Label("88888888888", skin);
        closeHelpMenuBtn = new HoverButton("Close", skin, Color.WHITE, Color.CYAN);
        closeHelpMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {

                }
            }
        });
        helpTable.add(moveDescriptionLbl).padBottom(20f).row();
        helpTable.add(closeHelpMenuBtn);
        helpTable.setBackground(tableBackground);
        helpTable.pack();
        helpTable.setPosition(stage.getWidth() / 2, stage.getHeight() / 2);
        helpTable.setSize(stage.getWidth() / 3f, stage.getHeight() / 3);
        */

        //Start first turn
        nextTurn();

        /*
        endTurnMessageLbl.setText("" + rules.getCurrentTeam().getTeamName() + " turn!");
        turnCountLbl.setText("Turn " + rules.getTurnCount());
        turnCountLbl.setColor(new Color(1,1,1,1).lerp(Color.ORANGE, (float) rules.getTurnCount() / 100f));
        endTurnMessageTable.setColor(rules.getCurrentTeam().getTeamColor());
        endTurnMessageTable.clearActions();
        SequenceAction sequence = new SequenceAction();
        sequence.addAction(Actions.fadeIn(.2f));
        sequence.addAction(Actions.delay(1.5f));
        sequence.addAction(Actions.fadeOut(.2f));
        endTurnMessageTable.addAction(sequence);
        for (Entity e : rules.getCurrentTeam().getEntities())
            shadeBasedOnState(e);
            */

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

        //Debug
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
        int rowSize = BoardComponent.boards.getBoard().getRowSize();
        int colSize = BoardComponent.boards.getBoard().getColumnSize();
        int curRow = 0;
        int cur = 0;
        Array<Cell> cells = boardTable.getCells();

        for (int i = 0; i < BoardComponent.boards.getBoard().getRowSize() * BoardComponent.boards.getBoard().getColumnSize(); i++) {
            cells.get(i).setActor(BoardComponent.boards.getBoard().getTile(curRow, cur % rowSize));
            cur += 1;
            curRow = cur / colSize;
        }
        boardTable.getCells();

        //region player input stuff (selection, attacks, etc.)
        if (!gameHasEnded && !playingComputerTurn) {
            //update last ENTITY selected ---
            for (Entity e : BoardComponent.boards.getCodeBoard().getEntities()) {
                if (am.get(e).actor.getLastSelected()) {
                    try {   //removes previously highlighted tiles
                        if (selectedEntity != null && stm.has(selectedEntity) && stm.get(selectedEntity).getModSpd(selectedEntity) > 0)
                            removeMovementTiles();
                    } catch (IndexOutOfBoundsException exc) {
                    }

                    if (Visuals.visualsArePlaying == 0 && selectedEntity != null) //stop orange highlight
                        shadeBasedOnState(selectedEntity); //TODO make a way to show multiple status effects well

                    selectedEntity = e; //selectedEntity changes to new entity from here on

                    if (Visuals.visualsArePlaying == 0) {
                        am.get(selectedEntity).actor.shade(Color.ORANGE);
                        if (mayAttack(selectedEntity) && !attacksEnabled)
                            enableAttacks();
                    }

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
                        BoardComponent.boards.move(selectedEntity, new BoardPosition(t.getRow(), t.getColumn()));
                        state.get(selectedEntity).canMove = false;
                    }
                }
            }

            //updating attack squares
            if (!mayAttack(selectedEntity) && attacksEnabled)
                disableAttacks();
            else if (mayAttack(selectedEntity) && !attacksEnabled && Visuals.visualsArePlaying == 0)
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
        }
        //endregion

        //computer Turn
        if (playingComputerTurn && !computer.getProcessing())
            processComputerTurn(delta);

        //playing current move animation
        if (currentMove != null) {
            if (currentMove.getVisuals().getIsPlaying()) {
                currentMove.updateVisuals(delta);
                currentMove.getVisuals().play();
            } else {
                currentMove.getVisuals().reset();
                enableUI();
                if (selectedEntity != null)
                    updateStatsAndMoves();
                currentMove = null;
            }
        }

        //update everything. (Graphics, engine, GRID_WARS.stage)
        background.update(delta);
        stage.act(delta);
        engine.getSystem(DrawingSystem.class).drawBackground(background, delta);
        stage.draw();
        engine.update(delta);
        //update LerpColor of teams
        for (Team t : teams)
            if (t.getTeamColor() instanceof LerpColor)
                ((LerpColor) t.getTeamColor()).update(delta);

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
                endTurnBtn.setDisabled(true);
                gameHasEnded = true;
            }

            //fade to black
            if (changeScreenTimer >= 3) {
                Entity blackCover = new Entity();
                Sprite darkness = (atlas.createSprite("DarkTile"));
                darkness.setColor(new Color(0, 0, 0, 0));
                blackCover.add(new SpriteComponent(darkness));
                blackCover.add(new PositionComponent(new Vector2(0, 0), stage.getHeight(), stage.getWidth(), 0));
                blackCover.add(new EventComponent(.005f, 0, true, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().cpy().add(0, 0, 0, .05f));
                }));
                engine.addEntity(blackCover);
            }

            //go to results screen
            if (changeScreenTimer >= 4)
                GRID_WARS.setScreen(new EndResultsScreen(teams, teams.indexOf(rules.checkWinConditions(), true), rules, GRID_WARS));
            changeScreenTimer += delta;
        }

        //debug
        if (Visuals.visualsArePlaying < 0)
            throw (new IndexOutOfBoundsException("Visuals.visualsArePlaying is < 0"));
        for (Team t : teams) {
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
        //debug: get values via key press
        if (Gdx.input.isKeyJustPressed(Input.Keys.V))
            System.out.println("Visuals.visualsArePlaying = " + Visuals.visualsArePlaying);
        if (Gdx.input.isKeyJustPressed(Input.Keys.F))
            System.out.println("Frames per Second: " + Gdx.graphics.getFramesPerSecond());

    }

    private void processComputerTurn(float delta) {
        //if game has ended stop
        if (gameHasEnded) {
            return;
        }

        //Getting the Turn. Null if dead.
        Entity currentEntity;
        Turn currentTurn =
                (currentComputerControlledEntity < computer.getDecidedTurns().size)? computer.getDecidedTurns().get(currentComputerControlledEntity) : null;
        timeAfterMove += delta;

        if ((currentTurn == null && currentComputerControlledEntity < computer.getDecidedTurns().size) || (currentTurn != null && !stm.get(currentTurn.entity).alive)) { //entity is dead/skip turn
            currentComputerControlledEntity++;
            turnPhase = 0;
            timeAfterMove = 0;
        }

        //Playing out the Turn
        if (timeAfterMove >= .1f && turnPhase == 0) { //Move
            try {
                BoardComponent.boards.move(currentTurn.entity, currentTurn.pos);
            } catch (Exception e) {
                System.out.println("\n \n --------------------------------" +
                        "\n Exception! : " + e + "  in BattleScreen 'Playing out the Turn'" +
                        "\n currentTurn = " + currentTurn +
                        "\n board size = " + BoardComponent.boards.getBoard().getRowSize()
                );
                Gdx.app.exit();
            }
            BoardComponent.boards.move(currentTurn.entity, currentTurn.pos);
            turnPhase = 1;
        } else if (timeAfterMove >= .5f && turnPhase == 1) { //use attack
            currentEntity = currentTurn.entity;
            if (currentTurn.attack != -1) {
                if (currentTurn.direction > 0)
                    for (int i = 0; i < currentTurn.direction; i++)
                        Move.orientAttack(true, mvm.get(currentEntity).moveList.get(currentTurn.attack));
                mvm.get(currentEntity).moveList.get(currentTurn.attack).useAttack();
                teams.get(team.get(currentEntity).teamNumber).incrementTotalAttacksUsed();
                stm.get(currentEntity).sp -= mvm.get(currentEntity).moveList.get(currentTurn.attack).spCost();
                currentMove = mvm.get(currentEntity).moveList.get(currentTurn.attack);
                showAttackMessage(nm.get(currentEntity).name, mvm.get(currentEntity).moveList.get(currentTurn.attack));
                turnPhase = 2;
            } else {
                currentComputerControlledEntity++;
                turnPhase = 0;
                timeAfterMove = 0;
            }
        } else if (currentMove == null && turnPhase == 2) { //next entity
            timeAfterMove = 0;
            turnPhase = 0;
            currentComputerControlledEntity++;
        }

        //Next turn or end the turn
        if (turnPhase == 3) { //next turn
            timeAfterMove += delta;
            if (timeAfterMove >= .75f) { //wait to end turn
                timeAfterMove = 0;
                turnPhase = 0;
                currentComputerControlledEntity = 0;
                if (!gameHasEnded)
                    nextTurn();
            }
        } else if (currentComputerControlledEntity >= computer.getTeamSize()) { //Check if ran through all turns
            timeAfterMove = 0;
            turnPhase = 3;
        }
    }

    /**
     * Shows the attack message of the Move stored in Current Move.
     */
    private void showAttackMessage(String name, Move move) {
        if (move.getAttackMessage() == null || move.getAttackMessage().trim().equals("")) { //default case
            if (name != null)
                if (move.getAttackMessage() != null)
                    infoLbl.setText(move.getAttackMessage());
                else
                    infoLbl.setText(name + " used " + move.getName() + "!");
            else if (move.getAttackMessage() != null)
                infoLbl.setText(move.getAttackMessage());
            else
                infoLbl.setText(move.getName() + " was used!");
        } else {
            infoLbl.setText(move.getAttackMessage());
        }
    }

    /**
     * Ends the current turn and starts the next.
     */
    private void nextTurn() {
        disableUI();
        rules.nextTurn();
        showEndTurnDisplay();

        if (!gameHasEnded) {
            boolean processingAComputerControlledTeam = false;
            int controlledTeamIndex = -1;
            for (int i = 0; i < computerControlledTeamsIndex.length; i++) {
                if (computerControlledTeamsIndex[i] == rules.getCurrentTeamNumber()) {
                    processingAComputerControlledTeam = true;
                    controlledTeamIndex = i;
                    break;
                }
            }

            if (processingAComputerControlledTeam) {
                playingComputerTurn = true;
                computer.setTeamControlled(computerControlledTeamsIndex[controlledTeamIndex]);

                if (rules instanceof ZoneRules)
                    computer.updateComputerPlayer(new BoardState(BoardComponent.boards.getCodeBoard().getEntities(), ((ZoneRules) rules).getZones()));
                else
                    computer.updateComputerPlayer(new BoardState(BoardComponent.boards.getCodeBoard().getEntities(), null));

                new Thread(computer).start();
            } else
                playingComputerTurn = false;

            enableUI();
        }
    }

    /**
     * Shows the window that displays who's turn it is.
     */
    public void showEndTurnDisplay() {
        //show next turn message
        endTurnMessageLbl.setText(rules.getCurrentTeam().getTeamName() + " turn!");
        turnCountLbl.setText("Turn " + rules.getTurnCount());
        turnCountLbl.setColor(new Color(1,1,1,1).lerp(Color.ORANGE, (float) rules.getTurnCount() / 100f));
        Color teamColor = rules.getCurrentTeam().getTeamColor();
        if (teamColor instanceof LerpColor)
            endTurnMessageTable.setColor(Color.WHITE);
        else
            endTurnMessageTable.setColor(rules.getCurrentTeam().getTeamColor());
        endTurnMessageTable.clearActions();
        SequenceAction sequence = new SequenceAction();
        sequence.addAction(Actions.fadeIn(.2f));
        sequence.addAction(Actions.delay(1f));
        sequence.addAction(Actions.fadeOut(.2f));
        endTurnMessageTable.addAction(sequence);

        //update entity appearance
        for (Entity e : rules.getCurrentTeam().getEntities()) {
            shadeBasedOnState(e);
        }
    }

    public void showMovementTiles() {
        for (Tile t : getMovableSquares(bm.get(selectedEntity).pos, stm.get(selectedEntity).getModSpd(selectedEntity))) {
            if (t.isOccupied())
                continue;
            if (!t.getIsListening()) {
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
        for (Tile t : getMovableSquares(bm.get(selectedEntity).pos, stm.get(selectedEntity).getModSpd(selectedEntity)))
            if (t != null) {
                t.revertTileColor();
                t.stopListening();
            }
    }

    /**
     * Algorithm that returns all tiles that can be moved to based on speed. Calls a recursive method. Takes into account barriers and blockades, while
     * avoiding duplicates of the same tile. Note that this returns tiles horizontal of the entity multiple times.
     * @param bp Position that is being branched from
     * @param spd remaining tiles the entity can move
     * @return {@link Array} of {@link Tile}s.
     */
    private Array<Tile> getMovableSquares(BoardPosition bp, int spd) {
        BoardPosition next = new BoardPosition(-1, -1);
        Array<Tile> tiles = new Array<>();

        if (spd == 0)
            return tiles;

        //get spread of tiles upwards
        getMovableSquaresSpread(bp, bp, spd, tiles, -1, 2, true);
        //get spread of tiles downwards
        getMovableSquaresSpread(bp, bp, spd, tiles, -1, 0, true);

        return tiles;
    }

    /**
     * Recursive algorithm that returns all tiles in one direction that can be moved to based on speed. Takes into account barriers and blockades.
     * @param bp Position that is being branched from
     * @param spd remaining tiles the entity can move
     * @param tiles {@link Array} of tiles that can be moved on
     * @param directionCameFrom direction the previous tile came from. Eliminates the need to check if the next tile is already in the
     *                          {@link Array}.
     *                          <p>-1: No direction(starting)
     *                          <p>0: top
     *                          <p>1: left
     *                          <p>2: bottom
     *                          <p>3: right
     * @param sourceDirection the direction it is branching from. This prevents the "U-Turns" that would overlap with other directions
     *                        <p>-1: No direction(starting)
     *                          <p>0: top
     *                          <p>1: left
     *                          <p>2: bottom
     *                          <p>3: right
     * @return {@link Array} of {@link Tile}s.
     */
    private Array<Tile> getMovableSquaresSpread(BoardPosition sourceBp, BoardPosition bp, int spd, Array<Tile> tiles, int directionCameFrom, int sourceDirection, boolean includeHorizontalSpaces) {
        BoardPosition next = new BoardPosition(-1, -1);

        if (spd == 0)
            return tiles;

        for (int i = 0; i < 4; i++) {
            if (directionCameFrom == i || sourceDirection == i) //Already checked tile -> skip!
                continue;

            if (i == 0) //set position
                next.set(bp.r - 1, bp.c);
            else if (i == 1)
                next.set(bp.r, bp.c - 1);
            else if (i == 2)
                next.set(bp.r + 1, bp.c);
            else if (i == 3)
                next.set(bp.r, bp.c + 1);

            //check if valid
            if (next.r >= BoardComponent.boards.getBoard().getRowSize() || next.r < 0
                    || next.c >= BoardComponent.boards.getBoard().getColumnSize() || next.c < 0
                    || BoardComponent.boards.getBoard().getTile(next.r, next.c).isOccupied())
                continue;

            if (!includeHorizontalSpaces && next.r == sourceBp.r)
                continue;

            //recursively call other tiles
            tiles.add(BoardComponent.boards.getBoard().getTile(next.r, next.c));
            getMovableSquaresSpread(sourceBp, next, spd - 1, tiles, (i + 2) % 4, sourceDirection, includeHorizontalSpaces);
        }

        return tiles;
    }

    public void showAttackTiles() {
        //show Attack Squares visual ---
        if (selectedEntity != null && mvm.has(selectedEntity)) {
            if (moveHover > -1) {
                //highlight attack squares
                if (mvm.get(selectedEntity).moveList.size > moveHover) {
                    for (BoardPosition pos :  mvm.get(selectedEntity).moveList.get(moveHover).getRange()) {
                        try {
                            BoardComponent.boards.getBoard().getTile(pos.r + bm.get(selectedEntity).pos.r, pos.c + bm.get(selectedEntity).pos.c).shadeTile(Color.RED);
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
                        Tile currTile = BoardComponent.boards.getBoard().getTile(pos.r + bm.get(selectedEntity).pos.r, pos.c + bm.get(selectedEntity).pos.c);
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
            am.get(e).actor.shade(status.get(e).statusEffects.values().toArray().first().getColor());
        } else { //defaults
            if (team.get(e).teamNumber == rules.getCurrentTeamNumber())
                am.get(e).actor.shade(rules.getCurrentTeam().getTeamColor().cpy());
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
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0)  //status effect
            return am.get(e).actor.getColor().equals(status.get(e).statusEffects.values().toArray().first().getColor());
        else if (team.get(e).teamNumber == rules.getCurrentTeamNumber()) //defualts
            return am.get(e).actor.getColor() == rules.getCurrentTeam().getTeamColor();
        else
            return am.get(e).actor.getColor() == Color.WHITE;
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
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0)
            return status.get(e).statusEffects.values().toArray().first().getColor();
        else { //defaults
            if (team.get(e).teamNumber == rules.getCurrentTeamNumber())
                return rules.getCurrentTeam().getTeamColor();
            else
              return Color.WHITE;
        }
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
            if (team.has(selectedEntity))
                nameLbl.setColor(teams.get(team.get(selectedEntity).teamNumber).getTeamColor());
            else
                nameLbl.setColor(Color.WHITE);
            hpLbl.setColor(Color.WHITE);
            spLbl.setColor(Color.WHITE);
            atkLbl.setColor(Color.WHITE);
            defLbl.setColor(Color.WHITE);
            spdLbl.setColor(Color.WHITE);
            statusLbl.setColor(Color.GREEN);
            if (status.has(selectedEntity) && status.get(selectedEntity).getTotalStatusEffects() > 0) {
                /* TODO optimize so it doesn't shade labels again if the shade does not need to be changed. (not essential)
                TODO change to make it more modular (using StatusEffects in StatusEffectComponent to figure out what to shade
                For example, switching from a burned entity to a cursed one will shade attack label red again.
                 */
                statusLbl.setColor(Color.RED);
                //Positive
                if (status.get(selectedEntity).statusEffects.containsKey("Quick")) {
                    spdLbl.setColor(Color.GREEN);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Power")) {
                    atkLbl.setColor(Color.GREEN);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Guard")) {
                    defLbl.setColor(Color.GREEN);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Durability")) {
                    hpLbl.setColor(Color.GREEN);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Charged") || status.get(selectedEntity).statusEffects.containsKey("Supercharged")) {
                    hpLbl.setColor(Color.GREEN);
                    spdLbl.setColor(Color.GREEN);
                    atkLbl.setColor(Color.GREEN);
                    spdLbl.setColor(Color.GREEN);
                }

                //Negative
                if (status.get(selectedEntity).statusEffects.containsKey("Burn"))
                    atkLbl.setColor(Color.RED);

                if (status.get(selectedEntity).statusEffects.containsKey("Paralyze"))
                    spdLbl.setColor(Color.RED);

                if (status.get(selectedEntity).statusEffects.containsKey("Petrify")) {
                    defLbl.setColor(Color.CYAN);
                    spdLbl.setColor(Color.RED);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Freeze")) {
                    defLbl.setColor(Color.RED);
                    spdLbl.setColor(Color.RED);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Shivers")) {
                    spLbl.setColor(Color.RED);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Defenseless")) {
                    defLbl.setColor(Color.RED);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Offenseless")) {
                    atkLbl.setColor(Color.RED);
                }

                if (status.get(selectedEntity).statusEffects.containsKey("Stillness"))
                    spLbl.setColor(Color.RED);

                if (status.get(selectedEntity).statusEffects.containsKey("Curse")) {
                    atkLbl.setColor(Color.RED);
                    defLbl.setColor(Color.RED);
                    spdLbl.setColor(Color.RED);
                }
                //status effect label
                StringBuilder statusEffects = new StringBuilder();
                for (StatusEffect effect : status.get(selectedEntity).statusEffects.values())
                    statusEffects.append(effect.getName());

                for (int i = 1; i < statusEffects.length(); i++) {
                    if (statusEffects.charAt(i) == statusEffects.toString().toUpperCase().charAt(i)) {
                        statusEffects.insert(i, ", ");
                        i += 2;
                    }
                }
                statusLbl.reset();
                statusLbl.setText(statusEffects.toString());
            } else
                statusLbl.setText("Healthy");
        } else {
            hpLbl.setText("-- / --");
            spLbl.setText("-- / --");
            atkLbl.setText("--");
            defLbl.setText("--");
            spdLbl.setText("--");
            statusLbl.setText("---");
            nameLbl.setColor(Color.YELLOW);
            hpLbl.setColor(Color.WHITE);
            atkLbl.setColor(Color.WHITE);
            defLbl.setColor(Color.WHITE);
            spdLbl.setColor(Color.WHITE);
            statusLbl.setColor(Color.WHITE);
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
                attackBtn4.setText(moves.moveList.get(3).getName() + " (" + moves.moveList.get(3).spCost() + ")");
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
     * Updates the bar with the icons of all the entities on a team. Shades the icons based on health and status effects.
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
            //smaller than current loop iteration OR team member is null
            if (rules.getCurrentTeam().getEntities().size < i || rules.getCurrentTeam().getEntities().get(i - 1) == null) {
                member.setColor(Color.WHITE);
                member.setDrawable(new TextureRegionDrawable(GRID_WARS.atlas.findRegion("mystery")));
                continue;
            }
            actor = am.get(rules.getCurrentTeam().getEntities().get(i - 1)).actor;
            entity = rules.getCurrentTeam().getEntities().get(i - 1);

            if (actor instanceof SpriteActor) {  //set image
                temp = new Sprite(((SpriteActor) actor).getSprite());
                temp.setColor(Color.WHITE);
                member.setDrawable(new SpriteDrawable(temp));
            } else if (actor instanceof AnimationActor)
                member.setDrawable(new SpriteDrawable(((AnimationActor) actor).getInitialFrame()));
            //color image
            if (stm.has(entity) && stm.get(entity).hp <= 0 && !(status.has(entity) && status.get(entity).getTotalStatusEffects() > 0)) { //Shade based on health
                if (stm.get(entity).hp <= 0)
                    member.setColor(Color.BLACK);
                else
                    member.setColor(new Color(1, 1, 1, 1).lerp(Color.RED, 1f - (float) stm.get(entity).hp / (float) stm.get(entity).getModMaxHp(entity)));
            } else
                member.setColor(Color.WHITE);
        }
    }

    /**
     * Disables the user input of the battle screen.
     */
    public void disableUI() {
        battleInputProcessor.setDisabled(true);
        disableAttacks();
        endTurnBtn.setTouchable(Touchable.disabled);
    }

    /**
     * Enables the user input of the battle screen.
     */
    public void enableUI() {
        battleInputProcessor.setDisabled(false);
        enableAttacks();
        endTurnBtn.setTouchable(Touchable.enabled);
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
                team.get(e).teamNumber == rules.getCurrentTeamNumber() && !(status.has(selectedEntity) && status.get(selectedEntity).statusEffects.containsKey("Petrify"));
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
        for (EntitySystem e : engine.getSystems())
            engine.removeSystem(e);
    }

    public int getMoveHover() {
        return moveHover;
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
