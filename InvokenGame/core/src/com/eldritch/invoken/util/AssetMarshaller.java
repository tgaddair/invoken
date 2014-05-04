package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.eldritch.invoken.InvokenGame;
import com.google.protobuf.Message;

public abstract class AssetMarshaller<T extends Message> {
	public T readAsset(String assetId) {
	    FileHandle file = Gdx.files.internal(getFilename(getAssetDirectory(), assetId));
		return readAsset(file);
	}
	
	private T readAsset(FileHandle handle) {
		try {
			InputStream is = handle.read();
			try {
				return readFrom(is);
			} finally {
			    is.close();
			}
		} catch (IOException ex) {
			Gdx.app.error(InvokenGame.LOG, "Failed reading " + handle.name(), ex);
			return null;
		}
	}
	
	private String getFilename(String directory, String id) {
		return String.format("%s/%s/%s.dat", getTopAssetDirectory(), directory, id);
	}
	
	private String getTopAssetDirectory() {
		return "data";
	}
	
	protected abstract String getAssetDirectory();
	
	protected abstract T readFrom(InputStream is) throws IOException;
}
