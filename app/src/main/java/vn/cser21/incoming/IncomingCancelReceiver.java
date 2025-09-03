package vn.cser21.incoming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IncomingCancelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null) {
            context.stopService(new Intent(context, IncomingCallNotificationService.class));
        }
    }
}
