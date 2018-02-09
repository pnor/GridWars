package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.atlas;
import static com.mygdx.game.GridWars.skin;

/**
 * First screen of the tutorial. Teaches the player what all the windows on the battle screen mean and do.
 * @author Phillip O'Reggio
 */
public class TutorialScreen extends MenuScreen {
    private Label titleLbl;
    private Image tutorialImage;
    private Label
            pinkDescription,
            orangeDescription,
            redDescription,
            greenDescription,
            cyanDescription,
            purpleDescription;
    private Image
            pinkIcon, orangeIcon, redIcon, greenIcon, cyanIcon, purpleIcon;
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
        pinkIcon = new Image(atlas.findRegion("star"));
        pinkIcon.setColor(Color.PINK);
        pinkDescription = new Label("This is a team member. Clicking on them will display it's stats and the spaces it can move to. Clicking" +
                " the movement spaces highlighted in blue will move the piece to that space. ", skin);
        pinkDescription.setWrap(true);

        redIcon = new Image(atlas.findRegion("star"));
        redIcon.setColor(Color.RED);
        redDescription = new Label("This shows the details of a selected piece. You can see the values of each stat and any status effects it has." , skin);
        redDescription.setWrap(true);

        greenIcon = new Image(atlas.findRegion("star"));
        greenIcon.setColor(Color.GREEN);
        greenDescription = new Label("This shows all of the attacks of a piece. Each piece can do 1 action each turn." , skin);
        greenDescription.setWrap(true);

        cyanIcon = new Image(atlas.findRegion("star"));
        cyanIcon.setColor(Color.CYAN);
        cyanDescription = new Label("This is the team bar. Click the \"End Turn\" button to end your turn. You can also see the status of your entire team" +
                " quickly here." , skin);
        cyanDescription.setWrap(true);

        orangeIcon = new Image(atlas.findRegion("star"));
        orangeIcon.setColor(Color.ORANGE);
        orangeDescription = new Label("This is the Infobar. It displays information about attacks used." , skin);
        orangeDescription.setWrap(true);

        purpleIcon = new Image(atlas.findRegion("star"));
        purpleIcon.setColor(Color.PURPLE);
        purpleDescription = new Label("This shows the game speed. Pressing the \"+\" or \"=\" key will alter the speed of the game." , skin);
        purpleDescription.setWrap(true);

        nextBtn = new HoverButton("Exit", skin, Color.WHITE, Color.DARK_GRAY);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == nextBtn) {
                        GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
                    }
                }
            }
        };

        background = BackgroundConstructor.makeMovingStripeBackground(Color.GRAY, Color.DARK_GRAY);

        nextBtn.addListener(listener);

        Table infoTable = new Table(skin);
        infoTable.add(pinkIcon).padRight(10);;
        infoTable.add(pinkDescription).size(350, 60).row();
        infoTable.add(redIcon).padRight(10);
        infoTable.add(redDescription).size(350, 80).row();
        infoTable.add(greenIcon).padRight(10);
        infoTable.add(greenDescription).size(350, 60).row();
        infoTable.add(cyanIcon).padRight(10);
        infoTable.add(cyanDescription).size(350, 60).row();
        infoTable.add(orangeIcon).padRight(10);
        infoTable.add(orangeDescription).size(350, 60).row();
        infoTable.add(purpleIcon).padRight(10);
        infoTable.add(purpleDescription).size(350, 60).row();
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("spritesAndBackgrounds/TableBackground.png")), 33, 33, 33, 33);
        NinePatchDrawable tableBackDrawable = new NinePatchDrawable(tableBack);
        infoTable.setBackground(tableBackDrawable);
        infoTable.pack();

        table.add(titleLbl).colspan(2).padBottom(60f).row();
        table.add(tutorialImage).padRight(10).size(450, 500);
        table.add(infoTable).row();
        table.add(nextBtn).padTop(40).colspan(2).size(180, 60);

        //table.debug();
        //infoTable.debug();

        fontGenerator.dispose();
    }
}
