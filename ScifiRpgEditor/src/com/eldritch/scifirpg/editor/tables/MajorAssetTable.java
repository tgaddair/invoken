package com.eldritch.scifirpg.editor.tables;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

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
	
	protected abstract T readFromBinary(InputStream is) throws IOException;
	
	protected abstract T readFromText(InputStream is) throws IOException;
	
	protected T deserialize(File assetFile) {
		try (FileInputStream fis = new FileInputStream(assetFile)) {
			if (assetFile.getName().endsWith(".pbtxt")) {
				return readFromText(fis);
			} else {
				return readFromBinary(fis);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to deserialize: " + assetFile.getName());
			return null;
		}
	}
	
	protected void importAssets() {
		Set<String> ids = new HashSet<>();
		importAssets("pbtxt", ids);
		importAssets("dat", ids);
	}
	
	protected void importAssets(String suffix, Set<String> ids) {
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
				String.format("glob:*.{%s}", suffix));
		
		String directoryName = getTopAssetDirectory() + "/" + getAssetDirectory();
		File dir = new File(directoryName);
		for (File assetFile : dir.listFiles()) {
			String id = assetFile.getName().substring(0, assetFile.getName().lastIndexOf("."));
			if (ids.contains(id)) {
				// already added
				continue;
			}
			
			Path path = Paths.get(assetFile.getName());
			if (matcher.matches(path)) {
				T asset = deserialize(assetFile);
				if (asset != null) {
					addAsset(asset);
					ids.add(id);
				}
			}
		}
	}
	
	protected void exportAsset(T asset) {
		writeText(asset, getAssetDirectory(), getAssetId(asset));
	}

	protected void writeText(T asset, String directory, String id) {
		String filename = getTextFilename(directory, id);
		try (DataOutputStream os = new DataOutputStream(new FileOutputStream(filename))) {
			os.writeBytes(TextFormat.printToString(asset));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void writeBinary(T asset, String directory, String id) {
		String filename = getTextFilename(directory, id);
		try (DataOutputStream os = new DataOutputStream(new FileOutputStream(filename))) {
			os.write(asset.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void delete(T asset) {
		delete(getTextFilename(getAssetDirectory(), getAssetId(asset)));
		delete(getBinaryFilename(getAssetDirectory(), getAssetId(asset)));
	}
	
	protected void delete(String filename) {
		try {
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected String getTextFilename(String directory, String id) {
		return String.format("%s/%s/%s.pbtxt", getTopAssetDirectory(), directory, id);
	}
	
	protected String getBinaryFilename(String directory, String id) {
		return String.format("%s/%s/%s.dat", getTopAssetDirectory(), directory, id);
	}
	
	public static String getTopAssetDirectory() {
		return "../InvokenGame/android/assets/data";
	}
}
