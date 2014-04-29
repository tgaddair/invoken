package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;

import com.eldritch.scifirpg.proto.Factions.Faction;

public class FactionMarshaller extends AssetMarshaller<Faction> {
    @Override
    protected String getAssetDirectory() {
        return "factions";
    }

    @Override
    protected Faction readFrom(InputStream is) throws IOException {
        return Faction.parseFrom(is);
    }
}
