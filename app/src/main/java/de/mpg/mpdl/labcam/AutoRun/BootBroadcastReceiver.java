package de.mpg.mpdl.labcam.AutoRun;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

/**
 * Created by yingli on 12/10/15.
 */
// WakefulBroadcastReceiver ensures the device does not go back to sleep
// during the startup of the service
public class BootBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Launch the specified service when this message is received

        Intent startServiceIntent = new Intent(context, AutoRunService.class);
        context.startService(startServiceIntent);
        Toast.makeText(context, "AutoRunService has started!", Toast.LENGTH_LONG)
                .show();
    }
}
