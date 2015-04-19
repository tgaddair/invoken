package com.eldritch.scifirpg.editor.tables;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.Terminal;
import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.panel.AssetEditorPanel;
import com.google.common.base.Optional;
import com.google.protobuf.TextFormat;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class TerminalTable extends MajorAssetTable<Terminal> {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = { "ID", "Dialogue" };

    public TerminalTable() {
        super(COLUMN_NAMES, "Terminal");
    }

    @Override
    protected JPanel getEditorPanel(Optional<Terminal> prev, JFrame frame) {
        return new EditorPanel(this, frame, prev);
    }

    @Override
    protected Object[] getDisplayFields(Terminal asset) {
        String dialogue = asset.getDialogueCount() + "";
        return new Object[] { asset.getId(), dialogue };
    }

    @Override
    protected String getAssetDirectory() {
        return "terminals";
    }

    @Override
    protected Terminal readFromBinary(InputStream is) throws IOException {
        return Terminal.parseFrom(is);
    }

    @Override
    protected Terminal readFromText(InputStream is) throws IOException {
        Terminal.Builder builder = Terminal.newBuilder();
        TextFormat.merge(new InputStreamReader(is), builder);
        return builder.build();
    }

    @Override
    protected String getAssetId(Terminal asset) {
        return asset.getId();
    }

    private class EditorPanel extends AssetEditorPanel<Terminal, TerminalTable> {
        private static final long serialVersionUID = 1L;

        private final JTextField idField = new JTextField();
        private final DialogueTable dialogueTable = new DialogueTable();

        public EditorPanel(TerminalTable owner, JFrame frame, Optional<Terminal> prev) {
            super(owner, frame, prev);

            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
            builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            builder.appendColumn("right:pref");
            builder.appendColumn("3dlu");
            builder.appendColumn("fill:max(pref; 100px)");

            builder.append("ID:", idField);
            builder.nextLine();

            builder.appendRow("fill:p:grow");
            builder.append("Dialogue:", new AssetTablePanel(dialogueTable));
            builder.nextLine();

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(this);
            builder.append(saveButton);
            builder.nextLine();

            if (prev.isPresent()) {
                Terminal asset = prev.get();
                idField.setText(asset.getId());
                for (DialogueTree dialogue : asset.getDialogueList()) {
                    dialogueTable.addAsset(dialogue);
                }
            }

            add(builder.getPanel());
            setPreferredSize(new Dimension(650, 750));
        }

        @Override
        public Terminal createAsset() {
            return Terminal.newBuilder().setId(idField.getText())
                    .addAllDialogue(dialogueTable.getAssets()).build();
        }
    }
}
