package com.mygdx.game.screens_ui.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.rules_types.Team;

import static com.mygdx.game.GridWars.atlas;

/**
 * Alteration of {@link BattleScreen} made for the Survival mode. Displays the floor on the bottom bar, and changes the look of the UI. Also changes
 * how it links to other screens after a victory or loss.
 * @author Phillip O'Reggio
 */
public class SurvivalBattleScreen extends BattleScreen implements Screen {
    private int level;
    private int healthPowerUp;
    private int spPowerUp;

    public SurvivalBattleScreen(Team team, Team enemyTeam, int difficulty, int floorLevel, int healthPowerUpNum, int spPowerUpNum, GridWars game) {
        super(new Array<Team>(new Team[]{team, enemyTeam}), floorLevel + 12, new Vector2[]{new Vector2(1, difficulty)}, game);
        healthPowerUp = healthPowerUpNum;
        spPowerUp = spPowerUpNum;
        level = floorLevel;
    }

    @Override
    public void show() {
        super.show();
        attackTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 33, 33)));
        boardTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 33, 33)));
        //helpTable.setBackground(new NinePatchDrawable(atlas.createPatch("TableBackDark")));
        infoTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 33, 33)));
        statsTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 33, 33)));
        teamTable.setBackground(new NinePatchDrawable(new NinePatch(atlas.findRegion("TableBackDark"), 33, 33, 33, 33)));
    }
}
