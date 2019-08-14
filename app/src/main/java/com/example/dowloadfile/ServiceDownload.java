package com.example.dowloadfile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.downloader.PRDownloader;

import java.util.ArrayList;

import static com.example.dowloadfile.MainActivity.obItemjDownloads;

public class ServiceDownload extends Service {
    private static final String CHANEL_ID = "Chanel ID";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    public class AsyngetInfoDownload extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (obItemjDownloads != null){
                for(int i=0;i<obItemjDownloads.size();i++){
                    if (obItemjDownloads.get(i).getPercent() < 100) {
                        PRDownloader.pause(obItemjDownloads.get(i).getId());
                    }
                    if (obItemjDownloads.get(i).getPercent() < 100) {
                        PRDownloader.resume(obItemjDownloads.get(i).getId());
                    }
//                    obItemjDownloads.set(i)
                }
            }
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new AsyngetInfoDownload().execute();
        createNotifiChanel();
        Intent intentService = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentService, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    private void createNotifiChanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANEL_ID,
                    "Chanel Download",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
