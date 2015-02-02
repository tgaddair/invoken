package com.eldritch.scifirpg.editor.util;

import java.util.HashMap;

import com.eldritch.invoken.proto.Actors.DialogueTree;
import com.eldritch.invoken.proto.Actors.DialogueTree.Choice;
import com.eldritch.invoken.proto.Actors.DialogueTree.Response;

public class DialogueConverter {
	public static DialogueTree convert(DialogueTree oldTree) {
		DialogueTree.Builder builder = DialogueTree.newBuilder();
		
		// extract all the deprecated choices and assign ids
		HashMap<String, Choice> choices = new HashMap<>();
		for (Response oldResponse : oldTree.getDialogueList()) {
			Response.Builder response = Response.newBuilder(oldResponse);
			
			for (Choice oldChoice : oldResponse.getChoiceDEPRECATEDList()) {
				Choice.Builder choice = Choice.newBuilder(oldChoice);
				choice.setId(truncate(oldChoice.getText()));
				choices.put(choice.getId(), choice.build());
				response.addChoiceId(choice.getId());
			}
			
			builder.addDialogue(response.build());
		}
		
		// add the uniquely identified choices
		for (Choice choice : choices.values()) {
			builder.addChoice(choice);
		}
		
		return builder.build();
	}
	
	public static String collapse(String in) {
		return truncate(in, true);
	}
	
	public static String truncate(String in) {
		return truncate(in, false);
	}
	
	private static String truncate(String in, boolean useSuffix) {
		int end = in.length();
		String suffix = "";
		int spaces = 0;
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i) == ' ') {
				spaces++;
			}
			if (spaces == 5) {
				end = i;
				if (useSuffix) {
					suffix = "...";
				}
				break;
			}
		}
		return in.substring(0, end) + suffix;
	}
	
	private DialogueConverter() {}
}
