package com.mygdx.game.screens_ui;

import com.badlogic.gdx.InputProcessor;
import com.mygdx.game.move_related.Move;

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
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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
        if (disabled)
            return false;

        if (battleScreen.getMoveHover() > -1)
            battleScreen.removeAttackTiles();

        if (battleScreen.getSelectedEntity() != null) {
            if (amount == 1) {
                if (mvm.has(battleScreen.getSelectedEntity())) {
                    for (Move move : mvm.get(battleScreen.getSelectedEntity()).moveList) {
                        Move.orientAttack(true, move.getRange());
                        move.getVisuals().setTargetPositions(move.getRange());
                    }
                }
            } else if (amount == -1) {
                if (mvm.has(battleScreen.getSelectedEntity())) {
                    for (Move move : mvm.get(battleScreen.getSelectedEntity()).moveList) {
                        Move.orientAttack(false, move.getRange());
                        move.getVisuals().setTargetPositions(move.getRange());
                    }
                }
            }
        }

        if (battleScreen.getMoveHover() > -1)
            battleScreen.showAttackTiles();

        return false;
    }

    public void setDisabled(boolean b) {
        disabled = b;
    }

    public boolean getDisabled() {
        return disabled;
    }
}
