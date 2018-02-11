package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
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
import com.mygdx.game.creators.*;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.move_related.StatusEffect;
import com.mygdx.game.move_related.Visuals;
import com.mygdx.game.rules_types.Rules;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.rules_types.ZoneRules;
import com.mygdx.game.systems.*;
import com.mygdx.game.ui.*;
import com.mygdx.game.music.Song;

import java.util.Iterator;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.*;

/**
 * Screen where the battles take place. Contains methods for handling the UI and other visual effects. Here, the player interacts with the game through
 * the buttons and sprites.
 * @author pnore_000
 */
public class BattleScreen implements Screen {

    protected final GridWars GRID_WARS;
    protected BattleInputProcessor battleInputProcessor;
    protected LerpColorManager lerpColorManager;

    //Background
    Background background;

    //General Info
    private final int TOTAL_ENTITIES_ON_TEAMS;

    //rules
    protected static Rules rules;
    private boolean gameHasEnded;
    private int winningTeamIndex;
    private float changeScreenTimer;

    //Entities
    protected Array<Team> teams = new Array<Team>();

    //Selection and Hover
    private Entity selectedEntity;
    private boolean checkedStats;
    /** Used for selection using keyboard */
    private byte hotkeyTeamsIndex;
    private final LerpColor SELECTION_COLOR = new LerpColor(Color.BLUE, Color.WHITE, .4f);

    //Attack Processing
    private Move currentMove;
    /**
     * value represents which move to show. If -1, means its showing no moves.
     */
    private int moveHover = -1;
    private boolean hoverChanged;
    public boolean attacksEnabled = true;

    //Computer Turn variables
    protected final ComputerPlayer computer;
    /**
     * x-coordinate of the vector is team index. y-coordinate is the depth level.
     * <p> Easy -> 0 </p>
     * <p> Normal -> 2 </p>
     * <p> Hard ->  6 </p>
     */
    private Vector2[] computerControlledTeamsIndex;
    private boolean playingComputerTurn;
    private float timeAfterMove;
    private float movementWaitTime;
    private float attackWaitTime;
    private int currentComputerControlledEntity;
    /**
     * Waiting to... <p>
     * 0 : move   1 : attack 2 : time after attack* 3 : waiting to end turn */
    private int turnPhase;

    //Ui Elements
    /**
     * Table with board tiles
     */
    protected Table boardTable;
    /**
     * Table that displays stat values and names
     */
    protected Table statsTable;
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
    protected Table attackTable;
    private Label attackTitleLabel;
    private HoverButton attackBtn1;
    private HoverButton attackBtn2;
    private HoverButton attackBtn3;
    private HoverButton attackBtn4;
    /**
     * Table that shows the info label
     */
    protected Table infoTable;
    private Label infoLbl;
    /**
     * Has data for the team. Has end turn button, and will have a icon of all the entities on a team
     */
    protected Table teamTable;
    private Image member1;
    private Image member2;
    private Image member3;
    private Image member4;
    private HoverButton endTurnBtn;
    /**
     * The box that says the next team's turn has started.
     */
    protected Table endTurnMessageTable;
    protected Label endTurnMessageLbl;
    protected Label turnCountLbl;
    protected boolean showingEndTurnMessageTable;
    protected float displayEndTurnMessageTime;

    /** Pop up message that says the effects of a move. */
    protected Dialog helpDialog;
    protected Table helpTable;
    private Label moveTitleLbl;
    /** displays range of an attack */
    private Table moveRangeTable;
    /** displays description of a move's effects */
    private Label moveDescriptionLbl;
    private HoverButton closeHelpMenuBtn;
    protected boolean showingHelpMenu;

    /**
     * Displays game speed
     */
    protected Table gameSpeedTable;
    protected Label gameSpeedLbl;

    /**
     * Creates a Battle Screen
     * @param selectedTeams teams selected from the team select screen
     * @param boardIndex what board is being played on
     * @param AIControlled which teams are being controlled by the AI (x: team number, y: difficulty)
     * @param colorManager Manger for {@link LerpColor}s in the screen. Typically, this parameter should be {@code new LerpColorManager()} unless
     *                     its survival mode which recycles it.
     * @param song Song that will play
     * @param game Instance of the game
     */
    public BattleScreen(Array<Team> selectedTeams, int boardIndex, Vector2[] AIControlled, LerpColorManager colorManager, Song song, GridWars game) {
        GRID_WARS = game;
        teams = selectedTeams;

        // initialize board and rule constructor first with lerpColorManager
        setUpLerpColorManager(colorManager);
        BoardAndRuleConstructor.initialize(lerpColorManager);

        if (BoardComponent.boards == null)
            BoardComponent.boards = new BoardManager();
        rules = BoardAndRuleConstructor.getBoardAndRules(boardIndex, this, teams, BoardComponent.boards);
        background = BackgroundConstructor.getBackground(boardIndex);

        //Initialize Others Constructors
        MoveConstructor.initialize(BoardComponent.boards.getBoard().getScale(), BoardComponent.boards, engine, stage, GRID_WARS);
        DamageAnimationConstructor.initialize(BoardComponent.boards.getBoard().getScale(), BoardComponent.boards, engine);

        //Updating AI with information about rules
        if (rules instanceof ZoneRules)
            computer = new ComputerPlayer(BoardComponent.boards, teams, ((ZoneRules) rules).getZones(), 1, 1, true, 0);
        else
            computer = new ComputerPlayer(BoardComponent.boards, teams, 1, 1, true, 0);
        computerControlledTeamsIndex = AIControlled;

        //get sum of all entities on teams in game + add lerpColors of teams to manager
        int sum = 0;
        for (int i = 0; i < teams.size; i++) {
            sum += teams.get(i).getEntities().size;
            if (teams.get(i).getTeamColor() instanceof LerpColor)
                lerpColorManager.registerLerpColor((LerpColor) teams.get(i).getTeamColor());
        }
        TOTAL_ENTITIES_ON_TEAMS = sum;

        //register lerpColors that will be used often
        lerpColorManager.registerLerpColor(SELECTION_COLOR);

        //Options preferences
        Preferences pref = Gdx.app.getPreferences("GridWars Options");
        Move.doesAnimations = pref.getBoolean("Move Animation");
        int AISpeed = pref.getInteger("AI Turn Speed");
        if (AISpeed == 0) { //slow
            movementWaitTime = 1f;
            attackWaitTime = 1.5f;
            displayEndTurnMessageTime = 1f;
        } else if (AISpeed == 1) { //normal
            movementWaitTime = .5f;
            attackWaitTime = 1f;
            displayEndTurnMessageTime = .5f;
        } else { //fast
            movementWaitTime = .1f;
            attackWaitTime = .3f;
            displayEndTurnMessageTime = .25f;
        }

        //play the music
        GRID_WARS.musicManager.setSong(song);
    }

