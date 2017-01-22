package com.mygdx.game;

import com.badlogic.ashley.core.Entity;

import static com.mygdx.game.ComponentMappers.am;
import static com.mygdx.game.ComponentMappers.bm;

/**
 * Has methods in charge of syncing the {@code Board} and {@code CodeBoard} when moving, adding, etc.
 * When using the {@code Board} and {@code CodeBoard} together, this class should be used. Entities in
 * the {@code CodeBoard} should have a {@code ActorComponent} and {@code BoardComponent}.
 * @author pnore_000
 */
public class BoardManager {

    private Board board;
    private CodeBoard codeBoard;

    /**
     * Boards must be of the same size.
     * @param b board that this will sync
     * @param cb codeBoard that this will sync
     */
    public BoardManager(Board b, CodeBoard cb) {
        if (b.getRowSize() * b.getColumnSize() != cb.getRows() * cb.getColumns())
            throw new IndexOutOfBoundsException("Boards of unequal size");
        board = b;
        codeBoard = cb;
    }

    /**
     * Adds an entity's data to a board and codeBoard. Doesn't add if there is an entity
     * there already
     * @param e Entity. Must have a {@code ActorComponent}
     * @param r row to add at
     * @param c columns to add at
     * @return true if it added the entity
     */
    public boolean add(Entity e, int r, int c) {
        if (codeBoard.get(r, c) != null)
            //there's something there
            return false;

        codeBoard.add(e, r, c);
        board.add(am.get(e).actor, r, c);
        return true;
    }

    /**
     * Removes an entity's data to a board and codeBoard.
     * @param e Entity. Must have a {@code ActorComponent} and {@code BoardComponent}
     * @return true if it removed an entity
     */
    public boolean remove(Entity e) {
        if (codeBoard.get(bm.get(e).r, bm.get(e).c) != null || (bm.get(e).r == -1 && bm.get(e).c == -1))
            //there's nothing there OR the entity's not on the board
            return false;

        codeBoard.remove(bm.get(e).r, bm.get(e).c);
        board.remove(am.get(e).actor, bm.get(e).r, bm.get(e).c);
        return true;
    }

    /**
     * Removes an entity's data to a board and codeBoard at a specific index
     * @param r row
     * @param c column
     * @return true if an entity was removed
     */
    public boolean remove(int r, int c) {
        if (codeBoard.get(r, c) != null)
            //there's nothing there
            return false;

        board.remove(am.get(codeBoard.get(r, c)).actor, r, c);
        codeBoard.remove(r, c);
        return true;
    }

    /**
     * Moves an entity's data to another place on the board and codeBoard.
     * Entity must have a {@code ActorComponent} and {@code BoardComponent}.
     * @param r old row
     * @param c old column
     * @param newR new row
     * @param newC new column
     * @return true if an entity was moved
     */
    public boolean move(int r, int c, int newR, int newC) {
        if (codeBoard.get(r, c) == null || codeBoard.get(newR, newC) != null)
            //there's nothing to move OR there's something at the destination
            return false;

        board.move(am.get(codeBoard.get(r, c)).actor, r, c, newR, newC);
        codeBoard.move(r, c, newR, newC);
        return true;
    }

    /**
     * Moves an entity's data to another place on the board and codeBoard.
     * @param e Entity, must have a {@code ActorComponent} and {@code BoardComponent}.
     * @param r new row
     * @param c new column
     * @return true if an entity was moved
     */
    public boolean move(Entity e, int r, int c) {
        if (codeBoard.get(bm.get(e).r, bm.get(e).c) == null || codeBoard.get(r, c) != null) {
            //there's nothing to move || there's something at the destination
            return false;
        }

        board.move(am.get(codeBoard.get(bm.get(e).r, bm.get(e).c)).actor, bm.get(e).r, bm.get(e).c, r, c);
        codeBoard.move(e, r, c);
        return true;
    }

    public Board getBoard() {
        return board;
    }

    public CodeBoard getCodeBoard() {
        return codeBoard;
    }
}
