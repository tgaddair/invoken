package com.eldritch.scifirpg.game.model.actor;

import java.util.Collection;
import java.util.List;

import com.eldritch.scifirpg.game.model.ActiveAugmentation;
import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.google.common.base.Optional;

public class Action {
    private final ActiveAugmentation aug;
    private final ActorState actor;
    private final Optional<ActorState> selected;
    
    public Action(ActiveAugmentation aug, ActorState actor) {
        this.aug = aug;
        this.actor = actor;
        selected = Optional.absent();
    }
    
    public Action(ActiveAugmentation aug, ActorState actor, ActorState selected) {
        this.aug = aug;
        this.actor = actor;
        this.selected = Optional.of(selected);
    }
    
    public boolean canTake() {
        if (aug.needsTarget() && !hasSelectedTarget()) {
            return false;
        }
        return actor.canTakeAction(this);
    }
    
    public boolean isAttack() {
        return aug.getType() == Augmentation.Type.ATTACK;
    }
    
    public boolean isInvoke() {
        return aug.getType() == Augmentation.Type.EXECUTE;
    }
    
    public boolean isDeceive() {
        return aug.getType() == Augmentation.Type.DECEIVE;
    }
    
    public boolean isInfluence() {
        return aug.getType() == Augmentation.Type.DIALOGUE;
    }
    
    public ActorState getActor() {
        return actor;
    }
    
    public ActiveAugmentation getAugmentation() {
        return aug;
    }
    
    public ActorState getSelected() {
        return selected.get();
    }
    
    public List<Result> applyEffects(Collection<ActorState> actors) {
        return aug.applyUse(this, actors);
    }
    
    public List<Effect> getEffects() {
        return aug.getEffects();
    }
    
    public boolean hasSelectedTarget() {
        if (!selected.isPresent()) {
            return false;
        }
        //return selected.get().isTargetable();
        return true;
    }
}
