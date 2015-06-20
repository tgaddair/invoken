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
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

public abstract class CollectedAssetTable<T extends Message> extends IdentifiedAssetTable<T> {
    private static final long serialVersionUID = 1L;

    public CollectedAssetTable(String[] columnNames, String assetName) {
        super(columnNames, assetName);
        importAssets();
    }

    @Override
    public void saveAsset(Optional<T> prev, T asset) {
        exportAsset(asset);
        if (prev.isPresent() && !getAssetId(prev.get()).equals(getAssetId(asset))) {
            // Do not replace the asset if the ID was changed -> create a new asset
            prev = Optional.<T> absent();
        }
        addAsset(prev, asset);
    }

    @Override
    protected void handleDeleteAsset(int row) {
        T asset = getModel().getAsset(row);
        int dialogResult = JOptionPane.showConfirmDialog(null, "Delete asset " + getAssetId(asset)
                + "?", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            super.handleDeleteAsset(row);

            // No exceptions - do the delete
            delete(asset);
        }
    }
    
    protected void importAssets() {
        Set<String> ids = new HashSet<>();
        importAssets("pbtxt", ids);
        importAssets("dat", ids);
    }
    
    protected void exportAsset(T asset) {
        writeText();
    }

    protected void delete(T asset) {
        writeText();
    }
    
    protected void writeText() {
        String filename = getTextFilename(getAssetDirectory(), getAssetDirectory());
        try (DataOutputStream os = new DataOutputStream(new FileOutputStream(filename))) {
            os.writeBytes(TextFormat.printToString(collect(getAssets())));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                List<T> assets = deserialize(assetFile);
                for (T asset : assets) {
                    addAsset(asset);
                    ids.add(id);
                }
            }
        }
    }
    
    protected abstract Message collect(List<T> assets);

    protected abstract List<T> readFromBinary(InputStream is) throws IOException;

    protected abstract List<T> readFromText(InputStream is) throws IOException;

    protected List<T> deserialize(File assetFile) {
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

    protected abstract String getAssetDirectory();

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
