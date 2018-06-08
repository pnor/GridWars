package com.mygdx.game.misc;

/**
 * To store 2 variables in one object
 * @author Phillip O'Reggio
 */
public class Tuple<U, V> {

    public U value1;
    public V value2;

    public Tuple(U firstVal, V secondValue) {
        value1 = firstVal;
        value2 = secondValue;
    }
}
