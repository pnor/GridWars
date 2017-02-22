package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Move;

/**
 * Component representing a list of moves for an Entity on the board to use.
 * Created by pnore_000 on 1/31/2017.
 */
public class MovesetComponent implements Component {
    public Array<Move> moveList;

    public MovesetComponent(Array<Move> m) {
        moveList = m;
    }
}
