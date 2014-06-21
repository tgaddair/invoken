package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.type.Agent;

public class PreparedAugmentations {
	private final List<Augmentation> augs = new ArrayList<Augmentation>();
	private final Agent owner;
	private final Set<Augmentation> activeSelfAugmentations = new HashSet<Augmentation>();
	private Augmentation activeAugmentation;
	
	public PreparedAugmentations(Agent owner) {
		this.owner = owner;
	}
	
	public Iterable<Augmentation> getAugmentations() {
	    return augs;
	}
	
	public Augmentation getAugmentation(int index) {
	    return augs.get(index);
	}
	
	public boolean isActive(Augmentation aug) {
	    return activeAugmentation == aug || activeSelfAugmentations.contains(aug);
	}
	
	public Augmentation getActiveAugmentation() {
	    return activeAugmentation;
	}
	
	public Set<Augmentation> getActiveSelfAugmentations() {
	    return activeSelfAugmentations;
	}
	
	public void addAugmentation(Augmentation aug) {
		augs.add(aug);
	}
	
	public boolean hasActiveAugmentation() {
	    return activeAugmentation != null;
	}
	
	public void toggleActiveAugmentation(int index) {
	    if (index < augs.size()) {
	        toggleActiveAugmentation(augs.get(index));
	    }
	}
	
	public void toggleActiveAugmentation(Augmentation aug) {
	    if (aug.castsOnSelf()) {
	        boolean used = use(aug);
	        if (activeSelfAugmentations.contains(aug)) {
	            removeActiveAugmentation(aug);
	        } else if (used) {
	            addActiveAugmentation(aug);
	        }
	    } else {
	        if (aug != activeAugmentation) {
	            addActiveAugmentation(aug);
	        } else {
	            removeActiveAugmentation(aug);
	        }
	    }
	}
	
	public void addActiveAugmentation(Augmentation aug) {
	    if (aug.castsOnSelf()) {
	        activeSelfAugmentations.add(aug);
	    } else {
	        activeAugmentation = aug;
	    }
	}
	
    public void removeActiveAugmentation(Augmentation aug) {
        if (aug.castsOnSelf()) {
            activeSelfAugmentations.remove(aug);
        } else {
            activeAugmentation = null;
        }
    }
	
	public void useActiveAugmentation() {
	    useActiveAugmentation(false);
	}
	
	public void useActiveAugmentation(boolean queued) {
	    if (activeAugmentation != null) {
	        use(activeAugmentation, queued);
	    }
	}
	
    public boolean use(int index) {
        return use(index, false);
    }
    
    public boolean use(int index, boolean queued) {
        if (index < augs.size()) {
            return use(augs.get(index), queued);
        }
        return false;
    }
    
    public boolean use(Augmentation aug) {
        return use(aug, false);
    }
    
    public boolean use(Augmentation aug, boolean queued) {
        if (queued || owner.canAddAction()) {
            return aug.invoke(owner, owner.getTarget());
        }
        return false;
    }
}
