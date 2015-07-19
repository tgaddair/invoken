/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.eldritch.invoken.ui;

import static com.badlogic.gdx.math.Interpolation.fade;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

/** Keeps track of an application's tooltips.
 * @author Nathan Sweet */
public class TooltipManager {
    static private TooltipManager instance;
    static private Application app;

    /** Seconds from when an actor is hovered to when the tooltip is shown. Default is 2. */
    public float initialTime = 2;
    /** Once a tooltip is shown, this is used instead of {@link #initialTime}. Default is 0. */
    public float subsequentTime = 0;
    /** Seconds to use {@link #subsequentTime}. Default is 1.5. */
    public float resetTime = 1.5f;
    /** If false, tooltips will not be shown. Default is true. */
    public boolean enabled = true;
    /** If false, tooltips will be shown without animations. Default is true. */
    public boolean animations = true;

    final Array<Tooltip> shown = new Array();
    float maxWidth = Integer.MAX_VALUE;

    float time = initialTime;
    final Task resetTask = new Task() {
        public void run () {
            time = initialTime;
        }
    };

    Tooltip showTooltip;
    final Task showTask = new Task() {
        public void run () {
            if (showTooltip == null) return;

            Stage stage = showTooltip.targetActor.getStage();
            if (stage == null) return;
            stage.addActor(showTooltip.table);
            showTooltip.table.toFront();
            shown.add(showTooltip);

            showTooltip.table.clearActions();
            showAction(showTooltip);

            if (!showTooltip.instant) {
                time = subsequentTime;
                resetTask.cancel();
            }
        }
    };

    public void touchDown (Tooltip tooltip) {
        showTask.cancel();
        if (tooltip.table.remove()) resetTask.cancel();
        resetTask.run();
        if (enabled || tooltip.always) {
            showTooltip = tooltip;
            Timer.schedule(showTask, time);
        }
    }

    public void enter (Tooltip tooltip) {
        showTooltip = tooltip;
        showTask.cancel();
        if (enabled || tooltip.always) {
            if (time == 0 || tooltip.instant)
                showTask.run();
            else
                Timer.schedule(showTask, time);
        }
    }

    public void hide (Tooltip tooltip) {
        showTooltip = null;
        showTask.cancel();
        if (tooltip.table.hasParent()) {
            shown.removeValue(tooltip, true);
            hideAction(tooltip);
            resetTask.cancel();
            Timer.schedule(resetTask, resetTime);
        }
    }

    /** Called when tooltip is shown. Default implementation sets actions to animate showing. */
    protected void showAction (Tooltip tooltip) {
        float actionTime = animations ? (time > 0 ? 0.5f : 0.15f) : 0.1f;
        tooltip.table.getColor().a = 0.2f;
        tooltip.table.setScale(0.05f);
        tooltip.table.addAction(parallel(fadeIn(actionTime, fade), scaleTo(1, 1, actionTime, Interpolation.fade)));
    }

    /** Called when tooltip is hidden. Default implementation sets actions to animate hiding and to remove the actor from the stage
     * when the actions are complete. A subclass must at least remove the actor. */
    protected void hideAction (Tooltip tooltip) {
        tooltip.table.addAction(sequence(parallel(alpha(0.2f, 0.2f, fade), scaleTo(0.05f, 0.05f, 0.2f, Interpolation.fade)),
            removeActor()));
    }

    public void hideAll () {
        for (Tooltip tooltip : shown)
            tooltip.hide();
        shown.clear();
    }

    /** Shows all tooltips on hover without a delay for {@link #resetTime} seconds. */
    public void instant () {
        time = 0;
        showTask.run();
        showTask.cancel();
    }

    /** The maximum width of a tooltip. The tooltip text will wrap if needed. Default is Integer.MAX_VALUE. */
    public void setMaxWidth (float maxWidth) {
        this.maxWidth = maxWidth;
    }

    static public TooltipManager getInstance () {
        if (app == null || app != Gdx.app) {
            app = Gdx.app;
            instance = new TooltipManager();
        }
        return instance;
    }
}