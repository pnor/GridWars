package com.mygdx.game.screens;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.GridWars;
import com.mygdx.game.ui.Background;
import com.mygdx.game.systems.DrawingSystem;

import static com.mygdx.game.GridWars.engine;
import static com.mygdx.game.GridWars.skin;
import static com.mygdx.game.GridWars.stage;

/**
 * Abstract subclass that all the menu classes derive from. provides the main table for UI elements and the background
 * @author Phillip O'Reggio
 */
public abstract class MenuScreen implements Screen{
    protected final GridWars GRID_WARS;
    protected Table table;
    protected Background background;

    public MenuScreen(GridWars game) {
        GRID_WARS = game;
    }

    @Override
    public void show() {
        stage.clear();
        for (EntitySystem system : engine.getSystems()) {
            engine.removeSystem(system);
        }

        table = new Table();
        table.setSize(stage.getWidth(), stage.getHeight());
        table.setFillParent(true);
        table.setSkin(skin);
        engine.addSystem(new DrawingSystem(GRID_WARS.getBatch()));
        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    public void render(float delta) {
        if (background != null) {
            background.update(delta);
            stage.act();
            engine.getSystem(DrawingSystem.class).drawBackground(background, delta);
            stage.draw();
        } else {
            stage.act();
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    public void hide() {
    }

    @Override
    public void dispose() {

    }

    public GridWars getGame() {
        return GRID_WARS;
    }

    public void setBackgound(Background back) {
        background = back;
    }
}
