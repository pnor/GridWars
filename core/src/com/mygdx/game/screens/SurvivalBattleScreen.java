package com.mygdx.game.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.AI.ComputerPlayer;
import com.mygdx.game.GridWars;
import com.mygdx.game.components.*;
import com.mygdx.game.misc.Tuple;
import com.mygdx.game.music.Song;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.ui.LerpColor;
import com.mygdx.game.ui.LerpColorManager;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.*;

/**
 * Alteration of {@link BattleScreen} made for the Survival mode. Displays the floor on the bottom bar, and changes the look of the UI. Also changes
 * how it links to other screens after a victory or loss.
 * @author Phillip O'Reggio
 */
public class SurvivalBattleScreen extends BattleScreen implements Screen {
    private int level;
    private int healthPowerUp;
    private int spPowerUp;
    private int powerPowerUp;
    private int speedPowerUp;
    //score keeping
    private int points;
    private int numberOfTurns;

    //game speed last set to
    private static byte gameSpeedSurvival = (byte) 1;

    //whether this is loaded from a save file
    private boolean loadedFromSave;

    public SurvivalBattleScreen(Team team, Team enemyTeam, ComputerPlayer.Difficulty difficulty, int floorLevel, int healthPowerUpNum, int spPowerUpNum,
                                int attackPowerUpAmount, int speedPowerUpAmount, int points, int turnCount, boolean loadedFromSave,
                                LerpColorManager survivalLerpColorManager, Song song, GridWars game) {
        super(new Array<Team>(new Team[]{team, enemyTeam}), floorLevel + 12, new Tuple[]{new Tuple(1, difficulty)}, survivalLerpColorManager, song, game);
        healthPowerUp = healthPowerUpNum;
        spPowerUp = spPowerUpNum;
        powerPowerUp = attackPowerUpAmount;
        speedPowerUp = speedPowerUpAmount;
        level = floorLevel;
        this.points = points;
        numberOfTurns = turnCount;
        this.loadedFromSave = loadedFromSave;
        lerpColorManager = survivalLerpColorManager;
    }

    public SurvivalBattleScreen(Team team, Team enemyTeam, Team objectTeam, ComputerPlayer.Difficulty difficulty, int floorLevel, int healthPowerUpNum,
                                int spPowerUpNum, int attackPowerUpAmount, int speedPowerUpAmount, int points, int turnCount,
                                boolean loadedFromSave, LerpColorManager survivalLerpColorManager, Song song, GridWars game) {
        super(new Array<Team>(new Team[]{team, enemyTeam, objectTeam}), floorLevel + 12, new Tuple[]{new Tuple(1, difficulty), new Tuple(2, ComputerPlayer.Difficulty.FIRST_ATTACK)},
                survivalLerpColorManager, song, game);
        healthPowerUp = healthPowerUpNum;
        spPowerUp = spPowerUpNum;
        powerPowerUp = attackPowerUpAmount;
        speedPowerUp = speedPowerUpAmount;
        level = floorLevel;
        computer.setIndexOfFirstAttackingTeams(2);
        this.points = points;
        numberOfTurns = turnCount;
        this.loadedFromSave = loadedFromSave;
        lerpColorManager = survivalLerpColorManager;
    }

