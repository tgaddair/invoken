package com.eldritch.scifirpg.editor.tables;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.eldritch.scifirpg.editor.panel.ActorEditorPanel;
import com.eldritch.invoken.proto.Actors.ActorParams;
import com.eldritch.invoken.proto.Actors.NonPlayerActor;
import com.google.common.base.Optional;
import com.google.protobuf.TextFormat;

public class ActorTable extends MajorAssetTable<NonPlayerActor> {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = { 
		"ID", "Name", "Species", "Gender", "Profession", "Level", "Unique" };
	
	public ActorTable() {
		super(COLUMN_NAMES, "Actor");
	}
	
	public List<String> getUniqueAssetIds() {
		List<String> ids = new ArrayList<>();
		for (NonPlayerActor asset : getAssets()) {
			if (asset.getUnique()) {
				ids.add(asset.getParams().getId());
			}
		}
		return ids;
	}

	@Override
	protected JPanel getEditorPanel(Optional<NonPlayerActor> prev, JFrame frame) {
		return new ActorEditorPanel(this, frame, prev);
	}

	@Override
	protected Object[] getDisplayFields(NonPlayerActor asset) {
		ActorParams params = asset.getParams();
		Object gender = params.hasGender() ? params.getGender() : "";
		Object profession = params.hasProfession() ? params.getProfession() : "";
		Object unique = asset.getUnique() ? "yes" : "";
		return new Object[]{params.getId(), params.getName(), params.getSpecies(),
				gender, profession, params.getLevel(), unique};
	}

	@Override
	protected String getAssetDirectory() {
		return "actors";
	}

	@Override
	protected String getAssetId(NonPlayerActor asset) {
		return asset.getParams().getId();
	}

	@Override
	protected NonPlayerActor readFromBinary(InputStream is) throws IOException {
		return NonPlayerActor.parseFrom(is);
	}

	@Override
	protected NonPlayerActor readFromText(InputStream is) throws IOException {
		NonPlayerActor.Builder builder = NonPlayerActor.newBuilder();
		TextFormat.merge(new InputStreamReader(is), builder);
		return builder.build();
	}
}
