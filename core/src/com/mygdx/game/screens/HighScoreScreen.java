package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameUtil;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.highscores.HighScore;

import static com.mygdx.game.GridWars.atlas;

/**
 * Screen that displays the top 5 highscores.
 * @author Phillip O'Reggio
 */
public class HighScoreScreen extends MenuScreen implements Screen {

    public HighScoreScreen(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        Label lblTitle;
        Table scoreTable = new Table();

        //get high scores
        Array<HighScore> highScores = GRID_WARS.highScoreManager.getHighScores();

        //title
        param.size = 50;
        lblTitle = new Label("High Scores", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));

        //different font sizes
        param.size = 16;
        BitmapFont smallFont = fontGenerator.generateFont(param);
        param.size = 20;
        BitmapFont bigFont = fontGenerator.generateFont(param);
        //constant spacing values
        final float HORIZ_SPACING = 150f;
        final float HORIZ_SPACING_BETWEEN_MEMBERS = 9f;
        final float VERT_SPACING = 40f;

        //set table structure
        scoreTable.add().colspan(8).padBottom(20).row();
        scoreTable.add().padRight(HORIZ_SPACING);
        scoreTable.add().padRight(HORIZ_SPACING);
        scoreTable.add().padRight(HORIZ_SPACING);
        scoreTable.add().padRight(HORIZ_SPACING_BETWEEN_MEMBERS);
        scoreTable.add().padRight(HORIZ_SPACING_BETWEEN_MEMBERS);
        scoreTable.add().padRight(HORIZ_SPACING_BETWEEN_MEMBERS);
        scoreTable.add().padRight(HORIZ_SPACING_BETWEEN_MEMBERS);
        scoreTable.add().row();

        //add header
        scoreTable.add(new Label("Name", new Label.LabelStyle(bigFont, Color.WHITE)));
        scoreTable.add(new Label("Score", new Label.LabelStyle(bigFont, Color.WHITE)));
        scoreTable.add(new Label("Turns", new Label.LabelStyle(bigFont, Color.WHITE)));
        scoreTable.add(new Label("Members", new Label.LabelStyle(bigFont, Color.WHITE))).colspan(4);
        scoreTable.add(new Label("Floor", new Label.LabelStyle(bigFont, Color.WHITE)));
        //add entries
        for (int i = 0; i < highScores.size; i++) {
            scoreTable.row().padBottom(VERT_SPACING);
            HighScore curScore = highScores.get(i);
            scoreTable.add(new Label(curScore.getTeamName(), new Label.LabelStyle(bigFont, Color.YELLOW)));
            scoreTable.add(new Label("" + curScore.getScore(), new Label.LabelStyle(smallFont, Color.WHITE)));
            scoreTable.add(new Label("" + curScore.getTurns(), new Label.LabelStyle(smallFont, Color.WHITE)));
            //add images
            int[] spriteDrawables = curScore.getTeamSprites();
            for (int j = 0; j < 4; j++) {
                if (j < spriteDrawables.length) //add normally
                    scoreTable.add(new Image(new SpriteDrawable(GameUtil.getSpriteFromID(spriteDrawables[j]))));
                else //no more entities; place defaults
                    scoreTable.add(new Image(new SpriteDrawable(atlas.createSprite("cube"))));
            }
            if (curScore.getLastFloor() > 50)
                scoreTable.add(new Label("50+", new Label.LabelStyle(smallFont, Color.CYAN)));
            else
                scoreTable.add(new Label("" + curScore.getLastFloor(), new Label.LabelStyle(smallFont, Color.WHITE)));
        }

        table.add(lblTitle).padBottom(20).row();
        table.add(scoreTable);

        //background = BackgroundConstructor.makeSurvivalBackChecker();
        background = BackgroundConstructor.makeMovingStripeBackground(new Color(0.1f, 0.1f, 0.1f, 1), Color.DARK_GRAY);
        fontGenerator.dispose();
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        //go back high scores screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new SurvivalModeOptions(GRID_WARS));
        }
    }


}
