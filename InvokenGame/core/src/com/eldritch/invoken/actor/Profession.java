package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.eldritch.invoken.InvokenGame;
import com.eldritch.invoken.actor.aug.Augmentation;
import com.eldritch.invoken.actor.aug.Barrier;
import com.eldritch.invoken.actor.aug.Cloak;
import com.eldritch.invoken.actor.aug.Crack;
import com.eldritch.invoken.actor.aug.Frenzy;
import com.eldritch.invoken.actor.aug.Drain;
import com.eldritch.invoken.actor.aug.FireWeapon;
import com.eldritch.invoken.actor.aug.Implode;
import com.eldritch.invoken.actor.aug.Infect;
import com.eldritch.invoken.actor.aug.Jaunt;
import com.eldritch.invoken.actor.aug.Mirror;
import com.eldritch.invoken.actor.aug.Observe;
import com.eldritch.invoken.actor.aug.Paralyze;
import com.eldritch.invoken.actor.aug.RendWeapon;
import com.eldritch.invoken.actor.aug.Resurrect;
import com.eldritch.invoken.actor.aug.Scramble;
import com.eldritch.invoken.actor.aug.ThrowGrenade;
import com.eldritch.invoken.actor.items.Item;
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.type.Human;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Actors.ActorParams.Skill;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.ui.MultiTextureRegionDrawable;
import com.google.common.collect.ImmutableList;

