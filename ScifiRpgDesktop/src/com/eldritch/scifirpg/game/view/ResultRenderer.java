package com.eldritch.scifirpg.game.view;

import com.eldritch.scifirpg.game.util.Result;

public class ResultRenderer {
    public static String render(Result result) {
        return result.toString();
    }
    
    private ResultRenderer() {}
}
