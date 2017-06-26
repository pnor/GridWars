package com.mygdx.game;

import com.badlogic.ashley.core.ComponentMapper;
import com.mygdx.game.components.*;

/**
 * @author pnore_000
 */
public class ComponentMappers {
    //sprites
    public static ComponentMapper<SpriteComponent> sm = ComponentMapper.getFor(SpriteComponent.class);
    //animations
    public static ComponentMapper<AnimationComponent> animm = ComponentMapper.getFor(AnimationComponent.class);
    //position
    public static ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    //movement
    public static ComponentMapper<MovementComponent> mm = ComponentMapper.getFor(MovementComponent.class);
    //lifetime
    public static ComponentMapper<LifetimeComponent> lfm = ComponentMapper.getFor(LifetimeComponent.class);
    //events
    public static ComponentMapper<EventComponent> em = ComponentMapper.getFor(EventComponent.class);
    //board
    public static ComponentMapper<BoardComponent> bm = ComponentMapper.getFor(BoardComponent.class);
    //actor
    public static ComponentMapper<ActorComponent> am = ComponentMapper.getFor(ActorComponent.class);
    //visuals
    public static ComponentMapper<VisualsComponent> vm = ComponentMapper.getFor(VisualsComponent.class);
    //stats
    public static ComponentMapper<StatComponent> stm = ComponentMapper.getFor(StatComponent.class);
    //status effects
    public static ComponentMapper<StatusEffectComponent> status = ComponentMapper.getFor(StatusEffectComponent.class);
    //moveset
    public static ComponentMapper<MovesetComponent> mvm = ComponentMapper.getFor(MovesetComponent.class);
    //team
    public static ComponentMapper<TeamComponent> team = ComponentMapper.getFor(TeamComponent.class);
    //state
    public static ComponentMapper<StateComponent> state = ComponentMapper.getFor(StateComponent.class);
    //names
    public static ComponentMapper<NameComponent> nm = ComponentMapper.getFor(NameComponent.class);

}