    @Override
    public void show() {
        super.show();
        attackTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 28, 28)));
        //helpTable.setBackground(new NinePatchDrawable(atlas.createPatch("TableBackDark")));
        infoTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 28, 28)));
        statsTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 28, 28)));
        teamTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 28, 28)));
        gameSpeedTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 28, 28)));
        teamTable.add(new Label("Floor " + level, skin)).padLeft(25);
        GRID_WARS.setGameSpeed(gameSpeedSurvival);
        setGameSpeedLblText();
    }

    @Override
    public void showEndTurnDisplay() {
        showingEndTurnMessageTable = true;
        //show next turn message
        if (rules.getCurrentTeamNumber() == 0)
            endTurnMessageLbl.setText(rules.getCurrentTeam().getTeamName() + " turn!");
        else
            endTurnMessageLbl.setText("Enemy's Turn!");

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

    @Override
    public void doScreenTransitionAnimation() {
        if (rules.checkWinConditions() == teams.first()) { //victory
            if (level == 50) { //last stage transition
                Entity whiteCover = new Entity();
                Sprite brightness = (atlas.createSprite("LightTile"));
                whiteCover.add(new SpriteComponent(brightness));
                brightness.setColor(new Color(1, 1, 1, 0));
                whiteCover.add(new PositionComponent(new Vector2(0, 0), stage.getHeight(), stage.getWidth(), 0));
                whiteCover.add(new EventComponent(.005f, 0, true, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().cpy().add(0, 0, 0, .05f));
                }));
                engine.addEntity(whiteCover);
            } else { //all other transition
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
        } else { //loss
            Entity whiteCover = new Entity();
            Sprite brightness = (atlas.createSprite("LightTile"));
            whiteCover.add(new SpriteComponent(brightness));
            brightness.setColor(new Color(1, 1, 1, 0));
            whiteCover.add(new PositionComponent(new Vector2(0, 0), stage.getHeight(), stage.getWidth(), 0));
            whiteCover.add(new EventComponent(.005f, 0, true, true, (entity, engine) -> {
                sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().cpy().add(0, 0, 0, .05f));
            }));
            engine.addEntity(whiteCover);
        }
    }

    @Override
    public void goToNextScreen() {
        GRID_WARS.soundManager.unloadSounds();
        gameSpeedSurvival = GRID_WARS.getGameSpeed();
        GRID_WARS.setGameSpeed((byte) 1);
        numberOfTurns += rules.getTurnCount();
        points += calculatePoints();
        if (rules.checkWinConditions() == teams.first()) { // victory
            // Healing Player Team somewhat
            for (Entity e : teams.first().getEntities()) {
                //has status effects
                if (status.has(e) && status.get(e).getTotalStatusEffects() >= 1)
                    status.get(e).removeAll(e);
                //if alive, add one third of total health. Always heals at least 1 and at most 4.
                if (stm.get(e).alive)
                    stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + MathUtils.clamp(stm.get(e).maxHP / 3, 1, 4), 0, stm.get(e).maxHP);
                else {
                    stm.get(e).setAlive();
                    vm.get(e).resetVisuals();
                    stm.get(e).hp = 1;
                    stm.get(e).sp = 0;
                }
            }

            // Decide what screen to go to
            if (level < 50)
                //is not 50th floor:
                GRID_WARS.setScreen(new SurvivalTowerScreen(teams.first(), ++level, healthPowerUp, spPowerUp, powerPowerUp, speedPowerUp, points, numberOfTurns, loadedFromSave, GRID_WARS));
            else
                GRID_WARS.setScreen(new SurvivalResultsScreen(51, points, numberOfTurns,  loadedFromSave, teams.first(), GRID_WARS));
        } else { //loss
            SurvivalTowerScreen.clearSurvivalLerpColorManager();
            GRID_WARS.setScreen(new GameOverScreen(level, points, numberOfTurns, loadedFromSave, teams.get(0), GRID_WARS));
        }
    }

    @Override
    protected void quitScreen() {
        GRID_WARS.saveDataManager.makeFileUnloadable();
        GRID_WARS.saveDataManager.saveSavedData();
        super.quitScreen();
    }

    @Override
    protected void disposeLerpColorManager() {
        lerpColorManager.clear();
    }

    @Override
    protected void setWinConditionsMet() {
        super.setWinConditionsMet();
        // Invalidate Save File
        GRID_WARS.saveDataManager.makeFileUnloadable();
        GRID_WARS.saveDataManager.saveSavedData();
    }

    public int calculatePoints() {
        int points = 0;
        points += MathUtils.clamp((60 - rules.getTurnCount()) * 10, 0, 6000);
        for (Entity e : teams.first().getEntities()) {
            points += MathUtils.clamp(stm.get(e).hp, 0, 999) * 20;
        }
        // Modifier for fewer team members
        points += 200 * MathUtils.clamp(4 - teams.first().getEntities().size, 0, 4);
        return points;
    }
}
