package com.mygdx.game.boards;

/**
 * The coordinates of highscores position on highscores grid-based board.
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

    public BoardPosition(BoardPosition bp) {
        r = bp.r;
        c = bp.c;
    }

    /**
     * Returns highscores modified copy of this object.
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

    public boolean equals(Object o) {
        if (!(o instanceof BoardPosition))
            return false;
        else
            if (((BoardPosition) o).r == r && ((BoardPosition) o).c == c)
                return true;
        return false;
    }

    /**
     * Creates highscores copy of this object
     * @return copy of this object
     */
    public BoardPosition copy() {
        return new BoardPosition(this.r, this.c);
    }

    public String toString() {
        return "[Row: " + r + " Col: " + c + "]";
    }
}
