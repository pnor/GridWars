package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.skin;

/**
 * First screen of the tutorial. Teaches the player what all the windows on the battle screen mean and do.
 * @author Phillip O'Reggio
 */
public class TutorialScreen extends MenuScreen {
    private Label titleLbl;
    private Image tutorialImage;
    private Label
            pinkIcon, pinkDescription,
            orangeIcon, orangeDescription,
            redIcon, redDescription,
            greenIcon, greenDescription,
            cyanIcon, cyanDescription,
            purpleIcon, purpleDescription;
    private HoverButton nextBtn;

    public TutorialScreen(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 40;
        param.color = Color.WHITE;
        titleLbl = new Label("The Battle Screen", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        //tutorial image
        tutorialImage = new Image(new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal("spritesAndBackgrounds/TutorialScreen.png")))));
        //descriptions
        pinkIcon = new Label("*", skin);
        pinkIcon.setColor(Color.PINK);
        pinkDescription = new Label("This is a team member. Clicking on them will display it's stats and the spaces it can move to. Clicking" +
                " the movement spaces highlighted in blue will move the piece to that space. ", skin);
        pinkDescription.setWrap(true);
        redIcon = new Label("*", skin);
        redIcon.setColor(Color.RED);
        redDescription = new Label("This shows the details of a selected piece. You can see the values of each stat and any status effects it has." , skin);
        redDescription.setWrap(true);
        nextBtn = new HoverButton("Next", skin, Color.WHITE, Color.DARK_GRAY);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == nextBtn) {
                        GRID_WARS.setScreen(new ModeSelectScreen(GRID_WARS));
                    }
                }
            }
        };

        background = BackgroundConstructor.makeMovingStripeBackground(Color.GRAY, Color.DARK_GRAY);

        nextBtn.addListener(listener);

        Table infoTable = new Table(skin);
        infoTable.add(pinkIcon);
        infoTable.add(pinkDescription).size(300, 60).row();
        infoTable.add(redIcon);
        infoTable.add(redDescription).size(300, 60).row();

        table.add(titleLbl).colspan(2).padBottom(60f).row();
        table.add(tutorialImage).size(752, 700);
        table.add(infoTable).padBottom(40).row();
        table.add(nextBtn).size(120, 50);

        table.debug();
        infoTable.debug();

        fontGenerator.dispose();
    }
}
