package com.eldritch.scifirpg.game.util;

import java.io.IOException;
import java.io.InputStream;

import com.eldritch.scifirpg.proto.Locations.Location;

public class LocationMarshaller extends AssetMarshaller<Location> {
	@Override
	protected String getAssetDirectory() {
		return "locations";
	}

	@Override
	protected Location readFrom(InputStream is) throws IOException {
		return Location.parseFrom(is);
	}
}