public enum Profession {
	Centurion() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.WARFARE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
					FireWeapon.getInstance(),
					Jaunt.getInstance(),
					ThrowGrenade.getInstance(),
					Implode.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("IcarianInfantryArmor"));
        }

        @Override
        public String getDescription() {
            return "Centurion";
        }
	},
	
	Executor() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.AUTOMATA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
					FireWeapon.getInstance(),
					Drain.getInstance(),
					Mirror.getInstance(),
					Barrier.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("EruInfantryArmor"));
        }
		
		@Override
        public String getDescription() {
            return "Executor";
        }
	},
	
	Assassin() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.SUBTERFUGE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
					RendWeapon.getInstance(),
					FireWeapon.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("LorentAssasinGarb"));
        }
		
		@Override
        public String getDescription() {
            return "Assassin";
        }
	},
	
	Warden() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.WARFARE, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
					FireWeapon.getInstance(),
					ThrowGrenade.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("EruInfantryArmor"));
        }
		
		@Override
        public String getDescription() {
            return "Warden";
        }
	},
	
	Architect() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.AUTOMATA, Discipline.AUTOMATA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
					Drain.getInstance(), 
					Infect.getInstance(),
					Resurrect.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("MorvaynArchitectRobes"));
        }
		
		@Override
        public String getDescription() {
            return "Architect";
        }
	},
	
	Ghost() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.AUTOMATA, Discipline.SUBTERFUGE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
					FireWeapon.getInstance(),
					Cloak.getInstance(),
					Crack.getInstance(),
					Scramble.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("IcarianOperativeExosuit"));
        }
		
		@Override
        public String getDescription() {
            return "Ghost";
        }
	},
	
	Inquisitor() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.AUTOMATA, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
					Drain.getInstance(),
					Paralyze.getInstance(),
					Frenzy.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("EruInquisitorVestments"));
        }
		
		@Override
        public String getDescription() {
            return "Inquisitor";
        }
	},
	
	Infiltrator() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.SUBTERFUGE, Discipline.SUBTERFUGE);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(FireWeapon.getInstance());
		}

        @Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("IcarianOperativeExosuit"));
        }
        
        @Override
        public String getDescription() {
            return "Infiltrator";
        }
	},
	
	Agent() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.SUBTERFUGE, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(
			        FireWeapon.getInstance(),
			        Observe.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("IcarianOperativeExosuit"));
        }
		
		@Override
        public String getDescription() {
            return "Agent";
        }
	},
	
	Broker() {
		@Override
		public List<Discipline> getMasteries() {
			return ImmutableList.of(Discipline.CHARISMA, Discipline.CHARISMA);
		}
		
		@Override
		public List<Augmentation> getStartingAugmentations() {
			return ImmutableList.<Augmentation>of(FireWeapon.getInstance());
		}
		
		@Override
        public Outfit getDefaultOutfit() {
            return new Outfit(InvokenGame.ITEM_READER.readAsset("ImperialCitizenAttire"));
        }
		
		@Override
        public String getDescription() {
            return "Broker";
        }
	};
	
	public abstract List<Discipline> getMasteries();
	
	public abstract List<Augmentation> getStartingAugmentations();
	
	public abstract Outfit getDefaultOutfit();
	
	public abstract String getDescription();
	
	public static Profession getDefault() {
        return Agent;
    }
	
	public static List<Skill> getSorted(List<Skill> skills, final Collection<Discipline> masteries) {
		Collections.sort(skills, new Comparator<Skill>() {
			@Override
			public int compare(Skill s1, Skill s2) {
				return masteries.contains(s2.getDiscipline()) ? 1 :
					masteries.contains(s1.getDiscipline()) ? -1 : 0;
			}
		});
		return skills;
	}
	
	public List<Skill> getSkillsFor(int level) {
		// All skills default to 10
		Map<Discipline, Skill.Builder> skills = new LinkedHashMap<Discipline, Skill.Builder>();
		for (Discipline d : Discipline.values()) {
			skills.put(d, Skill.newBuilder().setDiscipline(d).setLevel(10));
		}
		
		// Assign 10 additional skill levels for each mastery
		Collection<Discipline> masteries = getMasteries();
		for (Discipline d : masteries) {
			Skill.Builder s = skills.get(d);
			s.setLevel(s.getLevel() + 10);
		}
		
		// Start leveling from level 2.  5 skill points per level.  Assign skill points
		// intelligently.
		// In general, we want to maintain a 2:2:1:1 ratio.
		// For level n, we have 5(n-1) skill points to distribute
		int points = 5 * (level - 1);
		
		// Divide the first 1/3 of the points into a separate pool for masteries.
		// Spread them uniformly.
		int available = points / 3;
		
		int used = 0;
		for (Discipline d : masteries) {
			int a = available / 2;
			Skill.Builder s = skills.get(d);
			s.setLevel(s.getLevel() + a);
			used += a;
		}
		
		// Now we want to take the remainder and distribute them randomly
		List<Skill.Builder> list = new ArrayList<Skill.Builder>(skills.values()); 
		Random rand = new Random();
		int remaining = points - used;
		while (remaining > 0) {
			int i = rand.nextInt(4);
			Skill.Builder s = list.get(i);
			s.setLevel(s.getLevel() + 1);
			remaining--;
		}
		
		// Construct the skills
		List<Skill> result = new ArrayList<Skill>();
		for (Skill.Builder b : skills.values()) {
			result.add(b.build());
		}
		return getSorted(result, masteries);
	}
	
	public TextureRegionDrawable getPortrait() {
        TextureRegion region = Human.getDefaultAnimations()
                .get(Activity.Explore).get(Direction.Right).getKeyFrame(0);
        Outfit outfit = getDefaultOutfit();
        TextureRegion outfitRegion = outfit.getPortrait();
        if (outfit.covers()) {
            return new MultiTextureRegionDrawable(outfitRegion);
        } else {
            return new MultiTextureRegionDrawable(region, outfitRegion);
        }
    }
	
	public static Profession fromProto(com.eldritch.invoken.proto.Disciplines.Profession p) {
		switch (p) {
			case CENTURION:
				return Centurion;
			case EXECUTOR:
				return Executor;
			case ASSASSIN:
				return Assassin;
			case WARDEN:
				return Warden;
			case ARCHITECT:
				return Architect;
			case GHOST:
				return Ghost;
			case INQUISITOR:
				return Inquisitor;
			case INFILTRATOR:
				return Infiltrator;
			case AGENT:
				return Agent;
			case BROKER:
				return Broker;
			default:
				throw new IllegalArgumentException("Unrecognized Profession: " + p);
		}
	}
}
