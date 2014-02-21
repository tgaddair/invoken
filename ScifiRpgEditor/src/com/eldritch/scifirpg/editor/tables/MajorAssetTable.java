package com.eldritch.scifirpg.editor.tables;

import java.io.File;

import com.google.protobuf.Message;

public abstract class MajorAssetTable<T extends Message> extends AssetTable<T> {
	private static final long serialVersionUID = 1L;

	public MajorAssetTable(String[] columnNames) {
		super(columnNames);
	}
	
	protected abstract String getAssetDirectory();
	
	protected abstract String getAssetId(T asset);
	
	protected abstract T deserialize(File assetFile);
	
	@Override
	protected void importAssets() {
		String path = getTopAssetDirectory() + "/" + getAssetDirectory();
		File dir = new File(path);
		for (File assetFile : dir.listFiles()) {
			T asset = deserialize(assetFile);
			if (asset != null) {
				addAsset(asset);
			}
		}
	}
	
	@Override
	protected void exportAsset(T asset) {
		write(asset, getAssetDirectory(), getAssetId(asset));
	}
}
