package vn.cser21.incoming;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import org.greenrobot.eventbus.EventBus;

import vn.cser21.MainActivity;
import vn.cser21.R;

public class IncomingCallNotificationService extends Service {

    private final Handler handler = new Handler();
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.pause();
        mediaPlayer.release();
        handler.removeCallbacksAndMessages(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mediaPlayer = MediaPlayer.create(this, R.raw.call);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        Intent cancelCallIntent = new Intent(this, IncomingCancelReceiver.class);
        Intent acceptCallIntent = new Intent(this, CallingMeetingActivity.class);
        acceptCallIntent.putExtra("URL_MP3", intent.getStringExtra("URL_MP3"));
        acceptCallIntent.putExtra("ID", intent.getIntExtra("ID",0));


        Intent incomingCallIntent = new Intent(this, IncomingCallActivity.class);
        incomingCallIntent.putExtra("URL_MP3", intent.getStringExtra("URL_MP3"));
        incomingCallIntent.putExtra("ID", intent.getIntExtra("ID",0));
        incomingCallIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);


        PendingIntent acceptCallPendingIntent = TaskStackBuilder.create(this)
                .addNextIntent(acceptCallIntent)
                .getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent rejectCallPendingIntent = PendingIntent.getBroadcast(this, 0, cancelCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent incomingCallPendingIntent = PendingIntent.getActivity(this, 0, incomingCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews notificationLayout = new RemoteViews(this.getPackageName(), R.layout.layout_notification);
        notificationLayout.setOnClickPendingIntent(R.id.ivAccept, acceptCallPendingIntent);
        notificationLayout.setOnClickPendingIntent(R.id.ivReject, rejectCallPendingIntent);

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCustomContentView(notificationLayout)
                .setColor(Color.WHITE)
                .setCustomBigContentView(notificationLayout)
                .setAutoCancel(false)
                .setOngoing(true)
                .setFullScreenIntent(incomingCallPendingIntent, true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .build();



        startForeground(1000, notification);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new EndCallEvent());
                stopSelf();
            }
        }, 60000L);

        incomingCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(incomingCallIntent);
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {

                NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("des");
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
