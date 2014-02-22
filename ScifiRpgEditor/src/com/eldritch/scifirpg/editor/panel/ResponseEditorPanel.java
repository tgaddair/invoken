package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.scifirpg.editor.tables.DialogueTable;
import com.eldritch.scifirpg.proto.Actors.DialogueTree.Response;
import com.google.common.base.Optional;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ResponseEditorPanel extends AssetEditorPanel<Response, DialogueTable> {
	private static final long serialVersionUID = 1L;

	private final JTextField idField = new JTextField();
	private final JTextArea textField = createArea(true, 30, new Dimension(100, 100));
	private final JCheckBox greetingCheck = new JCheckBox();

	public ResponseEditorPanel(DialogueTable owner, JFrame frame, Optional<Response> prev) {
		super(owner, frame, prev);

		DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
		builder.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		builder.appendColumn("right:pref");
		builder.appendColumn("3dlu");
		builder.appendColumn("fill:max(pref; 100px)");
		
		builder.append("ID:", idField);
		builder.nextLine();

		builder.append("Text:", textField);
		builder.nextLine();

		builder.append("Greeting:", greetingCheck);
		builder.nextLine();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		builder.append(saveButton);
		builder.nextLine();
		
		if (prev.isPresent()) {
			Response resp = prev.get();
			idField.setText(resp.getId());
			textField.setText(resp.getText());
			greetingCheck.setSelected(resp.getGreeting());
		}

		add(builder.getPanel());
		setPreferredSize(new Dimension(500, 750));
	}

	@Override
	public Response createAsset() {
		String id = idField.getText();
		String text = textField.getText();
		boolean greeting = greetingCheck.isSelected();
		return Response.newBuilder()
				.setId(id)
				.setText(text)
				.setGreeting(greeting)
				.build();
	}
}
