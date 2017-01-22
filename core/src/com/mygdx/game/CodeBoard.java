package com.mygdx.game;

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
     * @param r rows
     * @param c column
     */
    public void add(Entity e, int r, int c) {
        if (r < 0 || c < 0 || r >= rows || c >= columns) {
            System.out.println("r = " + r + ", c = " + c);
            throw new IndexOutOfBoundsException();
        }

        grid.get(r).set(c, e);
        bm.get(e).update(r, c);
    }

    /**
     * Removes an {@code Entity} from the board.
     * @param r rows
     * @param c column
     * @return removed Entity
     */
    public Entity remove(int r, int c) {
        if (r < 0 || c < 0 || r >= rows || c >= columns) {
            System.out.println("r = " + r + ", c = " + c);
            throw new IndexOutOfBoundsException();
        }

        Entity e = grid.get(r).get(c);
        if (bm.has(e))
            bm.get(e).update(-1, -1);

        Entity temp = grid.get(r).get(c);
        grid.get(r).set(c, null);
        return temp;
    }

    /**
     * Moves an entity from one place to another on the board. Entity must have {@code BoardComponent}
     * @param r old row
     * @param c old column
     * @param newR new row location
     * @param newC new column location
     */
    public void move(int r, int c, int newR, int newC) {
        if ((r < 0 || c < 0 || r >= rows || c >= columns) || (newR < 0 || newC < 0 || newR >= rows || newC >= columns)) {
            System.out.println("r = " + r + ", c = " + c);
            throw new IndexOutOfBoundsException();
        }
        add(remove(r, c), newR, newC);
        bm.get(grid.get(r).get(c)).update(newR, newC);

    }

    /**
     * Moves an entity from one place to another on the board. Entity must have {@code BoardComponent}
     * @param e Entity
     * @param r new row
     * @param c new column
     */
    public void move(Entity e, int r, int c) {
        if (r < 0 || c < 0 || r >= rows || c >= columns) {
            System.out.println("r = " + r + ", c = " + c);
            throw new IndexOutOfBoundsException();
        }

        add(remove(bm.get(e).r, bm.get(e).c), r, c);
        bm.get(e).update(r, c);
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
     * @return Entity at the given index
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
