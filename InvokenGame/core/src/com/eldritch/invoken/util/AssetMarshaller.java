package com.eldritch.invoken.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.eldritch.invoken.InvokenGame;
import com.google.protobuf.Message;

public abstract class AssetMarshaller<T extends Message> {
	public T readAsset(String assetId) {
		File file = new File(getFilename(getAssetDirectory(), assetId));
		return readAsset(file);
	}
	
	private T readAsset(File assetFile) {
		try {
			FileInputStream fis = new FileInputStream(assetFile);
			try {
				return readFrom(fis);
			} finally {
				fis.close();
			}
		} catch (IOException ex) {
			Gdx.app.error(InvokenGame.LOG, "Failed reading " + assetFile, ex);
			return null;
		}
	}
	
	private String getFilename(String directory, String id) {
		return String.format("%s/%s/%s.dat", getTopAssetDirectory(), directory, id);
	}
	
	private String getTopAssetDirectory() {
		return "C:/Users/Travis/repos/data";
	}
	
	protected abstract String getAssetDirectory();
	
	protected abstract T readFrom(InputStream is) throws IOException;
}
