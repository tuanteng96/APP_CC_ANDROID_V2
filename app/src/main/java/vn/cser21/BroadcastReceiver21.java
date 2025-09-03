package vn.cser21;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastReceiver21 extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
           new AlarmReceiver21().setAlarm(context);
        }
        if (intent.getAction().equals("android.intent.action.ACTION_PACKAGE_FULLY_REMOVED")) {
            Log.e("TAG", "ACTION_PACKAGE_FULLY_REMOVED");
        }
    }

}
