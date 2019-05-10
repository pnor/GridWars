package com.mygdx.game.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.screens.BattleScreen;
import com.mygdx.game.move_related.Visuals;

import static com.mygdx.game.ComponentMappers.mvm;

/**
 * Has methods for actions that would be done on the Battle board, such as scrolling to rotate.
 * @author Phillip O'Reggio
 */
public class BattleInputProcessor implements InputProcessor {

    private BattleScreen battleScreen;
    private boolean disabled;

    public BattleInputProcessor(BattleScreen screen) {
        battleScreen = screen;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.J)
            changeAttackDirection(false);
        else if (keycode == Input.Keys.L)
            changeAttackDirection(true);

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Middle Click -> Next Turn
        if (button == Buttons.MIDDLE && !battleScreen.getPlayingComputerTurn() &&
         Visuals.visualsArePlaying == 0 && !battleScreen.getWaitingToEndGame()) {
            battleScreen.startNextTurn();
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (amount > 0)
            changeAttackDirection(true);
        else if (amount < 0)
            changeAttackDirection(false);

        return false;
    }

    /**
     * Rotates the attack if the scroll wheel was moved or the keys J or L was pressed. 
     * @param direction
     */
    private void changeAttackDirection(boolean direction) {
        if (disabled)
            return;

        if (battleScreen.getMoveHover() > -1)
            battleScreen.removeAttackTiles();

        if (battleScreen.getSelectedEntity() != null) {
            if (direction) {
                if (mvm.has(battleScreen.getSelectedEntity())) {
                    for (Move move : mvm.get(battleScreen.getSelectedEntity()).moveList) {
                        Move.orientAttack(true, move);
                    }
                }
            } else {
                if (mvm.has(battleScreen.getSelectedEntity())) {
                    for (Move move : mvm.get(battleScreen.getSelectedEntity()).moveList) {
                        Move.orientAttack(false, move);
                    }
                }
            }
        }

        if (battleScreen.getMoveHover() > -1)
            battleScreen.showAttackTiles();
    }

    public void setDisabled(boolean b) {
        disabled = b;
    }

    public boolean getDisabled() {
        return disabled;
    }
}
