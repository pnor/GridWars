package com.mygdx.game.boards;

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
     * there already. Also scales added entity to board's size.
     * @param e Entity. Must have a {@code ActorComponent}
     * @param bp position to add at
     * @return true if it added the entity
     */
    public boolean add(Entity e, BoardPosition bp) {
        if (codeBoard.get(bp.r, bp.c) != null)
            //there's something there
            return false;

        codeBoard.add(e, bp);
        board.add(am.get(e).actor, bp.r, bp.c);
        scaleEntity(e);
        return true;
    }

    /**
     * Removes an entity's data to a board and codeBoard.
     * @param e Entity. Must have a {@code ActorComponent} and {@code BoardComponent}
     * @return true if it removed an entity
     */
    public boolean remove(Entity e) {
        if (codeBoard.get(bm.get(e).pos.r, bm.get(e).pos.c) != null || (bm.get(e).pos.r == -1 && bm.get(e).pos.c == -1))
            //there's nothing there OR the entity's not on the board
            return false;

        codeBoard.remove(bm.get(e).pos);
        board.remove(am.get(e).actor, bm.get(e).pos.r, bm.get(e).pos.c);
        return true;
    }

    /**
     * Removes an entity's data to a board and codeBoard at a specific index
     * @param bp board position
     * @return true if an entity was removed
     */
    public boolean remove(BoardPosition bp) {
        if (codeBoard.get(bp.r, bp.c) != null)
            //there's nothing there
            return false;

        board.remove(am.get(codeBoard.get(bp.r, bp.c)).actor, bp.r, bp.c);
        codeBoard.remove(bp);
        return true;
    }

    /**
     * Moves an entity's data to another place on the board and codeBoard.
     * Entity must have a {@code ActorComponent} and {@code BoardComponent}.
     * @param bp old position
     * @param newBp new position
     * @return true if an entity was moved
     */
    public boolean move(BoardPosition bp, BoardPosition newBp) {
        if (codeBoard.get(bp.r, bp.c) == null || codeBoard.get(newBp.r, newBp.c) != null)
            //there's nothing to move OR there's something at the destination
            return false;

        board.move(am.get(codeBoard.get(bp.r, bp.c)).actor, bp.r, bp.c, newBp.r, newBp.c);
        codeBoard.move(bp, newBp);
        return true;
    }

    /**
     * Moves an entity's data to another place on the board and codeBoard.
     * @param e Entity, must have a {@code ActorComponent} and {@code BoardComponent}.
     * @param bp new position
     * @return true if an entity was moved
     */
    public boolean move(Entity e, BoardPosition bp) {
        if (codeBoard.get(bm.get(e).pos.r, bm.get(e).pos.c) == null || codeBoard.get(bp.r, bp.c) != null) {
            //there's nothing to move || there's something at the destination
            return false;
        }

        board.move(am.get(codeBoard.get(bm.get(e).pos.r, bm.get(e).pos.c)).actor, bm.get(e).pos.r, bm.get(e).pos.c, bp.r, bp.c);
        codeBoard.move(e, bp);
        return true;
    }

    /**
     * Scales an entity to the size of the board's tiles.
     * @param e Entity to be scaled
     */
    private void scaleEntity(Entity e) {
        if (am.has(e))
            if (am.get(e).actor.getParent() != null)
                am.get(e).actor.setScale(am.get(e).actor.getParent().getWidth() / 100);
    }

    public Board getBoard() {
        return board;
    }

    public CodeBoard getCodeBoard() {
        return codeBoard;
    }
}
