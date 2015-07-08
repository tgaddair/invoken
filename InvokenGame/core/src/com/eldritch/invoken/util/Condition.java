package com.eldritch.invoken.util;

import com.eldritch.invoken.location.Level;

public interface Condition {
    boolean satisfied();
    
    void onReset(Level level);
}
