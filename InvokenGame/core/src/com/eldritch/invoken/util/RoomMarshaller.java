package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.eldritch.invoken.proto.Locations.Room;
import com.google.protobuf.TextFormat;

public class RoomMarshaller extends AssetMarshaller<Room> {
	@Override
	protected String getAssetDirectory() {
		return "rooms";
	}

	@Override
	protected Room readFromBinary(InputStream is) throws IOException {
		return Room.parseFrom(is);
	}
	
	@Override
    protected Room readFromText(InputStream is) throws IOException {
        Room.Builder builder = Room.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }
}
