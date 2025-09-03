package vn.cser21.incoming;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import vn.cser21.MainActivity;
import vn.cser21.R;

public class CallingMeetingActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView time;
    private final Handler myHandler = new Handler();
    private double startTime = 0;
    private boolean isCompleted = false;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_meeting);
        time = findViewById(R.id.tvTime);
        stopService(new Intent(this, IncomingCallNotificationService.class));

        findViewById(R.id.llAccept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myHandler.removeCallbacksAndMessages(null);
                Intent intent = new Intent(CallingMeetingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getIntent().getExtras().getString("URL_MP3");
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            isCompleted = true;
                            finish();
                        }
                    });
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    startTime = mediaPlayer.getCurrentPosition();
                    time.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                            startTime)))
                    );
                    myHandler.postDelayed(UpdateSongTime, 100);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 500L);

    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            time.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String duration = String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                toMinutes((long) startTime)));
        int durationInt = Integer.parseInt(duration);
        if (!isCompleted) {
            EventBus.getDefault().post(new CallNotEndEvent(
                    getIntent().getExtras().getInt("ID", 0), durationInt
            ));
        }
        myHandler.removeCallbacksAndMessages(null);
        stopMediaPlayer();

    }

    private void stopMediaPlayer() {
        if (mediaPlayer == null) return;
        mediaPlayer.pause();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}