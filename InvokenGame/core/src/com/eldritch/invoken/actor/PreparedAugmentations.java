package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.List;

import com.eldritch.invoken.actor.aug.Augmentation;

public class PreparedAugmentations {
	private final List<Augmentation> augs = new ArrayList<Augmentation>();
	private final Agent owner;
	
	public PreparedAugmentations(Agent owner) {
		this.owner = owner;
	}
	
	public void use(int index) {
		augs.get(index).invoke(owner, owner.getTarget());
	}
	
	public void addAugmentation(Augmentation aug) {
		augs.add(aug);
	}
}
