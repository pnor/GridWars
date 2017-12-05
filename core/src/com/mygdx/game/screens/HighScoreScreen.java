package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.misc.HighScore;

import static com.mygdx.game.GridWars.atlas;

/**
 * @author Phillip O'Reggio
 */
public class HighScoreScreen extends MenuScreen implements Screen {

    public HighScoreScreen(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        Label lblTitle;
        Table scoreTable = new Table();

        //prepopulate
        prepopulateHighScores();

        //get high scores
        Json json = new Json();
        FileHandle scoreFile = Gdx.files.local("GridWarsHighScores.json");
        String jsonScores = scoreFile.readString();
        Array<HighScore> highScores = json.fromJson(Array.class, jsonScores);

        //title
        param.size = 50;
        lblTitle = new Label("High Scores", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));

        //put in each score into table
        param.size = 16;
        BitmapFont smallFont = fontGenerator.generateFont(param);
        param.size = 20;
        BitmapFont bigFont = fontGenerator.generateFont(param);
        scoreTable.add().colspan(7).row();
        for (int i = 0; i < highScores.size; i++) {
            HighScore curScore = highScores.get(i);
            scoreTable.add(new Label(curScore.getTeamName(), new Label.LabelStyle(bigFont, Color.YELLOW)));
            scoreTable.add(new Label("" + curScore.getScore(), new Label.LabelStyle(smallFont, Color.WHITE)));
            scoreTable.add(new Label("" + curScore.getTurns(), new Label.LabelStyle(smallFont, Color.WHITE)));
            //add image
            SpriteDrawable[] spriteDrawables = curScore.getSprites();
            for (int j = 0; j < spriteDrawables.length; j++) {
                if (spriteDrawables.length < j) //add normally
                    scoreTable.add(new Image(spriteDrawables[j]));
                else //no more entities; place defaults
                    scoreTable.add(new Image(new SpriteDrawable(atlas.createSprite("cube"))));
            }

        }

        table.add(lblTitle).padBottom(20).row();
        table.add(scoreTable);

        background = BackgroundConstructor.makeSurvivalBackChecker();
        fontGenerator.dispose();
    }

    public void prepopulateHighScores() {
        Array<HighScore> highScores = new Array<>();
        for (int i = 0; i < 10; i++) {
            highScores.add(new HighScore("---", 0, 0, new SpriteDrawable(atlas.createSprite("robot")), new SpriteDrawable(atlas.createSprite("robot"))));
        }
        FileHandle scoreFile = Gdx.files.local("GridWarsHighScores.json");
        Json json = new Json();
        scoreFile.writeString(json.prettyPrint(highScores), false);
    }
}
