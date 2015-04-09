package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.google.protobuf.TextFormat;

public class ActorMarshaller extends AssetMarshaller<NonPlayerActor> {
    @Override
    protected String getAssetDirectory() {
        return "actors";
    }

    @Override
    protected NonPlayerActor readFromBinary(InputStream is) throws IOException {
        return NonPlayerActor.parseFrom(is);
    }
    
    @Override
    protected NonPlayerActor readFromText(InputStream is) throws IOException {
        NonPlayerActor.Builder builder = NonPlayerActor.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }
}