package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.EncounterCollection;
import com.google.protobuf.TextFormat;

public class EncounterProvider extends AssetSelector<Encounter> {
    private final static String ENCOUNTERS = "encounters";

    public double getWeight(Encounter encounter, int level) {
        double weight = encounter.getWeight();
        if (!encounter.hasTargetLevel()) {
            return weight;
        }

        int dst = Math.abs(encounter.getTargetLevel() - level);
        if (dst == 0) {
            return weight;
        }

        double penalty = encounter.getVariance() / dst;
        return weight * penalty;
    }

    @Override
    protected void loadInto(Map<Integer, Set<Encounter>> levelToEncounter,
            Map<String, Encounter> encounters) {
        EncounterCollection collection = MARSHALLER.readAsset(ENCOUNTERS);
        for (Encounter encounter : collection.getEncounterList()) {
            if (!levelToEncounter.containsKey(encounter.getMinLevel())) {
                levelToEncounter.put(encounter.getMinLevel(), new HashSet<Encounter>());
            }
            levelToEncounter.get(encounter.getMinLevel()).add(encounter);
            encounters.put(encounter.getId(), encounter);
        }
    }

    @Override
    protected boolean isValid(Encounter encounter, int level) {
        return !encounter.hasMaxLevel() || level <= encounter.getMaxLevel();
    }

    private static final AssetMarshaller<EncounterCollection> MARSHALLER = new AssetMarshaller<EncounterCollection>() {
        @Override
        protected String getAssetDirectory() {
            return ENCOUNTERS;
        }

        @Override
        protected EncounterCollection readFromBinary(InputStream is) throws IOException {
            return EncounterCollection.parseFrom(is);
        }

        @Override
        protected EncounterCollection readFromText(InputStream is) throws IOException {
            EncounterCollection.Builder builder = EncounterCollection.newBuilder();
            TextFormat.merge(new InputStreamReader(is), builder);
            return builder.build();
        }

    };
}
