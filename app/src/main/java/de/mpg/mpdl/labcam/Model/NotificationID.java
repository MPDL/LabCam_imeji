package de.mpg.mpdl.labcam.Model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yingli on 7/7/16.
 */
public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }
}
