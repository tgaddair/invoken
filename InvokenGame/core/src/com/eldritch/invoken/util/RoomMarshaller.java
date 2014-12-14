package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;

import com.eldritch.invoken.proto.Locations.Room;

public class RoomMarshaller extends AssetMarshaller<Room> {
	@Override
	protected String getAssetDirectory() {
		return "rooms";
	}

	@Override
	protected Room readFrom(InputStream is) throws IOException {
		return Room.parseFrom(is);
	}
}
