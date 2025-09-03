package vn.cser21;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver21 extends BroadcastReceiver {

    //https://stackoverflow.com/questions/4459058/alarm-manager-example
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private static String shareName = "alert_config";
    public String Now() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return (dateFormat.format(date)); //2016/11/16 12:08:43
    }


   /* public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi); // Millisec * Second * Minute
    }*/

    public static void setConfig(String jsonConfig, Context context) {
        try {
            //Gson gson = new Gson();
            SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("config", jsonConfig);
            editor.apply();
        } catch (Exception e) {
        }
    }
    SERVER_NOTI_Config getConfig(Context context) {
        SERVER_NOTI_Config config = new SERVER_NOTI_Config();
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            config = gson.fromJson(sharedPreferences.getString("config", null), SERVER_NOTI_Config.class);
        } catch (Exception ex) {
        }
        return config;
    }
    public void setAlarm(Context context) {
        SERVER_NOTI_Config config = getConfig(context);
        if (config == null) return;
        if (!config.enable) {
            cancelAlarm(context);
            return;
        }
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver21.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long intervalMillis = config.intervalMillis;
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, alarmIntent);
        //
        ComponentName receiver = new ComponentName(context, BroadcastReceiver21.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver21.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        //
        ComponentName receiver = new ComponentName(context, BroadcastReceiver21.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    private static int increId = 1;
    @Override
    public void onReceive(Context context, Intent intent) {
        final SERVER_NOTI_Config config = getConfig(context);
        if (config == null) return;
        if (!config.enable) {
            return;
        }
        if (WebControl.IsNullEmpty(config.server)) return;
        final Context c = context;
        final Intent i = intent;
        SERVER_NOTI sv = new SERVER_NOTI(context);
        sv.runBackground(context,config, null);
        //demo(context);
    }
    void demo(Context context) {
        Noti21 noti21 = new Noti21();
        noti21.notification.title = "demo";
        SERVER_NOTI.noti(noti21, context);
    }


}