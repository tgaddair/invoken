package com.eldritch.scifirpg.game.model;

import java.util.List;

import com.eldritch.scifirpg.game.model.actor.Actor;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Type;
import com.eldritch.scifirpg.proto.Effects.Effect;

public class ActionAugmentation {
    private final Augmentation aug;
    private final Actor owner;
    
    public ActionAugmentation(Augmentation aug, Actor owner) {
        this.aug = aug;
        this.owner = owner;
    }
    
    public Actor getOwner() {
        return owner;
    }
    
    public String getName() {
        return aug.getName();
    }
    
    public Type getType() {
        return aug.getType();
    }
    
    public List<Effect> getEffects() {
        return aug.getEffectList();
    }
}
