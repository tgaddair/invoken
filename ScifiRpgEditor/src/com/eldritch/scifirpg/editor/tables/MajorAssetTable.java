package com.eldritch.scifirpg.editor.tables;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;

import com.google.common.base.Optional;
import com.google.protobuf.Message;

public abstract class MajorAssetTable<T extends Message> extends IdentifiedAssetTable<T> {
	private static final long serialVersionUID = 1L;

	public MajorAssetTable(String[] columnNames, String assetName) {
		super(columnNames, assetName);
		importAssets();
	}
	
	@Override
	public void saveAsset(Optional<T> prev, T asset) {
		exportAsset(asset);
		if (prev.isPresent() && !getAssetId(prev.get()).equals(getAssetId(asset))) {
			// Do not replace the asset if the ID was changed -> create a new asset
			prev = Optional.<T>absent();
		}
		addAsset(prev, asset);
	}
	
	@Override
	protected void handleDeleteAsset(int row) {
		T asset = getModel().getAsset(row);
		int dialogResult = JOptionPane.showConfirmDialog(null,
				"Delete asset " + getAssetId(asset) + "?", "Warning", JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			super.handleDeleteAsset(row);
			
			// No exceptions - do the delete
			delete(asset);
		}
	}
	
	protected abstract String getAssetDirectory();
	
	protected abstract T readFrom(InputStream is) throws IOException;
	
	protected T deserialize(File assetFile) {
		try (FileInputStream fis = new FileInputStream(assetFile)) {
			return readFrom(fis);
		} catch (IOException e) {
			return null;
		}
	}
	
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
	
	protected void exportAsset(T asset) {
		write(asset, getAssetDirectory(), getAssetId(asset));
	}

	protected void write(T asset, String directory, String id) {
		String filename = getFilename(directory, id);
		try (DataOutputStream os = new DataOutputStream(new FileOutputStream(filename))) {
			os.write(asset.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void delete(T asset) {
		String filename = getFilename(getAssetDirectory(), getAssetId(asset));
		try {
			File file = new File(filename);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected String getFilename(String directory, String id) {
		return String.format("%s/%s/%s.dat", getTopAssetDirectory(), directory, id);
	}
	
	public static String getTopAssetDirectory() {
		return "../InvokenGame/android/assets/data";
	}
}
