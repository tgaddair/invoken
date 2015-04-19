package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.eldritch.invoken.proto.Actors.Terminal;
import com.google.protobuf.TextFormat;

public class TerminalMarshaller extends AssetMarshaller<Terminal> {
	@Override
	protected String getAssetDirectory() {
		return "terminals";
	}

	@Override
	protected Terminal readFromBinary(InputStream is) throws IOException {
		return Terminal.parseFrom(is);
	}
	
	@Override
    protected Terminal readFromText(InputStream is) throws IOException {
	    Terminal.Builder builder = Terminal.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }
}
