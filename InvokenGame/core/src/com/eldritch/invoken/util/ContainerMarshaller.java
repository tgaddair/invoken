package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.eldritch.invoken.proto.Actors.Container;
import com.google.protobuf.TextFormat;

public class ContainerMarshaller extends AssetMarshaller<Container> {
	@Override
	protected String getAssetDirectory() {
		return "containers";
	}

	@Override
	protected Container readFromBinary(InputStream is) throws IOException {
		return Container.parseFrom(is);
	}
	
	@Override
    protected Container readFromText(InputStream is) throws IOException {
	    Container.Builder builder = Container.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }
}
