package vn.cser21;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import vn.cser21.incoming.Foreground;
import vn.cser21.incoming.IncomingCallActivity;
import vn.cser21.incoming.IncomingCallNotificationService;
import vn.cser21.incoming.IncomingEvent;


//https://github.com/jirawatee/FirebaseCloudMessaging-Android/blob/master/app/src/main/java/com/example/fcm/MyFirebaseMessagingService.java
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        Map<String, String> data = remoteMessage.getData();
        Log.d("onMessageReceived", "data " + data);
    }

    @Override
    public void handleIntent(@NonNull Intent intent) {
        String type = intent.getStringExtra("type");
        if (type == null || type.isEmpty()) {
            sendNotification(intent);
            return;
        }
        sendNotificationCall(intent);
    }

    private void sendNotificationCall(Intent intent) {
        String type = intent.getStringExtra("type");
        String url = intent.getStringExtra("url_mp3");
        String idstr = intent.getStringExtra("NOTI_ID");
        int id = intent.getIntExtra("NOTI_ID", 0);
        if (type == null || type.isEmpty()) {
            return;
        }

        if (Foreground.instance.isForeground()) {
            EventBus.getDefault().post(new IncomingEvent(url, Integer.parseInt(idstr)));
            return;
        }

        Intent serviceIntent = new Intent(this, IncomingCallNotificationService.class);
        serviceIntent.putExtra("URL_MP3", url);
        serviceIntent.putExtra("ID", Integer.parseInt(idstr));
        stopService(serviceIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        } else {
            startService(serviceIntent);
        }

    }

    public static Bitmap getImageUrl(String _url) {
        try {
            URL url = new URL(_url);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception e) {
            return null;
        }
    }

    private PendingIntent createPendingIntent(Intent intent) {
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    public void sendNotification(Intent intentNoti) {
        if (intentNoti.getStringExtra("gcm.notification.body") == null) return;

        // cái này là lấy nội dung từ trên server trả về
        String content = intentNoti.getStringExtra("gcm.notification.body").toString();
        String title = intentNoti.getStringExtra("gcm.notification.title").toString();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtras(intentNoti.getExtras());

        PendingIntent pendingIntent = createPendingIntent(intent);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(title)
                .setLargeIcon(icon)
                //.setColor(Color.RED)
                //.setNumber(badgeCount)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_stat_name);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("channel description");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }
        // muốn tạo nhiều notif thì phải cho thằng notification manager noti đến nhiều channel,
        // không được trùng nhau
        // vì vậy hàm new Random().nextInt() >> để tạo ra 1 channel ngẫu nhiên
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
        Log.e("On Click", "On Click");
    }

    /**
     * Create and show a custom notification containing the received FCM message.
     *
     * @param data FCM notification payload received.
     * @param data FCM data payload received.
     */
    public void sendNotification(Map<String, String> data) {

        // cái này là lấy nội dung từ trên server trả về
        String content = data.containsKey("body") ? data.get("body") : "";
        String title = data.containsKey("title") ? data.get("title") : "";

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        for (String key : data.keySet()) {
            intent.putExtra(key, data.get(key));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(title)
                .setLargeIcon(icon)
                //.setColor(Color.RED)
                //.setNumber(badgeCount)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);


        //data -> customize -> display noti
        //1
        String smallIcon = data.containsKey("smallIcon") ? data.get("smallIcon") : null;
        if (smallIcon != null && !"".equals(smallIcon)) {
            //It's not possible to set a custom small icon,
        }

        //2
        String largeIconUrl = data.containsKey("largeIcon") ? data.get("largeIcon") : null;
        if (largeIconUrl != null && !"".equals(largeIconUrl) && !"ic_launcher".equals(largeIconUrl)) {
            Bitmap b = getImageUrl(largeIconUrl);
            if (b != null) notificationBuilder.setLargeIcon(b);
        }

        //3
        String color = data.containsKey("color") ? data.get("color") : null;
        int _Color = getResources().getColor(R.color.colorPrimary);
        if (color != null && !"".equals(color)) {
            _Color = Color.parseColor(color);
            notificationBuilder.setColor(_Color);
        }

        //4
        String light_color = data.containsKey("light_color") ? data.get("light_color") : null;
        int color_light = getResources().getColor(R.color.colorPrimary);
        if (light_color != null && !"".equals(light_color)) {
            color_light = Color.parseColor(light_color);
        }

        //5
        String light_onMs = data.containsKey("light_onMs") ? data.get("light_onMs") : null;
        int onMs = 1000;
        if (light_onMs != null && !"".equals(light_onMs)) {
            onMs = Integer.parseInt(light_onMs);
        }

        //6
        int offMs = 300;
        String light_offMs = data.containsKey("light_offMs") ? data.get("light_offMs") : null;
        if (light_offMs != null && !"".equals(light_offMs)) {
            offMs = Integer.parseInt(light_offMs);
        }

        notificationBuilder.setLights(color_light, onMs, offMs);

        //7
        String vibrate = data.containsKey("vibrate") ? data.get("vibrate") : null;
        int _vibrate = Notification.DEFAULT_VIBRATE;
        if (vibrate != null && !"".equals(vibrate) && !"DEFAULT_VIBRATE".equals(vibrate)) {
            _vibrate = Integer.parseInt(vibrate);
            notificationBuilder.setDefaults(_vibrate);
        }


        //8
        String sound = data.containsKey("sound") ? data.get("sound") : null;
        Uri _sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (sound != null && !"".equals(sound) && !"TYPE_NOTIFICATION".equals(sound)) {
            _sound = Uri.parse(sound);
        }


        try {
            String picture_url = data.get("picture_url");
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(content)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("channel description");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(_Color);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }
        // muốn tạo nhiều notif thì phải cho thằng notification manager noti đến nhiều channel,
        // không được trùng nhau
        // vì vậy hàm new Random().nextInt() >> để tạo ra 1 channel ngẫu nhiên
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
        Log.e("On Click", "On Click");
    }

    //03/03/2022
    //hungnt
    @Override
    public void onNewToken(String token) {
        Log.d("firebase token", "Refreshed token: " + token);

        String name = this.getPackageName();
        SharedPreferences sharedPref = this.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("FirebaseNotiToken", token);
        editor.commit();
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        // sendRegistrationToServer(token);
    }
}