package com.eldritch.scifirpg.editor.tables;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.EncounterEditorPanel;
import com.eldritch.invoken.proto.Locations.Encounter;
import com.eldritch.invoken.proto.Locations.EncounterCollection;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

public class EncounterTable extends CollectedAssetTable<Encounter> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Title", "Type", "Weight", "Unique" };
	
	public EncounterTable() {
		super(COLUMN_NAMES, "Encounter");
	}

	@Override
	protected JPanel getEditorPanel(Optional<Encounter> prev, JFrame frame) {
		return new EncounterEditorPanel(this, frame, prev);
	}
	
	@Override
	protected Object[] getDisplayFields(Encounter asset) {
		return new Object[]{asset.getId(), asset.getTitle(), asset.getType(),
				asset.getWeight(), asset.getUnique()};
	}

	@Override
	protected String getAssetId(Encounter asset) {
		return asset.getId();
	}

    @Override
    protected Message collect(List<Encounter> assets) {
        return EncounterCollection.newBuilder().addAllEncounter(assets).build();
    }

    @Override
    protected List<Encounter> readFromBinary(InputStream is) throws IOException {
        EncounterCollection collection = EncounterCollection.parseFrom(is);
        return collection.getEncounterList();
    }

    @Override
    protected List<Encounter> readFromText(InputStream is) throws IOException {
        EncounterCollection.Builder builder = EncounterCollection.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        EncounterCollection collection = builder.build();
        return collection.getEncounterList();
    }

    @Override
    protected String getAssetDirectory() {
        return "encounters";
    }
}
