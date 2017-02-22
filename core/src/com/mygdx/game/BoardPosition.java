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

    /**
     * Returns a modified copy of this object.
     * @param rNum amount to add to row
     * @param cNum amount to add to column
     * @return new {@code BoardPosition} with amounts added to row and column
     */
    public BoardPosition add(int rNum, int cNum) {
        return new BoardPosition(r + rNum, c + cNum);
    }

    /**
     * Sets value of this object
     * @param newR new r
     * @param newC new c
     */
    public void set(int newR, int newC) {
        r = newR;
        c = newC;
    }

    public String toString() {
        return "row: " + r + " col: " + c;
    }
}
