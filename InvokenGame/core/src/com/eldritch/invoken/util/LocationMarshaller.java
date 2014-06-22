package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;

import com.eldritch.invoken.proto.Locations.Location;

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
