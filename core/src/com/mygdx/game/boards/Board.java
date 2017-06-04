package com.mygdx.game.boards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.actors.UIActor;

/**
 * Represents a board of actors where battles happen on.
 * @author pnore_000
 */
public class Board {

    private Array<Array<Tile>> grid;
    private int rows = 5,
                columns = 5;
    private float scaleFactor = 1;

    /**
     * Creates a board of a pre-determined size
     *
     * @param r row size
     * @param c column size
     * @param tileSize size of the tiles
     */
    public Board(int r, int c, float tileSize) {
        if (r <= 0 || c <= 0)
            throw new IndexOutOfBoundsException();
        scaleFactor = tileSize / 100;
        rows = r;
        columns = c;
        boolean dark = true;
        Tile d = new Tile(0, 0, true, tileSize);
        Tile l = new Tile(0, 1, false, tileSize);
        grid = new Array<Array<Tile>>();

        for (int i = 0; i < r; i++) {
            grid.add(new Array<Tile>());
            if ((r % 2) == 0)
                dark = !dark;
            for (int j = 0; j < c; j++) {
                if (dark)
                    grid.get(i).add(d.copy(i, j));
                else
                    grid.get(i).add(l.copy(i, j));

                dark = !dark;
            }
        }
    }

    /**
     * Creates a board of a set size and single color
     *
     * @param r     row size
     * @param c     column size
     * @param color color of board
     * @param tileSize size of the tiles
     */
    public Board(int r, int c, Color color, float tileSize) {
        if (r <= 0 || c <= 0)
            throw new IndexOutOfBoundsException();
        if (color == null)
            throw new NullPointerException("Color of board can't be null!");

        scaleFactor = tileSize / 100;
        rows = r;
        columns = c;
        boolean dark = true;
        Tile d = new Tile(0, 0, true, color, tileSize);
        Tile l = new Tile(0, 1, false, color, tileSize);
        grid = new Array<Array<Tile>>();

        for (int i = 0; i < r; i++) {
            grid.add(new Array<Tile>());
            if ((r % 2) == 0)
                dark = !dark;
            for (int j = 0; j < c; j++) {
                if (dark)
                    grid.get(i).add(d.copy(i, j));
                else
                    grid.get(i).add(l.copy(i, j));

                dark = !dark;
            }
        }
    }

    /**
     * Creates a board of a set size and of 2 different colors
     *
     * @param r  row size
     * @param c  column size
     * @param darkColor color of dark tiles
     * @param lightColor color of light tiles
     * @param tileSize size of the tiles
     */
    public Board(int r, int c, Color darkColor, Color lightColor, float tileSize) {
        if (r <= 0 || c <= 0)
            throw new IndexOutOfBoundsException();
        if (darkColor == null || lightColor == null)
            throw new NullPointerException("Color of board can't be null!");

        scaleFactor = tileSize / 100;
        rows = r;
        columns = c;
        boolean dark = true;
        Tile d = new Tile(0, 0, true, darkColor, tileSize);
        Tile l = new Tile(0, 1, false, lightColor, tileSize);
        grid = new Array<Array<Tile>>();

        for (int i = 0; i < r; i++) {
            grid.add(new Array<Tile>());
            if ((r % 2) == 0)
                dark = !dark;
            for (int j = 0; j < c; j++) {
                if (dark)
                    grid.get(i).add(d.copy(i, j));
                else
                    grid.get(i).add(l.copy(i, j));

                dark = !dark;
            }
        }
    }



    /**
     * Gets the Tile at the indexes
     * @param r row index
     * @param c column index
     */
    public Tile getTile(int r, int c) {
        return grid.get(r).get(c);
    }

    /**
     * @return Array of all Tiles in the board
     */
    public Array<Tile> getTiles() {
        Array<Tile> tiles = new Array<Tile>();
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < columns; j++)
                    tiles.add(getTile(i, j));
        return tiles;
    }

    /**
     * Adds an actor to the board. Does this by adding adding the {@code Actor} to the specified Group.
     * @param a {@code Actor}
     * @param r row index
     * @param c column index
     */
    public void add(Actor a, int r, int c) {
        if (r < 0 || c < 0 || r >= rows || c >= columns) {
            System.out.println("r = " + r + ", c = " + c);
            throw new IndexOutOfBoundsException();
        }

        grid.get(r).get(c).addActor(a);

        if (a instanceof UIActor)
            ((UIActor) a).center();
    }

    /**
     * Removes an actor from the board. Does this by removing an {@code Actor} from the specified Group.
     * @param a {@code Actor}
     * @param r row index
     * @param c column index
     */
    public void remove(Actor a, int r, int c) {
        if (r < 0 || c < 0 || r >= rows || c >= columns) {
            System.out.println("r = " + r + ", c = " + c);
            throw new IndexOutOfBoundsException();
        }

        grid.get(r).get(c).removeActor(a);
    }

    /**
     * Moves an Actor from one place to another on the board.
     * @param a {@code Actor}
     * @param r previous row index
     * @param c previous column index
     * @param newR new row index
     * @param newC new column index
     */
    public void move(Actor a, int r, int c, int newR, int newC) {
        if (r < 0 || c < 0 || r >= rows || c >= columns) {
            System.out.println("r = " + r + ", c = " + c);
            throw new IndexOutOfBoundsException();
        }

        remove(a, r, c);
        add(a, newR, newC);

        if (a instanceof UIActor)
            ((UIActor) a).center();
    }


    public int getRowSize() {
        return rows;
    }

    public int getColumnSize() {
        return columns;
    }

    public float getScale() {
        return scaleFactor;
    }

    public Array<Array<Tile>> getGrid() {
        return grid;
    }

    /**
     * Prints out a the board. XXX represent nothing
     *
     * @return the board in text
     */
    public String toString() {
        StringBuilder output = new StringBuilder();
        Tile curTile;
        for (int i = 0; i < grid.size; i++) {
            for (int j = 0; j < grid.get(i).size; j++) {
                curTile = grid.get(i).get(j);
                if (curTile.isOccupied())
                    output.append(curTile.getChildren().get(1));
                else
                    output.append("XXX");
                output.append("(" + curTile.getRow() + ", " + curTile.getColumn() + ") - ");
            }
            output.append("\n");
        }

        return output.toString();
    }
}
