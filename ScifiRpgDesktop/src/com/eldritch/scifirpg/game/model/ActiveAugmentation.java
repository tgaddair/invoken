package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eldritch.scifirpg.game.model.actor.Action;
import com.eldritch.scifirpg.game.model.actor.ActiveEffect;
import com.eldritch.scifirpg.game.model.actor.Actor;
import com.eldritch.scifirpg.game.model.actor.ActorState;
import com.eldritch.scifirpg.game.util.EffectUtil;
import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.invoken.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.invoken.proto.Augmentations.Augmentation;
import com.eldritch.invoken.proto.Augmentations.Augmentation.Type;
import com.eldritch.invoken.proto.Effects.Effect;
import com.eldritch.invoken.proto.Effects.Effect.Range;

public class ActiveAugmentation {
    private final Augmentation aug;
    private final Actor owner;
    private final int stages;
    private int uses;

    public ActiveAugmentation(Augmentation aug, Actor owner, StagedAugmentation state) {
        this.aug = aug;
        this.owner = owner;
        this.stages = state.getStages();
        this.uses = state.getRemainingUses();
    }
    
    public ActiveAugmentation(Augmentation aug, Actor owner, int uses) {
        this.aug = aug;
        this.owner = owner;
        this.uses = uses;
        stages = uses;
    }
    
    public int getUses() {
        return uses;
    }
    
    public List<Result> applyUse(Action action, Collection<ActorState> actors) {
        List<Result> results = new ArrayList<>();
        
        ActorState source = action.getActor();
        for (ActorState target : actors) {
            if (EffectUtil.isTargetFor(action, target)) {
                if (succeeds(action, target)) {
                    for (Effect effect : getEffects()) {
                        if (isApplicable(action, target, effect)) {
                            ActiveEffect active = EffectUtil.createEffect(effect, source, target);
                            results.add(target.applyEffect(active));
                        }
                    }
                } else {
                    results.add(new Result(target.getActor(), "MISSED", false));
                }
            }
        }
        
        markUse();
        return results;
    }
    
    private boolean succeeds(Action action, ActorState target) {
        return target.checkSuccess(action);
    }
    
    private boolean isApplicable(Action action, ActorState target, Effect effect) {
        return EffectUtil.isTargetFor(action, target, effect);
    }
    
    private void markUse() {
        uses--;
        if (uses <= 0) {
            // Disable further use
        }
    }
    
    public Actor getOwner() {
        return owner;
    }
    
    public String getName() {
        return aug.getName();
    }
    
    public String getId() {
        return aug.getId();
    }
    
    public Type getType() {
        return aug.getType();
    }
    
    public List<Effect> getEffects() {
        return aug.getEffectList();
    }
    
    public boolean needsTarget() {
        for (Effect effect : getEffects()) {
            if (effect.getRange() == Range.SELECTED) {
                return true;
            }
        }
        return false;
    }
    
    public int getStages() {
        return stages;
    }
}
