package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.skin;

/**
 * Screen that allows the player to set certain Options for the game.
 * @author Phillip O'Reggio
 */
public class OptionsScreen extends MenuScreen implements Screen {

    private Preferences preferences;

    public OptionsScreen(GridWars gridWars) {
        super(gridWars);
        preferences = Gdx.app.getPreferences("GridWars Options");
    }

    @Override
    public void show() {
        super.show();

        Label lblAnimationInfo = new Label("Move Animations", skin);
        ButtonGroup<TextButton> animationGroup;
        TextButton btnDoAnimation = new TextButton("Show", skin, "toggle");
        TextButton btnDontDoAnimation = new TextButton("Don't Show", skin, "toggle");
        animationGroup = new ButtonGroup<>(btnDoAnimation, btnDontDoAnimation);
        animationGroup.setMaxCheckCount(1);
        if (preferences.getBoolean("Move Animation"))
            btnDoAnimation.setChecked(true);
        else
            btnDontDoAnimation.setChecked(true);


        Label lblAISpeedInfo = new Label("Computer Player Turn Speed", skin);
        ButtonGroup<TextButton> AIGroup;
        TextButton btnSlowAI = new TextButton("Slow", skin, "toggle");
        TextButton btnNormalAI = new TextButton("Normal", skin, "toggle");
        TextButton btnFastAI = new TextButton("Fast", skin, "toggle");
        AIGroup = new ButtonGroup<>(btnSlowAI, btnNormalAI, btnFastAI);
        AIGroup.setMaxCheckCount(1);
        if (preferences.getInteger("AI Turn Speed") == 0)
            btnSlowAI.setChecked(true);
        else if (preferences.getInteger("AI Turn Speed") == 1)
            btnNormalAI.setChecked(true);
        else if (preferences.getInteger("AI Turn Speed") == 2)
            btnFastAI.setChecked(true);

        Label lblBackgroundInfo = new Label("Background Animations", skin);
        ButtonGroup<TextButton> backgroundGroup;
        TextButton btnAnimateBackground = new TextButton("Animate", skin, "toggle");
        TextButton btnDontAnimateBackground = new TextButton("Static", skin, "toggle");
        backgroundGroup = new ButtonGroup<>(btnAnimateBackground, btnDontAnimateBackground);
        backgroundGroup.setMaxCheckCount(1);
        if (preferences.getBoolean("Animate Background"))
            btnAnimateBackground.setChecked(true);
        else
            btnDontAnimateBackground.setChecked(true);

        Label lblMusicInfo = new Label("Volume", skin);
        Slider volumeSlider = new Slider(0, 1, .01f, false, skin);
        volumeSlider.setValue(preferences.getFloat("Music Volume"));
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GRID_WARS.musicManager.setMusicVolume(volumeSlider.getPercent());
            }
        });

        Table confirmationBox = new Table();
        TextButton btnBack = new HoverButton("Back", skin, Color.WHITE, Color.RED);
        TextButton btnOK = new HoverButton("OK", skin, Color.WHITE, Color.GREEN);
        confirmationBox.add(btnBack).size(80, 50).padRight(30);
        confirmationBox.add(btnOK).size(80, 50);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnBack) {
                        GRID_WARS.musicManager.setMusicVolume(preferences.getFloat("Music Volume"));
                        GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
                    } else if (actor == btnOK) {
                        if (animationGroup.getChecked() == btnDoAnimation)
                            preferences.putBoolean("Move Animation", true);
                        else
                            preferences.putBoolean("Move Animation", false);

                        if (AIGroup.getChecked() == btnSlowAI) {
                            preferences.putInteger("AI Turn Speed", 0);
                        } else if (AIGroup.getChecked() == btnNormalAI)
                            preferences.putInteger("AI Turn Speed", 1);
                        else if (AIGroup.getChecked() == btnFastAI)
                            preferences.putInteger("AI Turn Speed", 2);

                        if (backgroundGroup.getChecked() == btnAnimateBackground) {
                            preferences.putBoolean("Animate Background", true);
                            Background.setAnimateBackground(true);
                        } else {
                            preferences.putBoolean("Animate Background", false);
                            Background.setAnimateBackground(false);
                        }

                        preferences.putFloat("Music Volume", volumeSlider.getPercent());

                        preferences.flush();
                        GRID_WARS.setScreen(new TitleScreen(GRID_WARS));

                    }
                }
            }
        };
        btnOK.addListener(listener);
        btnBack.addListener(listener);

        table.add();
        table.add().row();
        table.add(lblAnimationInfo).colspan(2).padBottom(20).row();
        Table animBtnGroup = new Table();
        animBtnGroup.add(btnDoAnimation).size(90, 50);
        animBtnGroup.add(btnDontDoAnimation).size(90, 50);
        table.add(animBtnGroup).colspan(2).padBottom(20).row();
        table.add(lblAISpeedInfo).colspan(2).padBottom(20).row();
        Table AIBtnGroup = new Table();
        AIBtnGroup.add(btnSlowAI).size(80, 50);
        AIBtnGroup.add(btnNormalAI).size(80, 50);
        AIBtnGroup.add(btnFastAI).size(80, 50);
        table.add(AIBtnGroup).colspan(2).padBottom(20).row();
        table.add(lblBackgroundInfo).colspan(2).padBottom(20).row();
        Table animBackgroundGroup = new Table();
        animBackgroundGroup.add(btnAnimateBackground).size(90, 50);
        animBackgroundGroup.add(btnDontAnimateBackground).size(90, 50);
        table.add(animBackgroundGroup).colspan(2).padBottom(20).row();
        Table musicGroup = new Table();
        musicGroup.add(lblMusicInfo).row();
        musicGroup.add(volumeSlider).row();
        table.add(musicGroup).colspan(2).padBottom(30).row();
        table.add(btnBack).size(90, 50);
        table.add(btnOK).size(90, 50);

        background = BackgroundConstructor.makeNewTitle();
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        //go back highscores screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
        }
    }
}
