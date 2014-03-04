package com.eldritch.scifirpg.game.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.eldritch.scifirpg.game.model.actor.Actor;
import com.eldritch.scifirpg.game.util.EffectUtil;
import com.eldritch.scifirpg.game.util.Result;
import com.eldritch.scifirpg.proto.Actors.PlayerActor.StagedAugmentation;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation;
import com.eldritch.scifirpg.proto.Augmentations.Augmentation.Type;
import com.eldritch.scifirpg.proto.Effects.Effect;
import com.eldritch.scifirpg.proto.Effects.Effect.Range;
import com.google.common.base.Optional;

public class ActionAugmentation {
    private final Augmentation aug;
    private final Actor owner;
    private final int stages;
    private int uses;

    public ActionAugmentation(Augmentation aug, Actor owner, StagedAugmentation state) {
        this.aug = aug;
        this.owner = owner;
        this.stages = state.getStages();
        this.uses = state.getRemainingUses();
    }
    
    public ActionAugmentation(Augmentation aug, Actor owner, int uses) {
        this.aug = aug;
        this.owner = owner;
        this.uses = uses;
        stages = uses;
    }
    
    public int getUses() {
        return uses;
    }
    
    public List<Result> apply(Collection<Actor> combatants, Actor selected) {
        List<Result> results = new ArrayList<>();
        Optional<Actor> source = Optional.of(getOwner());
        for (Effect effect : getEffects()) {
            for (Actor target : EffectUtil.getTargets(effect, owner, selected, combatants)) {
                if (succeedsOn(target)) {
                    Optional<Actor> dest = Optional.of(target);
                    results.add(EffectUtil.apply(effect, source, dest));
                } else {
                    results.add(new Result(owner, String.format(
                            "%s MISSED %s", owner.getName(), target.getName())));
                }
            }
        }
        markUse();
        return results;
    }
    
    private void markUse() {
        uses--;
        if (uses <= 0) {
            // Disable further use
        }
    }
    
    /**
     * Returns true iff initiated success.
     */
    private boolean succeedsOn(Actor target) {
        switch (aug.getType()) {
            case ATTACK: // Playable to make hostile
                return target.handleAttack(this);
            case DECEIVE: // Playable when not detected
                return target.handleDeceive(this);
            case EXECUTE: // Playable in encounter
                return target.handleExecute(this);
            case DIALOGUE: // Playable in dialogue
            case COUNTER: // Playable when targeted
                return false;
            case TRAP: // Playable at any time, activates when targeted and effect applies
                // Deprecated
            case PASSIVE: // Playable when attuning outside encounter
                // It's a bug if we have an ActionAugmentation with passive type
            default:
                throw new IllegalArgumentException(
                        "Unrecognized Augmentation Type: " + aug.getType());
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
}
