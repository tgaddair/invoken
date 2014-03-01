package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.scifirpg.game.model.ActorModel.Npc;
import com.eldritch.scifirpg.game.util.EffectUtil;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.google.common.base.Optional;

/**
 * Handles the internal state of a single ActorEncounter. Makes requests to the
 * global ActorModel for data and updates the ActorModel's persistent state
 * (who's alive, etc.).
 * 
 */
public class ActorEncounterModel extends EncounterModel<ActorEncounter> {
    private final ActorModel model;
    private final List<Npc> actors;
    private final List<ActorListener> listeners = new ArrayList<>();

    public ActorEncounterModel(ActorEncounter encounter, ActorModel model) {
        super(encounter);
        this.model = model;
        this.actors = model.getActorsFor(getEncounter());
    }
    
    
    /**
     * Invoke on self if target is not specified.  Some augmentations will also automatically
     * apply to a specific target or group of targets (like all) depending on range.
     */
    public void invoke(ActionAugmentation aug) {
        invoke(aug, aug.getOwner());
    }

    public void invoke(ActionAugmentation aug, Actor target) {
        // Allow target to respond to the invocation
        switch (aug.getType()) {
            case ATTACK: // Playable to make hostile
                break;
            case DECEIVE: // Playable when not detected
            case EXECUTE: // Playable in encounter
            case DIALOGUE: // Playable in dialogue
            case COUNTER: // Playable when targeted
            case TRAP: // Playable at any time, activates when targeted and effect applies
                break;
            case PASSIVE: // Playable when attuning outside encounter
                // It's a bug if we have an ActionAugmentation with passive type
            default:
                throw new IllegalArgumentException(
                        "Unrecognized Augmentation Type: " + aug.getType());
        }
        
        // No counter, apply effects
        Optional<Actor> source = Optional.of(aug.getOwner());
        Optional<Actor> dest = Optional.of(target);
        for (Effect effect : aug.getEffects()) {
            EffectUtil.apply(effect, source, dest);
        }
        
        // TODO handle duration effects by keeping a list we apply at the end of the round
    }
    
    public void addListener(ActorListener listener) {
        listeners.add(listener);
    }
    
    public Player getPlayer() {
        return model.getPlayer();
    }
    
    public List<Npc> getActors() {
        return actors;
    }
    
    public ActorModel getActorModel() {
        return model;
    }
    
    public static interface ActorListener {
        void actorKilled(Actor actor);
        
        void actorTargeted(Actor actor);
    }
}
