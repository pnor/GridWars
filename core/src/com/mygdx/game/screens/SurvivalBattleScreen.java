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
import com.mygdx.game.GridWars;
import com.mygdx.game.components.EventComponent;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.components.SpriteComponent;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.ui.LerpColor;
import music.Song;

import static com.mygdx.game.ComponentMappers.sm;
import static com.mygdx.game.ComponentMappers.stm;
import static com.mygdx.game.GridWars.*;

/**
 * Alteration of {@link BattleScreen} made for the Survival mode. Displays the floor on the bottom bar, and changes the look of the UI. Also changes
 * how it links to other screens after highscores victory or loss.
 * @author Phillip O'Reggio
 */
public class SurvivalBattleScreen extends BattleScreen implements Screen {
    private int level;
    private int healthPowerUp;
    private int spPowerUp;
    //score keeping
    private int points;
    private int numberOfTurns;

    public SurvivalBattleScreen(Team team, Team enemyTeam, int difficulty, int floorLevel, int healthPowerUpNum, int spPowerUpNum, int points, int turnCount, Song song, GridWars game) {
        super(new Array<Team>(new Team[]{team, enemyTeam}), floorLevel + 12, new Vector2[]{new Vector2(1, difficulty)}, song, game);
        healthPowerUp = healthPowerUpNum;
        spPowerUp = spPowerUpNum;
        level = floorLevel;
        this.points = points;
        numberOfTurns = turnCount;
    }

    public SurvivalBattleScreen(Team team, Team enemyTeam, Team objectTeam, int difficulty, int floorLevel, int healthPowerUpNum, int spPowerUpNum, int points, int turnCount, Song song, GridWars game) {
        super(new Array<Team>(new Team[]{team, enemyTeam, objectTeam}), floorLevel + 12, new Vector2[]{new Vector2(1, difficulty), new Vector2(2, 0)}, song, game);
        healthPowerUp = healthPowerUpNum;
        spPowerUp = spPowerUpNum;
        level = floorLevel;
        computer.setIndexOfFirstAttackingTeams(2);
        this.points = points;
        numberOfTurns = turnCount;
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
        GRID_WARS.setGameSpeed((byte) 1);
        numberOfTurns += rules.getTurnCount();
        points += calculatePoints();
        if (rules.checkWinConditions() == teams.first()) { //victory
            if (level < 50)
                //is not 50th floor:
                GRID_WARS.setScreen(new SurvivalTowerScreen(teams.first(), ++level, healthPowerUp++, spPowerUp++, points, numberOfTurns, GRID_WARS));
            else
                GRID_WARS.setScreen(new SurvivalResultsScreen(51, points, numberOfTurns, teams.first(), GRID_WARS));
        } else { //loss
            GRID_WARS.setScreen(new GameOverScreen(level, points, numberOfTurns, teams.get(0), GRID_WARS));
        }
    }

    public int calculatePoints() {
        int points = 0;
        points += MathUtils.clamp((30 - rules.getTurnCount()) * 10, 0, 3000);
        for (Entity e : teams.first().getEntities()) {
            points += stm.get(e).hp * 20;
        }
        return points;
    }
}
