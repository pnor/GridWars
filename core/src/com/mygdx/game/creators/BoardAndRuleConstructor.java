package com.mygdx.game.creators;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.Board;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.boards.CodeBoard;
import com.mygdx.game.rules_types.Battle2PRules;
import com.mygdx.game.rules_types.Rules;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.screens_ui.screens.BattleScreen;

/**
 * Class containing static methods for initializing specific {@code Board}s and {@code Rule}s. Methods change the {@code BoardManager}
 * parameter to set the boards and they return the {@code Rules} object for the board. Also, they place entities at their correct starting
 * place on the board.
 *
 * @author Phillip O'Reggio
 */
public class BoardAndRuleConstructor {

    public static Rules getBoardAndRules(int boardIndex, BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        switch (boardIndex) {
            case 1 :
                return makeSimple2P(screen, teams, boardManager);
            case 2 :
                return makeSimple2P(screen, teams, boardManager);
            case 3 :
                return makeSimple2P(screen, teams, boardManager);
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

    public static Rules makeSimple2P(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(6, col));
            col--;
        }
        return new Battle2PRules(screen, teams);
    }
}
