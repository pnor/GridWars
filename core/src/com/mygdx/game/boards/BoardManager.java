package com.mygdx.game.boards;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

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
     * Makes a BoardManager with both types of board equal to null. {@code setBoards} should be called.
     */
    public BoardManager() { }

    /**
     * Boards must be of the same size.
     * @param b board that this will sync
     * @param cb codeBoard that this will sync
     */
    public BoardManager(Board b, CodeBoard cb) {
        if (b.getRowSize() * b.getColumnSize() != cb.getRows() * cb.getColumns())
            throw new ExceptionInInitializerError("Boards of unequal size");
        board = b;
        codeBoard = cb;
    }

    /**
     * Sets the {@code Board} and {@code CodeBoard}.
     * @param b Board
     * @param cb CodeBoard
     */
    public void setBoards(Board b, CodeBoard cb) {
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
     * Adds an entity's data to a board and codeBoard. Doesn't add if there is an entity
     * there already. Also scales added entity to board's size.
     * @param e Entity. Must have a {@code ActorComponent}
     * @param r row
     * @param c column
     * @return true if it added the entity
     */
    public boolean add(Entity e, int r, int c) {
        if (codeBoard.get(r, c) != null)
            //there's something there
            return false;

        codeBoard.add(e, new BoardPosition(r, c));
        board.add(am.get(e).actor, r, c);
        scaleEntity(e);
        return true;
    }

    /**
     * Removes an entity's data to a board and codeBoard.
     * @param e Entity. Must have a {@code ActorComponent} and {@code BoardComponent}
     * @return true if it removed an entity
     */
    public boolean remove(Entity e) {
        if (codeBoard.get(bm.get(e).pos.r, bm.get(e).pos.c) == null || (bm.get(e).pos.r == -1 && bm.get(e).pos.c == -1)) {
            //there's nothing there OR the entity's not on the board
            return false;
        }

        board.remove(am.get(e).actor, bm.get(e).pos.r, bm.get(e).pos.c);
        codeBoard.remove(bm.get(e).pos, true);
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

        codeBoard.remove(bp, true);
        board.remove(am.get(codeBoard.get(bp.r, bp.c)).actor, bp.r, bp.c);
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
     * Checks if the space at the indicated position already has an entity on it.
     * @param pos Position that is being checked
     * @return whether the entity at the position is not null
     */
    public boolean isOccupied(BoardPosition pos) {
        return !(codeBoard.get(pos.r, pos.c) == null);
    }

    /**
     * @param pos position being checked
     * @return whether the position is in the bounds of the board
     */
    public boolean containsPosition(BoardPosition pos) {
        return !(pos.r < 0 || pos.c < 0 || pos.r >= board.getRowSize() || pos.c >= board.getColumnSize());
    }

    /**
     * @return All entities on the {@link CodeBoard}
     */
    public Array<Entity> getAllEntities() {
        return codeBoard.getEntities();
    }

    /**
     * Scales an entity to the size of the board's tiles.
     * @param e Entity to be scaled
     */
    public void scaleEntity(Entity e) {
        if (am.has(e))
            if (am.get(e).actor.getParent() != null)
                am.get(e).actor.setScale(board.getScale());
    }

    /**
     * @return width of the first tile in the board.
     */
    public float getTileWidth() {
        return board.getTile(0, 0).getWidth();
    }

    /**
     * @return height of the first tile in the board.
     */
    public float getTileHeight() {
        return board.getTile(0, 0).getWidth();
    }

    public Board getBoard() {
        return board;
    }

    public CodeBoard getCodeBoard() {
        return codeBoard;
    }
}
