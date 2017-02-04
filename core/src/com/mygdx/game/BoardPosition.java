package com.mygdx.game;

/**
 * @author Phillip O'Reggio
 */
public class BoardPosition {
    /**
     * rows
     */
    public int r;
    /**
     * columns
     */
    public int c;

    public BoardPosition(int r2, int c2) {
        r = r2;
        c = c2;
    }

    public String toString() {
        return "row: " + r + " col: " + c;
    }
}