    @Override
    public void show() {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        stage.clear();

        for (EntitySystem system : engine.getSystems()) {
            engine.removeSystem(system);
        }

        boardTable = new Table();
        statsTable = new Table();
        attackTable = new Table();
        infoTable = new Table();
        teamTable = new Table();
        endTurnMessageTable = new Table();
        helpTable = new Table();
        stage.addActor(boardTable);
        stage.addActor(statsTable);
        stage.addActor(attackTable);
        stage.addActor(infoTable);
        stage.addActor(teamTable);
        stage.addActor(endTurnMessageTable);
        stage.addActor(helpTable);
        battleInputProcessor = new BattleInputProcessor(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, battleInputProcessor));

        //Set up Engine
        engine.addSystem(new DrawingSystem(stage.getBatch()));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new DamageDeathSystem());

        //Add all things on board to engine
        for (Entity e : BoardComponent.boards.getAllEntities()) {
            if (stm.has(e) && !stm.get(e).alive) continue; //skip dead
            engine.addEntity(e);
        }

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
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("spritesAndBackgrounds/TableBackground.png")), 33, 33, 33, 33);
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
        statusLbl = new NewsTickerLabel(new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE), "Healthy", 16, .05f, .3f);
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
        statsTable.setSize(statsTable.getWidth() - 30f, statsTable.getHeight() - 30f);
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
                            if (mvm.get(selectedEntity).moveList.get(0).spCost() > stm.get(selectedEntity).sp) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(0).useAttack();
                            stm.get(selectedEntity).sp -= mvm.get(selectedEntity).moveList.get(0).spCost();
                            currentMove = mvm.get(selectedEntity).moveList.get(0);
                        } else if (actor == attackBtn2) {
                            if (mvm.get(selectedEntity).moveList.get(1).spCost() > stm.get(selectedEntity).sp) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(1).useAttack();
                            stm.get(selectedEntity).sp -= mvm.get(selectedEntity).moveList.get(1).spCost();
                            currentMove = mvm.get(selectedEntity).moveList.get(1);
                        } else if (actor == attackBtn3) {
                            if (mvm.get(selectedEntity).moveList.get(2).spCost() > stm.get(selectedEntity).sp) {
                                infoLbl.setText("Not enough SP!");
                                return;
                            }
                            mvm.get(selectedEntity).moveList.get(2).useAttack();
                            stm.get(selectedEntity).sp -= mvm.get(selectedEntity).moveList.get(2).spCost();
                            currentMove = mvm.get(selectedEntity).moveList.get(2);
                        } else if (actor == attackBtn4) {
                            if (mvm.get(selectedEntity).moveList.get(3).spCost() > stm.get(selectedEntity).sp) {
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
        attackTable.setSize(attackTable.getWidth() - 30f, attackTable.getHeight() - 30f);
        attackTable.setPosition(stage.getWidth() * .875f - (attackTable.getWidth() / 2), stage.getHeight() * .225f - (attackTable.getWidth() / 2));

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
        teamTable.setSize(700, 70);
        teamTable.setPosition(teamTable.getX() + teamTable.getOriginX() + 50, stage.getHeight() * .01f);

        //set up infoTable
        infoLbl = new GradualLabel(.001f, "---", skin);
        infoTable.add(infoLbl).height(25).center();
        infoTable.setBackground(tableBackground);
        infoTable.pack();
        infoTable.setPosition(teamTable.getX(), stage.getHeight() * .91f);
        infoTable.setSize(700, 60);

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

        //set up help menu
        helpDialog = new Dialog("Help", skin);
        moveTitleLbl = new Label("~_~_~_", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        moveDescriptionLbl = new Label("-----", skin);
        moveDescriptionLbl.setWrap(true);
        moveRangeTable = new Table();
        moveRangeTable.add(new Image(atlas.createSprite("wall")));
        closeHelpMenuBtn = new HoverButton("Close", skin, Color.WHITE, Color.RED);
        closeHelpMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    hideHelpMenu();
                }
            }
        });
        helpTable.add(moveTitleLbl).padBottom(20f).row();
        helpTable.add(moveRangeTable).padBottom(20f).row();
        helpTable.add(moveDescriptionLbl).width(450f).padBottom(20f).row();
        helpTable.add(closeHelpMenuBtn).size(120f, 40f).padBottom(10f);
        //add to dialog window
        helpDialog.add(helpTable).row();

        //place game speed indicator
        param.size = 16;
        gameSpeedLbl = new Label("???", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        setGameSpeedLblText();
        gameSpeedTable = new Table();
        gameSpeedTable.add(gameSpeedLbl);
        gameSpeedTable.setBackground(tableBackground);
        gameSpeedTable.pack();
        gameSpeedTable.setSize(gameSpeedLbl.getWidth() * 2.8f , gameSpeedLbl.getHeight() * 2.8f);
        gameSpeedTable.setPosition(stage.getWidth() - stage.getWidth() * .05f, stage.getHeight() * .005f);
        stage.addActor(gameSpeedTable);
        //Start first turn
        nextTurn();

        fontGenerator.dispose();
    }

    /**
     * Game loop of the Battle Screen. In order of operations : <p>
     * 1 : Syncs Visual board with BoardManager boards <p>
     * 2 : If the game hasnt ended and it is the player's turn, process Player Input (selecting entities and attacks <p>
     * 3 : If it is the computer's turn and the computer has gotten its actions for the turn, then it should play those actions out on screen <p>
     * 4 : Play any move animations <p>
     * 5 : If the game is still going, check and perform actions if any Hot Keys were pressed <p>
     * 6 : update game engine and screen and draw onto screen <p>
     * 7 : Remove dead entities and play their animations <p>
     * 8 : Check win conditions to see if game has ended <p>
     * 9 : Any debug actions <p>
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        syncBoards();
        if (!gameHasEnded && !playingComputerTurn)
            processPlayerInput();
        if (playingComputerTurn && !computer.getProcessing())
            updateComputerTurn(delta);
        playCurrentMoveAnimation(delta);
        if (!gameHasEnded) //Hot Keys should not work while game has ended
            checkHotKeys();
        updateAndDraw(delta);
        handleDeadEntities();
        checkWinConditions(delta);

        //region Debug
        /*
        //checking if things are working as intended
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {//Visuals
            System.out.println("Visuals.visualsArePlaying = " + Visuals.visualsArePlaying);
            System.out.println("Current Move : " + currentMove);
            if (currentMove != null) {
                System.out.println("Current Move Visuals : " + currentMove.getVisuals());
                System.out.println("- isPlaying : " + currentMove.getVisuals().getIsPlaying());
                System.out.println("- Timer : " + currentMove.getVisuals().getTimer());
                System.out.println("- EndTime : " + currentMove.getVisuals().getTimer().getEndTime());
            }
            //check entity on all teams
            System.out.println("Team 0 ~~~");
            boolean visualsPlaying = false;
            for (Entity e : teams.get(0).getEntities()) {
                visualsPlaying = vm.get(e).shuffleAnimation.getIsPlaying() || vm.get(e).deathAnimation.getIsPlaying() || vm.get(e).damageAnimation.getIsPlaying()
                        || vm.get(e).heavyDamageAnimation.getIsPlaying();
                System.out.println("Entity : " + nm.get(e).name + "(" + teams.get(0).getEntities().indexOf(e, true) + ") has visuals playing = " + visualsPlaying);
            }
            System.out.println("Team 1 ~~~");
            for (Entity e : teams.get(1).getEntities()) {
                visualsPlaying = vm.get(e).shuffleAnimation.getIsPlaying() || vm.get(e).deathAnimation.getIsPlaying() || vm.get(e).damageAnimation.getIsPlaying()
                        || vm.get(e).heavyDamageAnimation.getIsPlaying();
                System.out.println("Entity : " + nm.get(e).name + "(" + teams.get(1).getEntities().indexOf(e, true) + ") has visuals playing = " + visualsPlaying);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) { //Force Visuals to 0
            System.out.println("Visuals forced to 0.");
            Visuals.visualsArePlaying = 0;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) //Frames
            System.out.println("Frames per Second: " + Gdx.graphics.getFramesPerSecond());
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) { // Computer Control info
            String contents = "";
            for (Vector2 v : computerControlledTeamsIndex)
                contents = contents.concat(", " + v.toString());

            System.out.println("Computer Info: \nplayingComputerTurn = " + playingComputerTurn +
                    "\ncomputerControlledTeamsIndeces = " + contents +
                    "\ncurrentTeam = " + rules.getCurrentTeamNumber() +
                    "\nCurrent Computer Controlled Entity = " + currentComputerControlledEntity +
                    "\nComputer is always using first attack = " + computer.getUsingFirstAttack() +
                    "\nIndex of first attack = " + computer.getIndexOfFirstAttack());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) { // Turn Info
            System.out.println("Turn Info: \n" +
                    "Turn Count = " + rules.getTurnCount() +
                    "\nTeam Number = " + rules.getCurrentTeamNumber());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) { // Turn Info
            System.out.println("ShowingEndTurnMessage = " + showingEndTurnMessageTable);
        }
        /*
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) { // Turn Info
            if (selectedEntity != null) {
                System.out.println("Showing help menu!");
                showHelpMenu(mvm.get(selectedEntity).moveList.random());
                System.out.println("help menu visible = " + showingHelpMenu);

            } else {
                System.out.println("No selected entity = no move to show");
            }
        }
        */
        //endregion
    }

    //region Render Loop Methods

    /**
     * Syncs up the BoardComponent with the board displayed on the screen.
     */
    protected void syncBoards() {
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
    }

    /**
     * Processes the player input. If the player clicks an entity,
     * it will be selected, and its information displayed. If the player hovers their cursor on the attack buttons, it will
     * show the range, as long the buttons are enabled.
     */
    protected void processPlayerInput() {
        //update last ENTITY selected --- (Selecting an Entity)
        for (Entity e : BoardComponent.boards.getCodeBoard().getEntities()) {
            if (Visuals.visualsArePlaying == 0) {
                if (am.get(e).actor.getLastSelected()) {
                    changeSelectedEntity(e);
                    am.get(e).actor.setLastSelected(false);
                }
            }
        }

        //update last TILE selected --- (Moving Entity by clicking)
        if (selectedEntity != null) {
            for (Tile t : BoardComponent.boards.getBoard().getTiles()) {
                if (t.getLastSelected()) {
                    t.setLastSelected(false);
                    //remove highlighted tiles
                    removeMovementTiles();
                    //move Entity location
                    BoardComponent.boards.move(selectedEntity, new BoardPosition(t.getRow(), t.getColumn()));
                    state.get(selectedEntity).canMove = false;
                }
            }
        }

        //updating attack squares
        if (!mayAttack(selectedEntity) && attacksEnabled) {
            disableAttacks();
        } else if (mayAttack(selectedEntity) && !attacksEnabled && Visuals.visualsArePlaying == 0)
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

    /**
     * Updates the computer with the delta time.
     */
    protected void updateComputerTurn(float deltaTime) {
        processComputerTurn(deltaTime);
    }

    /**
     * Plays a Move's animation if an entity used a move. When a move is done playing, {@code currentMove} is set to null.
     */
    protected void playCurrentMoveAnimation(float deltaTime) {
        if (currentMove != null) {
            if (currentMove.getVisuals().getIsPlaying()) {
                currentMove.updateVisuals(deltaTime);
                currentMove.getVisuals().play();
            } else {
                currentMove.getVisuals().reset();
                if (!playingComputerTurn)
                    enableUI();
                if (selectedEntity != null)
                    updateStatsAndMoves();
                currentMove = null;
            }
        }
    }

    /**
     * Checks if any hot keys have been pressed.
     */
    protected void checkHotKeys() {
        //whenever
        if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) { //game speed
            GRID_WARS.setGameSpeed((byte) (GRID_WARS.getGameSpeed() + 1));
            setGameSpeedLblText();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) { //game speed
            GRID_WARS.setGameSpeed((byte) (GRID_WARS.getGameSpeed() - 1));
            setGameSpeedLblText();
        }
        // During player turn and no Visuals
        if (!playingComputerTurn && Visuals.visualsArePlaying == 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) { //SHIFT : Next turn hotkey
                removeAttackTiles();
                nextTurn();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) { //D : Scroll though forward
                hotkeyTeamsIndex = (byte) ((hotkeyTeamsIndex + 1) % TOTAL_ENTITIES_ON_TEAMS);
                changeSelectedEntity(getEntityFromIndex(true));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) { //A : Scroll though backward
                hotkeyTeamsIndex -= 1;
                if (hotkeyTeamsIndex < 0) hotkeyTeamsIndex = (byte) (TOTAL_ENTITIES_ON_TEAMS - 1);
                changeSelectedEntity(getEntityFromIndex(false));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.H)) { //help menu
                if (selectedEntity != null) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && mvm.get(selectedEntity).moveList.size > 0) {
                        showHelpMenu(mvm.get(selectedEntity).moveList.first());
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && mvm.get(selectedEntity).moveList.size > 1) {
                        showHelpMenu(mvm.get(selectedEntity).moveList.get(1));
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && mvm.get(selectedEntity).moveList.size > 2) {
                        showHelpMenu(mvm.get(selectedEntity).moveList.get(2));
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) && mvm.get(selectedEntity).moveList.size > 3) {
                        showHelpMenu(mvm.get(selectedEntity).moveList.get(3));
                    }
                }
            }
        }
    }

    protected void updateAndDraw(float deltaTime) {
        background.update(deltaTime);
        stage.act(deltaTime);
        lerpColorManager.update(deltaTime);
        engine.getSystem(DrawingSystem.class).drawBackground(background, deltaTime);
        stage.draw();
        engine.update(deltaTime);
    }

    /**
     * Remove dead entities from the board and engine once they have finished playing animations.
     */
    protected void handleDeadEntities() {
        for (Entity e : BoardComponent.boards.getCodeBoard().getEntities()) {
            if (stm.has(e) && !stm.get(e).alive && stm.get(e).readyToRemoveFromGame) {
                BoardComponent.boards.remove(e);
                engine.removeEntity(e);
                if (nm.has(e)) //TODO make death messages not overwrite whatever message is supposed to show. perhaps have a message queue thing
                    infoLbl.setText(nm.get(e).name + " has been defeated!");
            }
        }
    }

    /**
     * Checks the win condition of the game. If the game has ended, then it does a transition to the next screen.
     */
    protected void checkWinConditions(float deltaTime) {
        if (rules.checkWinConditions() != null && currentMove == null) {
            if (!gameHasEnded) {
                endTurnBtn.setDisabled(true);
                gameHasEnded = true;
            }

            //fade to black
            if (changeScreenTimer >= 2)
                doScreenTransitionAnimation();

            //go to results screen
            if (changeScreenTimer >= 3)
                goToNextScreen();

            if (Visuals.visualsArePlaying == 0)
                changeScreenTimer += deltaTime;
        }
    }
    //endregion

    //region selection related
    public void changeSelectedEntity(Entity e) {
        //Undoing effects of selecting previous entity---
        //removes previously highlighted tiles
        if (selectedEntity != null && stm.has(selectedEntity) && stm.get(selectedEntity).getModSpd(selectedEntity) > 0)
            removeMovementTiles();

        if (Visuals.visualsArePlaying == 0 && selectedEntity != null) //stop highlight
            shadeBasedOnState(selectedEntity); //TODO make a way to show multiple status effects well

        removeAttackTiles();
        //---
        selectedEntity = e; //selectedEntity changes to new entity from here on

        if (Visuals.visualsArePlaying == 0) { //If no visuals are playing, color entity
            am.get(selectedEntity).actor.shade(SELECTION_COLOR);
            if (mayAttack(selectedEntity) && !attacksEnabled)
                enableAttacks();
        }

        //check if has a speed > 0, and can move. Also if it is not on another team/has no team
        if (mayMove(selectedEntity)) {
            // newly highlights spaces
            showMovementTiles();
        }

        checkedStats = false;
    }

    /**
     * Gets an entity from all player and computer team's based on the value of hotkeyTeamsIndex.
     * @param indexMovementDirection True if hotkeyTeamsIndex is incrementing. False if decrementing
     * @return Entity from one of the teams contained in field teams.
     */
    public Entity getEntityFromIndex(boolean indexMovementDirection) {
        Team team = null;
        int sum = 0;
        for (int i = 0; i < teams.size; i++) { //getting team within index range
            if (hotkeyTeamsIndex < sum + teams.get(i).getEntities().size) {
                team = teams.get(i);
                break; // found team
            }
            sum += teams.get(i).getEntities().size;
        }
        Entity e = team.getEntities().get(hotkeyTeamsIndex - sum);
        if (stm.get(e).alive) //live entity, then return it
            return e;
        else { //if dead, keeping going until it finds a live one
            if (indexMovementDirection) {
                hotkeyTeamsIndex = (byte) ((hotkeyTeamsIndex + 1) % TOTAL_ENTITIES_ON_TEAMS);
                return getEntityFromIndex(indexMovementDirection);
            } else {
                hotkeyTeamsIndex -= 1;
                if (hotkeyTeamsIndex < 0) hotkeyTeamsIndex = (byte) (TOTAL_ENTITIES_ON_TEAMS - 1);
                return getEntityFromIndex(indexMovementDirection);
            }
        }
    }
    //endregion

    //region Computer things
    private void processComputerTurn(float delta) {
        //if game has ended stop
        if (gameHasEnded) {
            return;
        }

        //Getting the Turn. Null if dead.
        Entity currentEntity;
        Turn currentTurn =
                (currentComputerControlledEntity < computer.getDecidedTurns().size)? computer.getDecidedTurns().get(currentComputerControlledEntity) : null;
        if (!showingEndTurnMessageTable)
            timeAfterMove += delta;

        if ((currentTurn == null && currentComputerControlledEntity < computer.getDecidedTurns().size) || (currentTurn != null && !stm.get(currentTurn.entity).alive)) { //entity is dead/skip turn
            currentComputerControlledEntity++;
            turnPhase = 0;
            timeAfterMove = 0;
        }

        //Playing out the Turn
        if (timeAfterMove >= movementWaitTime && turnPhase == 0) { //Move
            try { //debug
                BoardComponent.boards.move(currentTurn.entity, currentTurn.pos);
            } catch (Exception e) {
                System.out.println("\n \n --------------------------------" +
                        "\n Exception! : " + e + "  in BattleScreen 'Playing out the Turn'" +
                        "\n currentTurn = " + currentTurn +
                        "\n currentEntity = " + currentComputerControlledEntity +
                        "\n board size = " + BoardComponent.boards.getBoard().getRowSize()
                );
                Gdx.app.exit();
            }
            BoardComponent.boards.move(currentTurn.entity, currentTurn.pos);
            turnPhase = 1;
        } else if (timeAfterMove >= attackWaitTime && turnPhase == 1) { //use attack
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
            if (timeAfterMove >= 1f && Visuals.visualsArePlaying == 0) { //wait to end turn and wait till visuals are done
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
    //endregion

    //region Turns
    /**
     * Ends the current turn and starts the next.
     */
    private void nextTurn() {
        disableUI();
        rules.nextTurn();
        showEndTurnDisplay();

        if (!gameHasEnded) {
            //find computer controlled team
            boolean processingAComputerControlledTeam = false;
            int controlledTeamIndex = -1;
            for (int i = 0; i < computerControlledTeamsIndex.length; i++) {
                if ((int) computerControlledTeamsIndex[i].x == rules.getCurrentTeamNumber()) {
                    processingAComputerControlledTeam = true;
                    controlledTeamIndex = i;
                    break;
                }
            }

            if (processingAComputerControlledTeam) {
                playingComputerTurn = true;
                computer.setTeamControlled((int) computerControlledTeamsIndex[controlledTeamIndex].x);
                Vector2 v = computerControlledTeamsIndex[controlledTeamIndex];
                if ((int) v.y == 0) { //first attack
                    computer.setGetFirstAttackAlways(true);
                } else if ((int) v.y == 1) { //easy
                    computer.setGetFirstAttackAlways(false);
                    computer.setDepthLevel(0);
                    computer.setForgetBestMoveChance(.5f);
                } else if ((int) v.y == 2) { //normal
                    computer.setGetFirstAttackAlways(false);
                    computer.setDepthLevel(3);
                    computer.setForgetBestMoveChance(.3f);
                } if ((int) v.y == 3) { //hard
                    computer.setGetFirstAttackAlways(false);
                    computer.setDepthLevel(5);
                    computer.setForgetBestMoveChance(.1f);
                }
                if (rules instanceof ZoneRules)
                    computer.updateComputerPlayer(new BoardState(BoardComponent.boards.getCodeBoard().getEntities(), ((ZoneRules) rules).getZones()));
                else
                    computer.updateComputerPlayer(new BoardState(BoardComponent.boards.getCodeBoard().getEntities(), null));

                new Thread(computer).start();
            } else
                playingComputerTurn = false;

            if (!playingComputerTurn)
                enableUI();
        }
    }

    /**
     * Shows the window that displays who's turn it is.
     */
    public void showEndTurnDisplay() {
        //show next turn message
        showingEndTurnMessageTable = true;
        endTurnMessageLbl.setText(rules.getCurrentTeam().getTeamName() + " turn!");
        turnCountLbl.setText("Turn " + rules.getTurnCount());
        turnCountLbl.setColor(new Color(1,1,1,1).lerp(Color.ORANGE, (float) rules.getTurnCount() / 100f));
        Color teamColor = rules.getCurrentTeam().getTeamColor();
        if (teamColor instanceof LerpColor)
            endTurnMessageTable.setColor(((LerpColor) teamColor).getMiddleColor());
        else
            endTurnMessageTable.setColor(rules.getCurrentTeam().getTeamColor());
        endTurnMessageTable.clearActions();
        SequenceAction sequence = new SequenceAction();
        sequence.addAction(Actions.fadeIn(.2f));
        sequence.addAction(Actions.delay(displayEndTurnMessageTime));
        sequence.addAction(Actions.fadeOut(.2f));
        sequence.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                showingEndTurnMessageTable = false;
                return false;
            }
        });
        endTurnMessageTable.addAction(sequence);

        //update entity appearance
        for (Entity e : rules.getCurrentTeam().getEntities()) {
            shadeBasedOnState(e);
        }
    }
    //endregion

    //region Shading of Entities
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
            am.get(e).actor.shade(new Color(.1f, .1f, .1f, 1));
        else if (!state.get(e).canMove || !state.get(e).canAttack)
            am.get(e).actor.shade(Color.GRAY);
        else if (rules.getCurrentTeamNumber() == team.get(e).teamNumber)
            am.get(e).actor.shade(rules.getCurrentTeam().getTeamColor());
            //status effects
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {
            Color color = status.get(e).getStatusEffects().get(status.get(e).getTotalStatusEffects() - 1).getColor();
            if (color instanceof LerpColor)
                lerpColorManager.registerLerpColor((LerpColor) color);
            am.get(e).actor.shade(color);
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
            return am.get(e).actor.getColor() == new Color(.1f, .1f, .1f, 1);
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0) {  //status effect
                return am.get(e).actor.getColor().equals(status.get(e).getStatusEffects().get(status.get(e).getTotalStatusEffects() - 1).getColor());
        } else if (team.get(e).teamNumber == rules.getCurrentTeamNumber()) //defualts
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
            return new Color(.1f, .1f, .1f, 1);
        else if (!state.get(e).canMove || !state.get(e).canAttack)
            return Color.GRAY;
            //status effects
        else if (status.has(e) && status.get(e).getTotalStatusEffects() > 0)
            return status.get(e).getStatusEffects().get(status.get(e).getTotalStatusEffects() - 1).getColor();
        else { //defaults
            if (team.get(e).teamNumber == rules.getCurrentTeamNumber())
                return rules.getCurrentTeam().getTeamColor();
            else
                return Color.WHITE;
        }
    }
    //endregion

    //region UI related
    /**
     * Shows the window that displays attack information
     * @param move that is having its information displayed
     */
    protected void showHelpMenu(Move move) {
        //show help menu pop up
        showingHelpMenu = true;
        //update display
        moveTitleLbl.setText(move.getName());
        //region Create the attack range map
        moveRangeTable.clear();
        //get the largest distance of the attack's affected squares to create a box
        Array<BoardPosition> moveRange = move.getRange();
        int largestXRange = 0;
        int largestYRange = 0;
        for (BoardPosition bp : moveRange) {
            if (Math.abs(bp.r) > largestXRange) {
                largestXRange = Math.abs(bp.r);
            }
            if (Math.abs(bp.c) > largestYRange) {
                largestYRange = Math.abs(bp.c);
            }
        }
        largestXRange = (largestXRange == 0) ? largestXRange + 1 : largestXRange;
        largestYRange = (largestYRange == 0) ? largestYRange + 1 : largestYRange;
        int boxRadius = Math.max(largestXRange, largestYRange); // size from edge to tile right before center
        int squareSideLength = boxRadius * 2 + 1; // size of the side lengths
        //fill in the square centered around target entity
        for (int i = -boxRadius; i <= boxRadius; i++) {
            for (int j = -boxRadius; j <= boxRadius; j++) {
                Image curImage;
                if (i == 0 && j == 0) {
                    curImage = new Image(atlas.createSprite("robot"));
                } else {
                    curImage = new Image(atlas.createSprite("LightTile"));
                    //checkerboard coloring
                    if (Math.abs(j) % 2 == 0) {
                        if (Math.abs(i) % 2 == 0) {
                            curImage.setColor(Color.LIGHT_GRAY);
                        } else {
                            curImage.setColor(Color.WHITE);
                        }
                    } else {
                        if (Math.abs(i) % 2 == 0) {
                            curImage.setColor(Color.WHITE);
                        } else {
                            curImage.setColor(Color.LIGHT_GRAY);
                        }
                    }
                }
                moveRangeTable.add(curImage).size(200f / squareSideLength, 200f / squareSideLength);
            }
            moveRangeTable.row();
        }
        //color attack tiles
        Array<Cell> tableCells = moveRangeTable.getCells();
        for (BoardPosition bp : moveRange) {
            //get middle
            int index = squareSideLength * squareSideLength / 2;
            //displace by move location
            index += bp.r * squareSideLength + bp.c;
            tableCells.get(index).getActor().setColor(Color.RED);
        }
        //endregion
        moveDescriptionLbl.setText(move.getAttackDescription());

        moveRangeTable.pack();
        //fade in animation
        helpDialog.show(stage);

        /*
        moveRangeTable.debug();
        */
    }

    /**
     * Hides the help menu from view
     */
    protected void hideHelpMenu() {
        showingHelpMenu = false;
        //fade out animation
        /*
        helpTable.clearActions();
        helpTable.addAction(Actions.fadeOut(.2f));
        */
        helpDialog.hide();
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
     * Updates the HUD display that shows the information for the stats and attacks of an entity.
     */
    public void updateStatsAndMoves() {
        if (stm.has(selectedEntity)) {
            if (!stm.get(selectedEntity).obscureStatInfo) {
                StatComponent stat = stm.get(selectedEntity);
                hpLbl.setText(stat.hp + " / " + stat.getModMaxHp(selectedEntity));
                spLbl.setText(stat.sp + " / " + stat.getModMaxSp(selectedEntity));
                atkLbl.setText("" + stat.getModAtk(selectedEntity));
                defLbl.setText("" + stat.getModDef(selectedEntity));
                spdLbl.setText("" + stat.getModSpd(selectedEntity));
            } else {
                hpLbl.setText("? / ?");
                spLbl.setText("? / ?");
                atkLbl.setText("?");
                defLbl.setText("?");
                spdLbl.setText("?");
            }
            if (team.has(selectedEntity)) {
                if (teams.get(team.get(selectedEntity).teamNumber).getTeamColor() instanceof LerpColor)
                    nameLbl.setColor(((LerpColor) teams.get(team.get(selectedEntity).teamNumber).getTeamColor()).getMiddleColor());
                else
                    nameLbl.setColor(teams.get(team.get(selectedEntity).teamNumber).getTeamColor());
            } else
                nameLbl.setColor(Color.WHITE);
            hpLbl.setColor(Color.WHITE);
            spLbl.setColor(Color.WHITE);
            atkLbl.setColor(Color.WHITE);
            defLbl.setColor(Color.WHITE);
            spdLbl.setColor(Color.WHITE);
            statusLbl.setColor(Color.GREEN);
            if (status.has(selectedEntity) && status.get(selectedEntity).getTotalStatusEffects() > 0) {
                statusLbl.setColor(Color.ORANGE);
                for (StatusEffect status : status.get(selectedEntity).getStatusEffects()) {
                    if (status.getStatChanges().maxHP > 1f && !hpLbl.getColor().equals(Color.RED) && !hpLbl.getColor().equals(Color.GREEN))
                        hpLbl.setColor(Color.GREEN);
                    else if (status.getStatChanges().maxHP < 1f && !hpLbl.getColor().equals(Color.RED))
                        hpLbl.setColor(Color.RED);

                    if (status.getStatChanges().maxSP > 1f && !spLbl.getColor().equals(Color.RED) && !spLbl.getColor().equals(Color.GREEN))
                        spLbl.setColor(Color.GREEN);
                    else if (status.getStatChanges().maxSP < 1f && !spLbl.getColor().equals(Color.RED))
                        spLbl.setColor(Color.RED);

                    if (status.getStatChanges().atk > 1f && !atkLbl.getColor().equals(Color.RED) && !atkLbl.getColor().equals(Color.GREEN))
                        atkLbl.setColor(Color.GREEN);
                    else if (status.getStatChanges().atk < 1f && !atkLbl.getColor().equals(Color.RED))
                        atkLbl.setColor(Color.RED);

                    if (status.getStatChanges().def > 1f && !defLbl.getColor().equals(Color.RED) && !defLbl.getColor().equals(Color.GREEN))
                        defLbl.setColor(Color.GREEN);
                    else if (status.getStatChanges().def < 1f && !defLbl.getColor().equals(Color.RED))
                        defLbl.setColor(Color.RED);

                    if (status.getStatChanges().spd > 1f && !spdLbl.getColor().equals(Color.RED) && !spdLbl.getColor().equals(Color.GREEN))
                        spdLbl.setColor(Color.GREEN);
                    else if (status.getStatChanges().spd < 1f && !spdLbl.getColor().equals(Color.RED))
                        spdLbl.setColor(Color.RED);
                }

                //status effect label
                statusLbl.reset();
                if (status.get(selectedEntity).getTotalStatusEffects() == 1) //only one status effect
                    statusLbl.setText(status.get(selectedEntity).getStatusEffects().first().getName());
                else { //more than one status effect
                    StringBuilder statusEffects = new StringBuilder();
                    Iterator iterator = status.get(selectedEntity).getStatusEffects().iterator();
                    do {
                        statusEffects.append(((StatusEffect) iterator.next()).getName());
                        if (iterator.hasNext()) //still more elements to go
                            statusEffects.append(", ");
                    } while (iterator.hasNext());

                    statusLbl.setText(statusEffects.toString());
                }
            } else if (status.has(selectedEntity))
                statusLbl.setText("Healthy");
            else
                statusLbl.setText("---");
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
            if (stm.has(entity)) { //Shade based on health
                if (stm.get(entity).hp <= 0)
                    member.setColor(Color.BLACK);
                else
                    member.setColor(new Color(1, 1, 1, 1).lerp(Color.RED, 1f - (float) stm.get(entity).hp / (float) stm.get(entity).getModMaxHp(entity)));
            } else
                member.setColor(Color.WHITE);
        }
    }

    public void doScreenTransitionAnimation() {
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

    /**
     * Enables attack buttons
     */
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
     * Sets the game speed label to reflect the game speed of the game.
     */
    public void setGameSpeedLblText() {
        if (GRID_WARS.getGameSpeed() == 0) {
            gameSpeedLbl.setText("x.5");
            gameSpeedLbl.setColor(Color.BLUE);
        } else if (GRID_WARS.getGameSpeed() == 1) {
            gameSpeedLbl.setText("x1");
            gameSpeedLbl.setColor(Color.WHITE);
        } else if (GRID_WARS.getGameSpeed() == 2) {
            gameSpeedLbl.setText("x1.5");
            gameSpeedLbl.setColor(Color.YELLOW);
        } else if (GRID_WARS.getGameSpeed() == 3) {
            gameSpeedLbl.setText("x2");
            gameSpeedLbl.setColor(Color.ORANGE);
        } else if (GRID_WARS.getGameSpeed() == 4) {
            gameSpeedLbl.setText("x3");
            gameSpeedLbl.setColor(Color.RED);
        }
    }
    //endregion

    //region Changing Tiles and movements square related things
    //Movement
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
                    || BoardComponent.boards.getBoard().getTile(next.r, next.c).isOccupied()
                    || BoardComponent.boards.getBoard().getTile(next.r, next.c).isInvisible())
                continue;

            if (!includeHorizontalSpaces && next.r == sourceBp.r)
                continue;

            //recursively call other tiles
            tiles.add(BoardComponent.boards.getBoard().getTile(next.r, next.c));
            getMovableSquaresSpread(sourceBp, next, spd - 1, tiles, (i + 2) % 4, sourceDirection, includeHorizontalSpaces);
        }

        return tiles;
    }

    //Attacks
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
    //endregion

    //region Checking whether entities can attack/move
    /**
     * Checks if an entity has the criteria to use attacks.
     * Criteria : not null, has {@code MovesetComponent}, has {@code StateComponent}, can attack,
     * has {@code TeamComponent}, the current team turn is the same as the entity's team
     * @param e Entity
     * @return true if it may attack. False otherwise
     */
    public boolean mayAttack(Entity e) {
        return e != null && mvm.has(e) && state.has(e) && state.get(e).canAttack && team.has(e) &&
                team.get(e).teamNumber == rules.getCurrentTeamNumber() && !(status.has(selectedEntity) && status.get(selectedEntity).contains("Petrify"));
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
    //endregion

    /**
     * Creates a {@link LerpColorManager} for the screen, and initializes the {@link StatusEffectComponent} with it.
     */
    protected void setUpLerpColorManager(LerpColorManager colorManager) {
        lerpColorManager = colorManager;
        StatusEffectComponent.setLerpColorManager(lerpColorManager);
    }

    /**
     * Sets the {@link LerpColorManager} for the screen to null, and clears the {@link StatusEffectComponent} and {@link BoardAndRuleConstructor}.
     */
    protected void disposeLerpColorManager() {
        lerpColorManager = null;
        StatusEffectComponent.setLerpColorManager(null);
        BoardAndRuleConstructor.clear();
    }

    /**
     * Disposes of resources used by {@link BattleScreen} to prepare for the next screen
     */
    public void goToNextScreen() {
        disposeLerpColorManager();
        MoveConstructor.clear();
        EntityConstructor.clear();
        DamageAnimationConstructor.clear();
        GRID_WARS.setGameSpeed((byte) 1);
        GRID_WARS.setScreen(new EndResultsScreen(teams, teams.indexOf(rules.checkWinConditions(), true), rules, GRID_WARS));
    }

    /**
     * Adds a entity meant to act as a visual particle effect (Ex. a sparkle or explosion). This method is not how
     * {@link Move}s and Death/Damage visuals act.
     * @param e Entity being added
     */
    public void addParticleEntity(Entity e) {
        engine.addEntity(e);
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

    //region getters
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
    //endregion
}
