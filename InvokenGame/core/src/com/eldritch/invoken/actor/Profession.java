package com.eldritch.invoken.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
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
import com.eldritch.invoken.actor.items.Outfit;
import com.eldritch.invoken.actor.type.Human;
import com.eldritch.invoken.actor.type.Agent.Activity;
import com.eldritch.invoken.actor.type.Agent.Direction;
import com.eldritch.invoken.proto.Actors.ActorParams.Skill;
import com.eldritch.invoken.proto.Disciplines;
import com.eldritch.invoken.proto.Disciplines.Discipline;
import com.eldritch.invoken.screens.GameScreen;
import com.eldritch.invoken.ui.MultiTextureRegionDrawable;
import com.eldritch.invoken.util.Settings;
import com.eldritch.invoken.util.WeightedSample;
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

        @Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.CENTURION;
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
            return new Outfit(InvokenGame.ITEM_READER.readAsset("ExecutorArmor"));
        }
		
		@Override
        public String getDescription() {
            return "Executor";
        }
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.EXECUTOR;
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
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.ASSASSIN;
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
            return new Outfit(InvokenGame.ITEM_READER.readAsset("ArtorenWardenMail"));
        }
		
		@Override
        public String getDescription() {
            return "Warden";
        }
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.WARDEN;
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
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.ARCHITECT;
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
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.GHOST;
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
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.INQUISITOR;
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
            return new Outfit(InvokenGame.ITEM_READER.readAsset("InfiltratorWetsuit"));
        }
        
        @Override
        public String getDescription() {
            return "Infiltrator";
        }
        
        @Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.INFILTRATOR;
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
            return new Outfit(InvokenGame.ITEM_READER.readAsset("AgentAttire"));
        }
		
		@Override
        public String getDescription() {
            return "Agent";
        }
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.AGENT;
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
		
		@Override
        public Disciplines.Profession toProto() {
            return Disciplines.Profession.BROKER;
        }
	};
	
	public abstract List<Discipline> getMasteries();
	
	public abstract List<Augmentation> getStartingAugmentations();
	
	public abstract Outfit getDefaultOutfit();
	
	public abstract String getDescription();
	
	public abstract Disciplines.Profession toProto();
	
	public static Profession getDefault() {
        return Settings.DEFAULT_PROFESSION;
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
		
		// Assign skill points wisely.
		Map<Discipline, Double> weights = new HashMap<>();
		for (Discipline d : Discipline.values()) {
		    weights.put(d, 1.0);
		}
		
		// In general, each master should be 50% more likely than a non-master.
        // So if we double down on a mastery, it should be twice as represented as other skills.
		Collection<Discipline> masteries = getMasteries();
		for (Discipline d : masteries) {
		    weights.put(d, weights.get(d) + 0.5);
		}
		WeightedSample<Discipline> sample = new WeightedSample<>(weights);
		
		// Start leveling from level 2.  1 skill point per level.
        int points = level - 1;
        for (int i = 0; i < points; i++) {
            Discipline d = sample.sample();
            Skill.Builder s = skills.get(d);
            s.setLevel(s.getLevel() + 1);
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
	
	public List<Texture> getIcons() {
	    ImmutableList.Builder<Texture> builder = ImmutableList.builder();
	    for (Discipline d : getMasteries()) {
	        builder.add(Holder.DISCIPLINE_ICONS.get(d));
	    }
	    return builder.build();
	}
	
	public static Profession fromProto(Disciplines.Profession p) {
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
	
	private static class Holder {
		private static final EnumMap<Discipline, Texture> DISCIPLINE_ICONS = 
	            new EnumMap<Discipline, Texture>(Discipline.class);
		static {
		    DISCIPLINE_ICONS.put(Discipline.WARFARE, getTexture("discipline-warfare"));
		    DISCIPLINE_ICONS.put(Discipline.AUTOMATA, getTexture("discipline-automata"));
		    DISCIPLINE_ICONS.put(Discipline.SUBTERFUGE, getTexture("discipline-subterfuge"));
		    DISCIPLINE_ICONS.put(Discipline.CHARISMA, getTexture("discipline-charisma"));
		}
	}
	
	private static Texture getTexture(String asset) {
	    return GameScreen.getTexture("icon/" + asset + ".png");
	}
}
