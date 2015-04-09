package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.eldritch.invoken.InvokenGame;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.Message;

public abstract class AssetMarshaller<T extends Message> {
    private final LoadingCache<String, T> items = CacheBuilder.newBuilder().build(
            new CacheLoader<String, T>() {
                public T load(String assetId) {
                    FileHandle file = Gdx.files.internal(getTextFilename(getAssetDirectory(),
                            assetId));
                    if (file.exists()) {
                        return readTextAsset(file);
                    } else {
                        file = Gdx.files.internal(getBinaryFilename(getAssetDirectory(), assetId));
                        return readBinaryAsset(file);
                    }
                }
            });

    public T readAsset(String assetId) {
        try {
            return items.get(assetId);
        } catch (ExecutionException e) {
            InvokenGame.error("Failed to read: " + assetId, e);
            return null;
        }
    }

    private T readBinaryAsset(FileHandle handle) {
        try (InputStream is = handle.read()) {
            return readFromBinary(is);
        } catch (IOException ex) {
            InvokenGame.error("Failed reading " + handle.name(), ex);
            return null;
        }
    }

    private T readTextAsset(FileHandle handle) {
        try (InputStream is = handle.read()) {
            return readFromText(is);
        } catch (IOException ex) {
            InvokenGame.error("Failed reading " + handle.name(), ex);
            return null;
        }
    }

    private String getBinaryFilename(String directory, String id) {
        return String.format("%s/%s/%s.dat", getTopAssetDirectory(), directory, id);
    }

    private String getTextFilename(String directory, String id) {
        return String.format("%s/%s/%s.pbtxt", getTopAssetDirectory(), directory, id);
    }

    private String getTopAssetDirectory() {
        return "data";
    }

    protected abstract String getAssetDirectory();

    protected abstract T readFromBinary(InputStream is) throws IOException;

    protected abstract T readFromText(InputStream is) throws IOException;
}
