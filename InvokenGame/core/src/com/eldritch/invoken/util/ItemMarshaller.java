package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;

import com.eldritch.invoken.proto.Items.Item;

public class ItemMarshaller extends AssetMarshaller<Item> {
    @Override
    protected String getAssetDirectory() {
        return "items";
    }

    @Override
    protected Item readFrom(InputStream is) throws IOException {
        return Item.parseFrom(is);
    }
}
