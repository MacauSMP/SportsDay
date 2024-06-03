package org.macausmp.sportsday.customize.animation;

/**
 * Represents an animation for victory dance.
 */
public abstract class Animation {
    private boolean cancel = false;

    public abstract void run();

    public abstract void stop();

    public boolean isCancelled() {
        return cancel;
    }

    public void cancel() {
        this.cancel = true;
    }
}
