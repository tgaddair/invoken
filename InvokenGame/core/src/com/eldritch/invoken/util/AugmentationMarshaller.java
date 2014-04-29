package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;

import com.eldritch.scifirpg.proto.Augmentations.Augmentation;

public class AugmentationMarshaller extends AssetMarshaller<Augmentation> {
    @Override
    protected String getAssetDirectory() {
        return "augmentations";
    }

    @Override
    protected Augmentation readFrom(InputStream is) throws IOException {
        return Augmentation.parseFrom(is);
    }
}
