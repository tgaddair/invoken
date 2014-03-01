package com.eldritch.scifirpg.game.model;

import com.eldritch.scifirpg.proto.Augmentations.Augmentation;

public class ActionAugmentation {
    private final Augmentation aug;
    private final Actor owner;
    
    public ActionAugmentation(Augmentation aug, Actor owner) {
        this.aug = aug;
        this.owner = owner;
    }
    
    /**
     * Invoke on self if target is not specified.  Some augmentations will also automatically
     * apply to a specific target or group of targets (like all) depending on range.
     */
    public void invoke() {
        invokeOn(owner);
    }

    public void invokeOn(Actor target) {
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
    }
    
    public String getName() {
        return aug.getName();
    }
}
