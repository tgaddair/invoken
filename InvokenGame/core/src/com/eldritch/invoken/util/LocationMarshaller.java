package com.eldritch.invoken.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.eldritch.invoken.proto.Locations.Location;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.protobuf.TextFormat;

public class LocationMarshaller extends AssetMarshaller<Location> {
	@Override
	protected String getAssetDirectory() {
		return "locations";
	}

	@Override
	protected Location readFrom(InputStream is) throws IOException {
		return Location.parseFrom(is);
	}
	
	public static void writeString(Location location) throws IOException {
		StringBuilder sb = new StringBuilder();
		TextFormat.print(location, sb);
		System.out.println(sb.toString());
		Files.write(
				sb.toString(), 
				new File(System.getProperty("user.home") + "/" + location.getId() + ".pbtxt"),
				Charsets.UTF_8);
	}
}
