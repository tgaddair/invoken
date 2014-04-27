package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;

import com.eldritch.scifirpg.proto.Actors.NonPlayerActor;

public class ActorMarshaller extends AssetMarshaller<NonPlayerActor> {
    @Override
    protected String getAssetDirectory() {
        return "actors";
    }

    @Override
    protected NonPlayerActor readFrom(InputStream is) throws IOException {
        return NonPlayerActor.parseFrom(is);
    }
}