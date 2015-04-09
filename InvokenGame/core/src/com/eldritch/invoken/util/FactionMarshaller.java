package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.eldritch.invoken.proto.Factions.Faction;
import com.google.protobuf.TextFormat;

public class FactionMarshaller extends AssetMarshaller<Faction> {
    @Override
    protected String getAssetDirectory() {
        return "factions";
    }

    @Override
    protected Faction readFromBinary(InputStream is) throws IOException {
        return Faction.parseFrom(is);
    }
    
    @Override
    protected Faction readFromText(InputStream is) throws IOException {
        Faction.Builder builder = Faction.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }
}
