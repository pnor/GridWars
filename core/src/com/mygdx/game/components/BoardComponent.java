package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;

/**
 * Represents a location and orientation on the board.
 * @author pnore_000
 */
public class BoardComponent implements Component {
    public static BoardManager boards;
    /*
    Used for giving each Entity a unique ID
     */
    private static int currentIDNumberAssigned;
    /**
     * Unique ID index to tell entities apart from one another.
     */
    public final int BOARD_ENTITY_ID;
    /**
     * if r and c are -1, means its not on the board
     */
    public BoardPosition pos = new BoardPosition(-1, -1);

    public BoardComponent() {
        BOARD_ENTITY_ID = currentIDNumberAssigned++;
    }


    /**
     * Updates the position. Is called from BoardManager, so should not be called directly from an entity.
     * @param bp new BoardPosition
     */
    public void update(BoardPosition bp) {
        pos.set(bp.r, bp.c);
    }
}
