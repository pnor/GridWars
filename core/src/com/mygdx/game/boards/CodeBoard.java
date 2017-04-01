package com.mygdx.game.boards;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

import static com.mygdx.game.ComponentMappers.bm;

/**
 * Board of entities where battles happen on.
 * @author pnore_000
 */
public class CodeBoard {

    private Array<Array<Entity>> grid;
    private int rows = 5,
                columns = 5;

    /**
     * Creates a {@code CodeBoard} with 5 rows and columns.
     */
    public CodeBoard() {
        grid = new Array<Array<Entity>>();
        for (int i = 0; i < 5; i++) {
            grid.add(new Array<Entity>(5));
            for (int j = 0; j < 5; j++)
                grid.get(i).add(null);
        }
    }

    /**
     * Creates a {@code CodeBoard} with a defined amount of rows and columns
     * @param r row amount
     * @param c column amount
     */
    public CodeBoard(int r, int c) {
        if (r <= 0 || c <= 0)
            throw new IndexOutOfBoundsException();
        rows = r;
        columns = c;

        grid = new Array<Array<Entity>>();
        for (int i = 0; i < r; i++) {
            grid.add(new Array<Entity>(c));
            for (int j = 0; j < c; j++)
                grid.get(i).add(null);
        }
    }

    /**
     * Adds an {@code Entity} at a place on the board
     * @param e entity. Must have {@code BoardComponent}
     * @param bp position
     */
    public void add(Entity e, BoardPosition bp) {
        if (bp.r < 0 || bp.c < 0 || bp.r >= rows || bp.c >= columns) {
            System.out.println("r = " + bp.r + ", c = " + bp.c);
            throw new IndexOutOfBoundsException();
        }

        grid.get(bp.r).set(bp.c, e);
        bm.get(e).update(bp);
    }

    /**
     * Removes an {@code Entity} from the board.
     * @param bp position
     * @param defaultPositionValue whether it should set the {@code BoardPosition} of the removed Entity to -1, -1
     * @return removed Entity
     */
    public Entity remove(BoardPosition bp, boolean defaultPositionValue) {
        if (bp.r < 0 || bp.c < 0 || bp.r >= rows || bp.c >= columns) {
            System.out.println("r = " + bp.r + ", c = " + bp.c);
            throw new IndexOutOfBoundsException();
        }

        Entity temp = grid.get(bp.r).get(bp.c);
        Entity e = grid.get(bp.r).get(bp.c);
        grid.get(bp.r).set(bp.c, null);
        if (defaultPositionValue && bm.has(e))
            bm.get(e).update(new BoardPosition(-1, -1));

        return temp;
    }

    /**
     * Moves an entity from one place to another on the board. Entity must have {@code BoardComponent}
     * @param bp old position
     * @param newBp new location
     */
    public void move(BoardPosition bp, BoardPosition newBp) {
        if ((bp.r < 0 || bp.c < 0 || bp.r >= rows || bp.c >= columns) || (newBp.r < 0 || newBp.c < 0 || newBp.r >= rows || newBp.c >= columns)) {
            System.out.println("r = " + bp.r + ", c = " + bp.c);
            throw new IndexOutOfBoundsException();
        }
        add(remove(bp, false), newBp);
        bm.get(grid.get(bp.r).get(bp.c)).update(newBp);

    }

    /**
     * Moves an entity from one place to another on the board. Entity must have {@code BoardComponent}
     * @param e Entity
     * @param bp new location
     */
    public void move(Entity e, BoardPosition bp) {
        if (bp.r < 0 || bp.c < 0 || bp.r >= rows || bp.c >= columns) {
            System.out.println("r = " + bp.r + ", c = " + bp.c);
            throw new IndexOutOfBoundsException();
        }

        add(remove(bm.get(e).pos, false), bp);
        bm.get(e).update(bp);
    }

    /**
     * @return All entities on the board
     */
    public Array<Entity> getEntities() {
        Array<Entity> entities = new Array<Entity>();

        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getColumns(); j++)
                if (get(i, j) != null)
                    entities.add(get(i, j));

        return entities;
    }

    /**
     * @param r row
     * @param c column
     * @return Entity at the given index. Can be null.
     */
    public Entity get(int r, int c) {
        return grid.get(r).get(c);
    }

    public int getRows() {
        //return rows;
        return grid.size;
    }

    public int getColumns() {
        //return columns;
        return grid.get(0).size;
    }

    public Array<Array<Entity>> getGrid() {
        return grid;
    }
}
