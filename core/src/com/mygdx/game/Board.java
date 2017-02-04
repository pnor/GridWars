package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
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

    /**
     * Creates a regular 5 by 5 board
     */
    public Board() {
        Tile d = new Tile(0, 0, true);
        Tile l = new Tile(0, 1, false);
        grid = new Array<Array<Tile>>();
        grid.add(new Array<Tile>(new Tile[]{d.copy(), l.copy(), d.copy(0, 2), l.copy(0, 3), d.copy(0, 4)}));
        grid.add(new Array<Tile>(new Tile[]{l.copy(1, 0), d.copy(1, 1), l.copy(1, 2), d.copy(1, 3), l.copy(1, 4)}));
        grid.add(new Array<Tile>(new Tile[]{d.copy(2, 0), l.copy(2, 1), d.copy(2, 2), l.copy(2, 3), d.copy(2, 4)}));
        grid.add(new Array<Tile>(new Tile[]{l.copy(3, 0), d.copy(3, 1), l.copy(3, 2), d.copy(3, 3), l.copy(3, 4)}));
        grid.add(new Array<Tile>(new Tile[]{d.copy(4, 0), l.copy(4, 1), d.copy(4, 2), l.copy(4, 3), d.copy(4, 4)}));
    }

    /**
     * Creates a 5 by 5 board of a single certain color
     *
     * @param c {@code Color}
     */
    public Board(Color c) {
        if (c == null)
            throw new NullPointerException("Color of board can't be null!");

        Tile d = new Tile(0, 0, true, c);
        Tile l = new Tile(0, 1, false, c);
        grid = new Array<Array<Tile>>();
        grid.add(new Array<Tile>(new Tile[]{d.copy(), l.copy(), d.copy(0, 2), l.copy(0, 3), d.copy(0, 4)}));
        grid.add(new Array<Tile>(new Tile[]{l.copy(1, 0), d.copy(1, 1), l.copy(1, 2), d.copy(1, 3), l.copy(1, 4)}));
        grid.add(new Array<Tile>(new Tile[]{d.copy(2, 0), l.copy(2, 1), d.copy(2, 2), l.copy(2, 3), d.copy(2, 4)}));
        grid.add(new Array<Tile>(new Tile[]{l.copy(3, 0), d.copy(3, 1), l.copy(3, 2), d.copy(3, 3), l.copy(3, 4)}));
        grid.add(new Array<Tile>(new Tile[]{d.copy(4, 0), l.copy(4, 1), d.copy(4, 2), l.copy(4, 3), d.copy(4, 4)}));
    }

    /**
     * Creates a 5 by 5 board of 2 colors
     *
     * @param darkColor color of dark tiles
     * @param lightColor color of light tiles
     */
    public Board(Color darkColor, Color lightColor) {
        if (darkColor == null || lightColor == null)
            throw new NullPointerException("Color of board can't be null!");

        Tile d = new Tile(0, 0, true, darkColor);
        Tile l = new Tile(0, 1, false, lightColor);
        grid = new Array<Array<Tile>>();
        grid.add(new Array<Tile>(new Tile[]{d.copy(), l.copy(), d.copy(0, 2), l.copy(0, 3), d.copy(0, 4)}));
        grid.add(new Array<Tile>(new Tile[]{l.copy(1, 0), d.copy(1, 1), l.copy(1, 2), d.copy(1, 3), l.copy(1, 4)}));
        grid.add(new Array<Tile>(new Tile[]{d.copy(2, 0), l.copy(2, 1), d.copy(2, 2), l.copy(2, 3), d.copy(2, 4)}));
        grid.add(new Array<Tile>(new Tile[]{l.copy(3, 0), d.copy(3, 1), l.copy(3, 2), d.copy(3, 3), l.copy(3, 4)}));
        grid.add(new Array<Tile>(new Tile[]{d.copy(4, 0), l.copy(4, 1), d.copy(4, 2), l.copy(4, 3), d.copy(4, 4)}));
    }


    /**
     * Creates a board of a pre-determined size
     *
     * @param r row size
     * @param c column size
     */
    public Board(int r, int c) {
        if (r <= 0 || c <= 0)
            throw new IndexOutOfBoundsException();

        rows = r;
        columns = c;
        boolean dark = true;
        Tile d = new Tile(0, 0, true);
        Tile l = new Tile(0, 1, false);
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
     */
    public Board(int r, int c, Color color) {
        if (r <= 0 || c <= 0)
            throw new IndexOutOfBoundsException();
        if (color == null)
            throw new NullPointerException("Color of board can't be null!");

        rows = r;
        columns = c;
        boolean dark = true;
        Tile d = new Tile(0, 0, true, color);
        Tile l = new Tile(0, 1, false, color);
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
     */
    public Board(int r, int c, Color darkColor, Color lightColor) {
        if (r <= 0 || c <= 0)
            throw new IndexOutOfBoundsException();
        if (darkColor == null || lightColor == null)
            throw new NullPointerException("Color of board can't be null!");

        rows = r;
        columns = c;
        boolean dark = true;
        Tile d = new Tile(0, 0, true, darkColor);
        Tile l = new Tile(0, 1, false, lightColor);
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

    public Array<Array<Tile>> getGrid() {
        return grid;
    }

    /**
     * Prints out a text version of the board where Xs are objects and Os are empty spaces.
     * {UNTESTED; MAY NOT WORK}
     *
     * @return the board in print
     */
    public String toString() {
        String output = "";
        for (int i = 0; i < grid.size; i++) {
            for (int j = 0; j < grid.get(i).size; j++) {
                if (!grid.get(i).get(j).swapActor(0, 1))
                    output += "O - ";
                else
                    output += grid.get(i).get(j) + "X - ";
            }
            output += "\n";
        }

        return output;
    }
}
