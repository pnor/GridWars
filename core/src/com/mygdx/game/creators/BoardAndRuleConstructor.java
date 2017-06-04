package com.mygdx.game.creators;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.boards.Board;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.boards.CodeBoard;
import com.mygdx.game.rules_types.Battle2PRules;
import com.mygdx.game.rules_types.Rules;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.rules_types.ZoneRules;
import com.mygdx.game.screens_ui.screens.BattleScreen;

/**
 * Class containing static methods for initializing specific {@code Board}s and {@code Rule}s. Methods change the {@code BoardManager}
 * parameter to set the boards and they return the {@code Rules} object for the board. Also, they place entities at their correct starting
 * place on the board.
 *
 * @author Phillip O'Reggio
 */
public class BoardAndRuleConstructor {

    /**
     * Creates the Board and Rules based on the board index. Indexes are arranged in groups of 3, with the 1st one being a standard
     * 2-player match, the second being a 2-player zone match, and the third being a 4-player zone match.
     * @param boardIndex Index determining what board is created and what rules are returned. <p>
     *                   1-3 : Simple <p>
     *                   4-6 : Complex <p>
     *                   7-9 : <p>
     * @return {@code Rules} that should be used in the BattleScreen.
     */
    public static Rules getBoardAndRules(int boardIndex, BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        switch (boardIndex) {
            case 1 :
                return makeSimple2P(screen, teams, boardManager);
            case 2 :
                return makeSimple2PZone(screen, teams, boardManager);
            case 3 :
                return makeSimple4PZone(screen, teams, boardManager);
            case 4 :
                return makeComplex2P(screen, teams, boardManager);
            case 5 :
                return makeComplex2PZone(screen, teams, boardManager);
            case 6 :
                return makeComplex4PZone(screen, teams, boardManager);
        }
        return null;
    }

    //for reference when making boards that are greater than 7 by 7
    /*
    LESS THAN 7
       new Board(boardSize, boardSize, c, c2, 100
    GREATER
       new Board(boardSize, boardSize, c, c2, 700 / boardSize
   */

    //Simple
    public static Rules makeSimple2P(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }
        System.out.println(boardManager.getBoard());
        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSimple2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(maxSize, 5),
                        new BoardPosition(maxSize, 4),
                        new BoardPosition(maxSize, 3),
                        new BoardPosition(maxSize, 2)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 1),
                        new BoardPosition(0, 2),
                        new BoardPosition(0, 3),
                        new BoardPosition(0, 4)})
        }));

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //color zones
        rules.colorZones();

        return rules;
    }

    public static Rules makeSimple4PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(9, 9, 700 / 9), new CodeBoard(9, 9));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(maxSize, 6),
                        new BoardPosition(maxSize, 5),
                        new BoardPosition(maxSize, 4),
                        new BoardPosition(maxSize, 3)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 2),
                        new BoardPosition(0, 3),
                        new BoardPosition(0, 4),
                        new BoardPosition(0, 5)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(3, maxSize),
                        new BoardPosition(4, maxSize),
                        new BoardPosition(5, maxSize),
                        new BoardPosition(6, maxSize)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(2, 0),
                        new BoardPosition(3, 0),
                        new BoardPosition(4, 0),
                        new BoardPosition(5, 0)}),
        }));

        //place entities
        int col, row;
        col = 2;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 6;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }
        row = 2;
        for (Entity e : teams.get(2).getEntities()) {
            boardManager.add(e, new BoardPosition(row, 0));
            row++;
        }
        row = 3;
        for (Entity e : teams.get(3).getEntities()) {
            boardManager.add(e, new BoardPosition(row, maxSize));
            row++;
        }

        //color zones
        rules.colorZones();

        return rules;
    }

    //Complex
    public static Rules makeComplex2P(BattleScreen screen, Array<Team> teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }
        //place blocks randomly
        for (int i = 0; i < 8; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(1, 5), MathUtils.random(1, 5));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            boardManager.add(EntityConstructor.cube(GridWars.engine, GridWars.stage), pos);
        }
        return new Battle2PRules(screen, teams);
    }

    public static Rules makeComplex2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(maxSize, 5),
                        new BoardPosition(maxSize, 4),
                        new BoardPosition(maxSize, 3),
                        new BoardPosition(maxSize, 2)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 1),
                        new BoardPosition(0, 2),
                        new BoardPosition(0, 3),
                        new BoardPosition(0, 4)})
        }));

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //place blocks randomly
        for (int i = 0; i < 12; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(1, 5), MathUtils.random(1, 5));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            boardManager.add(EntityConstructor.cube(GridWars.engine, GridWars.stage), pos);
        }

        //color zones
        rules.colorZones();

        return rules;
    }

    public static Rules makeComplex4PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(9, 9, 700 / 9), new CodeBoard(9, 9));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(maxSize, 6),
                        new BoardPosition(maxSize, 5),
                        new BoardPosition(maxSize, 4),
                        new BoardPosition(maxSize, 3)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 2),
                        new BoardPosition(0, 3),
                        new BoardPosition(0, 4),
                        new BoardPosition(0, 5)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(3, maxSize),
                        new BoardPosition(4, maxSize),
                        new BoardPosition(5, maxSize),
                        new BoardPosition(6, maxSize)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(2, 0),
                        new BoardPosition(3, 0),
                        new BoardPosition(4, 0),
                        new BoardPosition(5, 0)}),
        }));

        //place entities
        int col, row;
        col = 2;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 6;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }
        row = 2;
        for (Entity e : teams.get(2).getEntities()) {
            boardManager.add(e, new BoardPosition(row, 0));
            row++;
        }
        row = 3;
        for (Entity e : teams.get(3).getEntities()) {
            boardManager.add(e, new BoardPosition(row, maxSize));
            row++;
        }

        //place blocks randomly
        for (int i = 0; i < 24; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(1, 7), MathUtils.random(1, 7));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            boardManager.add(EntityConstructor.cube(GridWars.engine, GridWars.stage), pos);
        }

        //color zones
        rules.colorZones();

        return rules;
    }
}
