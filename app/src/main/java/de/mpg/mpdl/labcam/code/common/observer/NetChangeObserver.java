package de.mpg.mpdl.labcam.code.common.observer;



/**
 * Created by yingli on 12/8/15.
 */
public interface NetChangeObserver {

    /**
     * call when connect
     */
    public void OnConnect();

    /**
     * when disconnect
     */
    public void OnDisConnect();
}
