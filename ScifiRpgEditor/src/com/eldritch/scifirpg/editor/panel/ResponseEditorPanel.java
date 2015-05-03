package com.eldritch.scifirpg.editor.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;
import com.eldritch.invoken.proto.Outcomes.Outcome;
import com.eldritch.invoken.proto.Prerequisites.Prerequisite;
import com.eldritch.scifirpg.editor.AssetTablePanel;
import com.eldritch.scifirpg.editor.tables.AssetPointerTable;
import com.eldritch.scifirpg.editor.tables.OutcomeTable;
import com.eldritch.scifirpg.editor.tables.PrerequisiteTable;
import com.eldritch.scifirpg.editor.tables.ResponseTable;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ResponseEditorPanel extends AssetEditorPanel<Response, ResponseTable> {
    private static final long serialVersionUID = 1L;

    private final DialogueEditorPanel dialoguePanel;
    private final JTextField idField = new JTextField();
    private final JTextArea textField = createArea(true, 30, new Dimension(100, 100));
    private final JCheckBox greetingCheck = new JCheckBox();
    private final JCheckBox forcedCheck = new JCheckBox();
    private final JTextField weightField = new JTextField("0");
    private final PrerequisiteTable prereqTable = new PrerequisiteTable();
    private final OutcomeTable outcomeTable = new OutcomeTable();
    private final AssetPointerTable<Choice> choiceTable;

    public ResponseEditorPanel(ResponseTable owner, DialogueEditorPanel dialoguePanel,
            JFrame frame, Optional<Response> prev) {
        super(owner, frame, prev);
        this.dialoguePanel = dialoguePanel;

        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        builder.appendColumn("right:pref");
        builder.appendColumn("3dlu");
        builder.appendColumn("fill:max(pref; 100px)");

        builder.append("ID:", idField);
        builder.nextLine();

        builder.append("Text:", textField);
        builder.nextLine();

        builder.append("Greeting:", greetingCheck);
        builder.nextLine();

        builder.append("Forced:", forcedCheck);
        builder.nextLine();

        builder.append("Weight:", weightField);
        builder.nextLine();

        builder.append("Prerequisites:", new AssetTablePanel(prereqTable));
        builder.nextLine();

        builder.append("Outcomes:", new AssetTablePanel(outcomeTable));
        builder.nextLine();

        builder.appendRow("fill:120dlu");
        choiceTable = new AssetPointerTable<Choice>(dialoguePanel.getChoiceTable());
        builder.append("Choices:", new AssetTablePanel(choiceTable));
        builder.nextLine();

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        builder.append(saveButton);
        builder.nextLine();

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        builder.append(deleteButton);
        builder.nextLine();

        if (prev.isPresent()) {
            init(prev.get());
        }

        add(builder.getPanel());
        setPreferredSize(new Dimension(650, 750));
    }

    private void init(Response resp) {
        prereqTable.clearAssets();
        outcomeTable.clearAssets();
        choiceTable.clearAssets();

        idField.setText(resp.getId());
        textField.setText(resp.getText());
        greetingCheck.setSelected(resp.getGreeting());
        forcedCheck.setSelected(resp.getForced());
        weightField.setText(resp.getWeight() + "");
        for (Prerequisite asset : resp.getPrereqList()) {
            prereqTable.addAsset(asset);
        }
        for (Outcome asset : resp.getOutcomeList()) {
            outcomeTable.addAsset(asset);
        }
        for (String choiceId : resp.getChoiceIdList()) {
            choiceTable.addAssetId(choiceId);
        }
    }

    @Override
    protected void save() {
        super.save();
        dialoguePanel.handleSaveAction();
    }

    protected void delete() {
        if (getPrev().isPresent()) {
            getTable().deleteAsset(getPrev().get());
            dialoguePanel.handleSaveAction();
        }
    }

    @Override
    public Response createAsset() {
        // sort by ascending weight
        List<Choice> choices = new ArrayList<>(choiceTable.getAssets());
        Collections.sort(choices, new Comparator<Choice>() {
            @Override
            public int compare(Choice c1, Choice c2) {
                return Double.compare(c1.getWeight(), c2.getWeight());
            }
        });
        List<String> choiceIds = Lists.transform(choices, new Function<Choice, String>() {
            @Override
            public String apply(Choice choice) {
                return choice.getId();
            }
        });

        String id = idField.getText();
        String text = textField.getText();
        boolean greeting = greetingCheck.isSelected();
        return Response.newBuilder().setId(id).setText(text).setGreeting(greeting)
                .setForced(forcedCheck.isSelected())
                .setWeight(Integer.parseInt(weightField.getText()))
                .addAllPrereq(prereqTable.getAssets()).addAllOutcome(outcomeTable.getAssets())
                .addAllChoiceId(choiceIds).build();
    }
}
