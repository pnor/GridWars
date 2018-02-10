package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.skin;

/**
 * Tutorial Screen showing the objective of the game.
 * @author Phillip O'Reggio
 */
public class TutorialScreenObjective extends MenuScreen {
    private Label titleLbl, winObjectiveTitleLbl, hotkeysTitleLbl;
    private Label
            normalWinLbl,
            zoneWinLbl,
            hotKeysLbl;
    private HoverButton nextBtn;

    public TutorialScreenObjective(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 40;
        param.color = Color.WHITE;
        titleLbl = new Label("Rules", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        param.size = 30;
        winObjectiveTitleLbl = new Label("Win Objectives", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        hotkeysTitleLbl = new Label("Hot Keys", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));

        normalWinLbl = new Label("To win, reduce the Health Points of all the pieces on the opposing team to 0.", skin);
        normalWinLbl.setWrap(true);
        zoneWinLbl = new Label("In Zone Rules, their will be spaces glowing in the opposing team's color. Placing a piece on the other team's " +
                "zone will win the match.", skin);
        zoneWinLbl.setWrap(true);

        hotKeysLbl = new Label(
                "H + [Number Key 1-4] : Display information about a selected piece's move.\n\n" +
                "A, D : Scroll through pieces on the board.\n\n" +
                "Shift (Left) : End the current turn.\n\n" +
                "=, - : Change game speed.", skin);
        nextBtn = new HoverButton("Next", skin, Color.WHITE, Color.DARK_GRAY);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == nextBtn) {
                        GRID_WARS.setScreen(new TutorialScreen(GRID_WARS));
                    }
                }
            }
        };

        background = BackgroundConstructor.makeMovingStripeBackground(Color.GRAY, Color.DARK_GRAY);

        nextBtn.addListener(listener);

        Table subTable = new Table();
        subTable.add(titleLbl).colspan(2).padBottom(40f).row();
        subTable.add(winObjectiveTitleLbl).padBottom(20f).row();
        subTable.add(normalWinLbl).size(600, 70).padBottom(5f).row();
        subTable.add(zoneWinLbl).size(600, 70).padBottom(5f).row();
        subTable.add(hotkeysTitleLbl).padBottom(20f).row();
        subTable.add(hotKeysLbl).width(600).padBottom(20f).row();
        subTable.add(nextBtn).padTop(40).size(180, 60);
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("spritesAndBackgrounds/TableBackground.png")), 33, 33, 33, 33);
        NinePatchDrawable tableBackDrawable = new NinePatchDrawable(tableBack);
        subTable.setBackground(tableBackDrawable);
        subTable.pack();
        table.add(subTable);

        fontGenerator.dispose();
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        //go back a screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
        }
    }
}
