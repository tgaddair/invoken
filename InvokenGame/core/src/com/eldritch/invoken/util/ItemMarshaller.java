package com.eldritch.invoken.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.eldritch.invoken.proto.Items.Item;
import com.google.protobuf.TextFormat;

public class ItemMarshaller extends AssetMarshaller<Item> {
    @Override
    protected String getAssetDirectory() {
        return "items";
    }

    @Override
    protected Item readFromBinary(InputStream is) throws IOException {
        return Item.parseFrom(is);
    }
    
    @Override
    protected Item readFromText(InputStream is) throws IOException {
        Item.Builder builder = Item.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }
}
