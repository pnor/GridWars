package com.mygdx.game.screens_ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.GridWars;
import com.mygdx.game.screens_ui.HoverButton;

import static com.mygdx.game.GridWars.skin;

/**
 * @author Phillip O'Reggio
 */
public class OptionsScreen extends MenuScreen implements Screen {

    private Preferences preferences;

    public OptionsScreen(GridWars gridWars) {
        super(gridWars);
        preferences = Gdx.app.getPreferences("Options");
        //put in option variables if this is the first time
        if (!preferences.contains("Move Animation")) {
            preferences.putBoolean("Move Animation", true);
            preferences.putInteger("AI Turn Speed", 1);
            preferences.flush();
        }
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
            btnDontDoAnimation.setChecked(true);
        else if (preferences.getInteger("AI Turn Speed") == 2)
            btnFastAI.setChecked(true);


        HorizontalGroup confirmationBox = new HorizontalGroup();
        TextButton btnBack = new HoverButton("Back", skin, Color.WHITE, Color.RED);
        TextButton btnOK = new HoverButton("OK", skin, Color.WHITE, Color.BLUE);
        confirmationBox.addActor(btnBack);
        confirmationBox.addActor(btnOK);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnBack) {
                        GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
                    } else if (actor == btnOK) {
                        if (animationGroup.getChecked() == btnDoAnimation) {
                            preferences.putBoolean("Move Animation", true);
                        } else
                            preferences.putBoolean("Move Animation", false);

                        if (AIGroup.getChecked() == btnSlowAI) {
                            preferences.putInteger("AI Turn Speed", 0);
                        } else if (AIGroup.getChecked() == btnNormalAI)
                            preferences.putInteger("AI Turn Speed", 1);
                        else if (AIGroup.getChecked() == btnFastAI)
                            preferences.putInteger("AI Turn Speed", 2);

                        preferences.flush();
                        GRID_WARS.setScreen(new TitleScreen(GRID_WARS));

                    }
                }
            }
        };
        btnOK.addListener(listener);
        btnBack.addListener(listener);

        table.add(lblAnimationInfo).padBottom(20).row();
        HorizontalGroup animBtnGroup = new HorizontalGroup();
        animBtnGroup.addActor(btnDoAnimation);
        animBtnGroup.addActor(btnDontDoAnimation);
        table.add(animBtnGroup).padBottom(20).row();
        table.add(lblAISpeedInfo).padBottom(20).row();
        HorizontalGroup AIBtnGroup = new HorizontalGroup();
        AIBtnGroup.addActor(btnSlowAI);
        AIBtnGroup.addActor(btnNormalAI);
        AIBtnGroup.addActor(btnFastAI);
        table.add(AIBtnGroup).padBottom(20).row();
        table.add(confirmationBox);
    }
}
