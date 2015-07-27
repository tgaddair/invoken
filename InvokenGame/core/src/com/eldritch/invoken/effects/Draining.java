package com.eldritch.invoken.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.eldritch.invoken.actor.aug.Drain;
import com.eldritch.invoken.actor.type.Agent;
import com.eldritch.invoken.util.Damage;
import com.eldritch.invoken.util.Utils;

public class Draining extends AnimatedEffect {
    private final Damage damage;
    private final Vector2 contact;
    private final float duration;
    private boolean cancelled = false;

    /**
     * @param actor
     *            caster, the one to be healed
     * @param target
     *            the one to be damaged
     * @param magnitude
     *            damage per second
     * @param duration
     *            seconds of continuous draining
     */
    public Draining(Agent target, Damage damage, Vector2 contact, float duration) {
        super(target, target.getRenderPosition(), Drain.SPLASH_REGIONS, Vector2.Zero, 0,
                Animation.PlayMode.LOOP, 0.1f, Utils.getWidth(Drain.SPLASH_REGIONS[0], 4), Utils
                        .getHeight(Drain.SPLASH_REGIONS[0], 4));
        this.contact = contact;
        this.damage = damage;
        this.duration = duration;
    }

    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isFinished() {
        return getStateTime() > duration || cancelled;
    }

    @Override
    public void update(float delta) {
        // transfer life essence
        float damaged = getTarget().damage(damage, contact, delta);
        damage.getSource().heal(damaged);
    }

    @Override
    public void doApply() {
    }

    @Override
    public void dispel() {
    }
}
