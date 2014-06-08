package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.actor.aug.Augmentation;

public class PreparedAugmentations {
	private final List<Augmentation> augs = new ArrayList<Augmentation>();
	private final Agent owner;
	private Augmentation activeAugmentation;
	
	public PreparedAugmentations(Agent owner) {
		this.owner = owner;
	}
	
	public Iterable<Augmentation> getAugmentations() {
	    return augs;
	}
	
	public Augmentation getActiveAugmentation() {
	    return activeAugmentation;
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
	    if (aug != activeAugmentation) {
            setActiveAugmentation(aug);
        } else {
            clearActiveAugmentation();
        }
	}
	
	public void clearActiveAugmentation() {
	    setActiveAugmentation(null);
	}
	
	public void setActiveAugmentation(Augmentation aug) {
	    activeAugmentation = aug;
	}
	
	public void useActiveAugmentation() {
	    useActiveAugmentation(false);
	}
	
	public void useActiveAugmentation(boolean queued) {
	    if (activeAugmentation != null) {
	        use(activeAugmentation, queued);
	    }
	}
	
    public void use(int index) {
        use(index, false);
    }
    
    public void use(int index, boolean queued) {
        if (index < augs.size()) {
            use(augs.get(index), queued);
        }
    }
    
    public void use(Augmentation aug, boolean queued) {
        if (queued || owner.canAddAction()) {
            aug.invoke(owner, owner.getTarget());
        }
    }
}
