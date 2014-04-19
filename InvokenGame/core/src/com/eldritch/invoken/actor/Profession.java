package com.eldritch.invoken.actor;

import java.util.List;

import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Barrier;
import com.eldritch.invoken.actor.aug.Command;
import com.eldritch.invoken.actor.aug.Drain;
import com.eldritch.invoken.actor.aug.FireWeapon;
import com.eldritch.invoken.actor.aug.Paralyze;
import com.eldritch.invoken.actor.aug.Resurrect;
import com.google.common.collect.ImmutableList;

public interface Profession {
	List<Augmentation> getStartingAugmentations();
	
	public static class Centurion implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Executor implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon(), new Barrier(), new Drain());
		}
	}
	
	public static class Warden implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Assassin implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Architect implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new Drain(), new Resurrect());
		}
	}
	
	public static class Ghost implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Inquisitor implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new Drain(), new Paralyze(), new Command());
		}
	}
	
	public static class Infiltrator implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Agent implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
	
	public static class Broker implements Profession {
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(new FireWeapon());
		}
	}
}
