package com.eldritch.invoken.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	protected Location readFromBinary(InputStream is) throws IOException {
		return Location.parseFrom(is);
	}
	
	@Override
    protected Location readFromText(InputStream is) throws IOException {
        Location.Builder builder = Location.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
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
