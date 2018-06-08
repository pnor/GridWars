package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.boards.BoardPosition;

/**
 * Wrapper class for the ArrayMap that stores EntityValues. Either {@link com.mygdx.game.boards.BoardPosition} or {@link com.badlogic.ashley.core.Entity} can
 * be used to get the entityValue
 *
 * @author Phillip O'Reggio
 */
public class EntityMap {
    private ObjectMap<Entity, EntityValue> entityMap;
    private ObjectMap<BoardPosition, EntityValue> boardPostionMap;
    /**
     * Map that allows the access of Entity Keys from EntityValues. This allows things like removing Entities by BoardPosition possible in a
     * timely fashion.
     */
    private ObjectMap<EntityValue, Entity> reverseEntityMap;

    public EntityMap() {
        entityMap = new ObjectMap<>();
        boardPostionMap = new ObjectMap<>();
        reverseEntityMap = new ObjectMap<>();
    }

    /**
     * Puts a {@link EntityValue} into the two {@link ArrayMap}s
     * @param keyEntity Entity to be used as a key value
     * @param keyBoardPosition BoardPosition to be used as a key value
     * @param entityValue EntityValue being stored
     */
    public void put(Entity keyEntity, BoardPosition keyBoardPosition, EntityValue entityValue) {
        entityMap.put(keyEntity, entityValue);
        boardPostionMap.put(keyBoardPosition, entityValue);
        reverseEntityMap.put(entityValue, keyEntity);
    }

    public EntityValue get(Entity keyEntity) {
        return entityMap.get(keyEntity);
    }

    public EntityValue get(BoardPosition keyBoardPosition) {
        return boardPostionMap.get(keyBoardPosition);
    }

    public Entity getKeyEntity(EntityValue entityValue) {
        return reverseEntityMap.get(entityValue);
    }

    public EntityValue remove(Entity keyEntity) {
        boardPostionMap.remove(entityMap.get(keyEntity).pos);
        reverseEntityMap.remove(entityMap.get(keyEntity));
        return entityMap.remove(keyEntity);
    }

    public EntityValue remove(BoardPosition keyBoardPosition) {
        entityMap.remove(reverseEntityMap.get(boardPostionMap.get(keyBoardPosition)));
        reverseEntityMap.remove(boardPostionMap.get(keyBoardPosition));
        return boardPostionMap.remove(keyBoardPosition);
    }

    public EntityValue remove(EntityValue entityValue) {
        boardPostionMap.remove(entityValue.pos);
        entityMap.remove(reverseEntityMap.get(entityValue));
        reverseEntityMap.remove(entityValue);
        return entityValue;
    }

    public boolean containsKey(Entity keyEntity) {
        return entityMap.containsKey(keyEntity);
    }

    public boolean containsKey(BoardPosition keyBoardPosition) {
        return boardPostionMap.containsKey(keyBoardPosition);
    }

    public boolean contains(EntityValue entity) {
        return reverseEntityMap.containsKey(entity);
    }

    public Array<EntityValue> getEntityValues() {
        return entityMap.values().toArray();
    }

    public Array<BoardPosition> getAllPositions() {
        return boardPostionMap.keys().toArray();
    }

    public Array<Entity> getAllEntities() {
        return reverseEntityMap.values().toArray();
    }

    /**
     * Used in toString method of BoardState
     */
    public ObjectMap.Entries<BoardPosition, EntityValue> getPositionEntityValuePairs() {
        return boardPostionMap.entries();
    }
}
