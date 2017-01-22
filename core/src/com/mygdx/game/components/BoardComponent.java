package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.Board;
import com.mygdx.game.BoardManager;
import com.mygdx.game.CodeBoard;

import java.awt.*;

/**
 * Represents a location on the board.
 * @author pnore_000
 */
public class BoardComponent implements Component {
    public static BoardManager boards;
    /**
     * if r and c are -1, means its not on the board
     */
    public int r = -1;
    public int c = -1;

    public BoardComponent() {
        super();
    }

    public BoardComponent(Board b, CodeBoard cb) {
        boards = new BoardManager(b, cb);
    }

    public BoardComponent(BoardManager bm) {
        boards = bm;
    }

    public void update(int r2, int c2) {
        r = r2;
        c = c2;
    }

    public static void setBoardManager(BoardManager bm) {
        boards = bm;
    }
}
