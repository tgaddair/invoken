package com.eldritch.scifirpg.editor.tables;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

public abstract class IdentifiedAssetTable<T extends Message> extends AssetTable<T> {
	private static final long serialVersionUID = 1L;

	public IdentifiedAssetTable(String[] columnNames, String assetName) {
		super(columnNames, assetName);
	}
	
	public List<String> getAssetIds() {
		List<String> ids = new ArrayList<>();
		for (T asset : getAssets()) {
			ids.add(getAssetId(asset));
		}
		return ids;
	}
	
	public T getAssetFor(String id) {
		for (T asset : getAssets()) {
			if (getAssetId(asset).equals(id)) {
				return asset;
			}
		}
		return null;
	}
	
	protected abstract String getAssetId(T asset);
}
