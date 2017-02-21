package de.mpg.mpdl.labcam.code.rxbus;

/**
 * Created by yingli on 2/21/17.
 */

public abstract class EventSubscriber<T> {
    public abstract void onEvent(T event);
    public void onError(Throwable throwable) {

    }
}
