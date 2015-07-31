package com.eldritch.invoken.actor.factions;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.eldritch.invoken.actor.Profession;
import com.eldritch.invoken.actor.type.HumanNpc;
import com.eldritch.invoken.location.Level;
import com.eldritch.invoken.location.layer.LocationMap;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.eldritch.invoken.proto.Actors.PlayerActor;
import com.eldritch.invoken.proto.Disciplines;
import com.eldritch.invoken.proto.Factions;
import com.eldritch.invoken.proto.Factions.Faction.Rank;
import com.eldritch.invoken.proto.Locations;
import com.eldritch.invoken.proto.Locations.Encounter.ActorParams.ActorScenario;
import com.eldritch.invoken.util.GameTransition;
import com.eldritch.invoken.util.GameTransition.GameState;
import com.eldritch.invoken.util.GameTransition.GameTransitionHandler;
import com.eldritch.invoken.util.Settings;
import com.google.common.base.Optional;

public class FactionManagerTest {
	private FactionManager manager;
	private Faction faction;

	@Before
	public void setUp() {
		manager = new FactionManager(new DummyNpc(0, 0, FakeLevel.newInstance()));
		faction = createTestFaction();
	}

	@Test
	public void testHasReputation() {
		// initially, our rank is 0, corresponding to no rank
		assertEquals(0, manager.getRank(faction));
		
		// similarly, our reputation should be 0
		assertEquals(0, manager.getReputation(faction));
		
		// now let's increase our reputation with the faction
		manager.modifyReputationFor(faction, 20);
		
		// verify that we now have positive reputation with the faction
		assertEquals(20, manager.getReputation(faction));
		
		// ... but check that we still have no official rank
		assertEquals(0, manager.getRank(faction));
	}

	private static class FakeLevel extends Level {
		private FakeLevel(Locations.Level data, LocationMap map) {
			super(data, map, new GameTransition(dummyHandler, new Skin()), 0L);
		}
		
		public static Level newInstance() {
			Locations.Level.Builder builder = Locations.Level.newBuilder();
	        builder.setRegion(Settings.FIRST_REGION);
	        builder.setLevel(1);
	        
	        LocationMap map = new LocationMap(null, 25, 25);

	        Level level = new FakeLevel(builder.build(), map);
	        level.createDummyPlayer();
	        return level;
		}
	}

	private static class DummyNpc extends HumanNpc {
		private DummyNpc(float x, float y, Level level) {
			super(getNpcData(), Optional.<ActorScenario> absent(), x, y,
					"sprite/characters/light-blue-hair.png", level);
		}

		@Override
		protected void takeAction(float delta, Level screen) {
		}
	}
	
	private static final GameTransitionHandler dummyHandler = new GameTransitionHandler() {
        @Override
        public void transition(GameState prev, GameState next, PlayerActor playerState) {
        }
    };

	private static NonPlayerActor getNpcData() {
		NonPlayerActor.Builder builder = NonPlayerActor.newBuilder();
		ActorParams params = ActorParams.newBuilder().setLevel(1)
				.setProfession(Disciplines.Profession.CENTURION)
				.addAllSkill(Profession.Centurion.getSkillsFor(1)).build();
		return builder.build();
	}
	
	private static Faction createTestFaction() {
		Factions.Faction.Builder builder = Factions.Faction.newBuilder();
		builder.setId("Test").setName("Test");
		builder.addRank(Rank.newBuilder().setId(1).setTitle("Initiate"));
		builder.addRank(Rank.newBuilder().setId(3).setTitle("Member"));
		builder.addRank(Rank.newBuilder().setId(9).setTitle("Leader"));
		return new Faction(builder.build());
	}
}