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
import android.os.AsyncTask;
import android.os.Build;
import android.os.NetworkOnMainThreadException;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SERVER_NOTI {
    Context context;


    public SERVER_NOTI(Context context) {
        this.context = context;
    }

    public void run(final Result result, final Callback21 callback21) {

        Gson gson = new Gson();
        SERVER_NOTI_Config config = gson.fromJson(result.params, SERVER_NOTI_Config.class);
        runBackground(context,config, callback21);
    }


    public static void noti(Noti21 noti21, Context context) {
        String content = noti21.notification.body;
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Map<String, String> data = noti21.data;

        int noti_id = 001;

        if (data == null) data = new HashMap<String, String>();

        if (data.containsKey("noti_id")) noti_id = Integer.parseInt(data.get("noti_id"));
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Intent intent = new Intent(APP.getAppContext(), MainActivity.class);

        if (data != null)
            for (String key : data.keySet()) {
                intent.putExtra(key, data.get(key));
            }


        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "channel_id")
                .setContentTitle(noti21.notification.title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(noti21.notification.title)
                .setLargeIcon(icon)
                //.setColor(Color.RED)
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
            largeIconUrl = DownloadFilesTask.tryDecodeUrl(largeIconUrl);
            Bitmap b = MyFirebaseMessagingService.getImageUrl(largeIconUrl);
            if (b != null) notificationBuilder.setLargeIcon(b);
        }

        //3
        String color = data.containsKey("color") ? data.get("color") : null;
        int _Color = context.getResources().getColor(R.color.colorPrimary);
        if (color != null && !"".equals(color)) {
            _Color = Color.parseColor(color);
            notificationBuilder.setColor(_Color);
        }

        //4
        String light_color = data.containsKey("light_color") ? data.get("light_color") : null;
        int color_light = context.getResources().getColor(R.color.colorPrimary);
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
        if (sound != null && !"".equals(sound) && sound != "TYPE_NOTIFICATION") {
            _sound = Uri.parse(sound);
        }


        try {
            String picture_url = data.get("picture_url");
            if (picture_url != null && !"".equals(picture_url)) {
                picture_url = DownloadFilesTask.tryDecodeUrl(picture_url);
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(content)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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

        notificationManager.notify(noti_id, notificationBuilder.build());
    }

    public static void runBackground(final  Context context,final SERVER_NOTI_Config config, final Callback21 callback21) {
        if (context == null || config == null) return;
        ;

        final AsyncTask execute = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                String _url = "";
                String step = "";
                try {
                    _url = WebControl.toUrlWithsParams(config.server, config.serverParams);
                    step += "->url:" + _url;
                    InputStream in = null;
                    URL url = new URL(_url);
                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection) url.openConnection();
                    step += "->conn";
                    // 2. Open InputStream to connection
                    conn.connect();
                    in = conn.getInputStream();
                    step += "->getInputStream";
                    byte[] bytes = IOUtils.toByteArray(in);
                    String str = new String(bytes, "UTF-8");

                    try{
                        Response rsp = new Gson().fromJson(str, Response.class);
                        if (rsp != null && rsp.success) {


                            if(rsp.data != null){
                                //noti
                                if ( rsp.data.notis != null) {
                                    for (Noti21 noti21 : rsp.data.notis) {
                                        noti(noti21, context);
                                    }
                                }
                                //detech_location
                                if(rsp.data.detechLocation != null && rsp.data.detechLocation.enable){
                                    new Loction21(context).SendTo(rsp.data.detechLocation.receiver);
                                }
                            }


                        }



                        if (callback21 != null)
                            callback21.ok();
                    }catch (Exception ex){
                        if (callback21 != null)
                        {
                            callback21.lastExp = ex;
                            callback21.no();
                        }
                    }


                } catch (NetworkOnMainThreadException netError) {
                    Log.i("NetThreadException:", step);
                    netError.printStackTrace();
                } catch (IOException e) {


                    e.printStackTrace();
                    if (callback21 != null) {
                        callback21.lastExp = e;
                        callback21.no();
                    }
                }
                return null;
            }
        };

        execute.execute();

    }


    public class Data {
        List<Noti21> notis;
        DetectLocation detechLocation;
    }

    class Response {
        public Boolean success;
        public Data data;
    }

    class DetectLocation{
        boolean enable;
        String receiver;
    }


}

class SERVER_NOTI_Config {
    public boolean enable;
    public int intervalMillis = 1000 * 60 * 15;
    public String server;
    public Map<String, String> serverParams;
}
