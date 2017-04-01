package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.boards.Board;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.boards.CodeBoard;

/**
 * Represents a location and orientation on the board.
 * @author pnore_000
 */
public class BoardComponent implements Component {
    public static BoardManager boards;
    /**
     * if r and c are -1, means its not on the board
     */
    public BoardPosition pos = new BoardPosition(-1, -1);

    public BoardComponent() {
        super();
    }

    public BoardComponent(Board b, CodeBoard cb) {
        boards = new BoardManager(b, cb);
    }

    public BoardComponent(BoardManager bm) {
        boards = bm;
    }

    public void update(BoardPosition bp) {
        pos.set(bp.r, bp.c);
    }

    public static void setBoardManager(BoardManager bm) {
        boards = bm;
    }
}
