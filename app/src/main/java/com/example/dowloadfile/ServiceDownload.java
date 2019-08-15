package com.example.dowloadfile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import android.os.IBinder;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import com.downloader.PRDownloader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class ServiceDownload extends Service {
    private static final String CHANEL_ID = "Chanel ID";
    public ArrayList<ObItemjDownload> arrayList;

    @Override
    public IBinder onBind(final Intent intent) {

        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();


    }
    private void getData(){
        SharedPreferences shared_preference =getSharedPreferences("shared preference",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = shared_preference.getString("obItemjDownloads",null);
        Type type = new TypeToken<ArrayList<ObItemjDownload>>(){}.getType();
        arrayList = gson.fromJson(json,type);

        if(arrayList == null){
            arrayList = new ArrayList<>();
        }
        for (int i=0;i<arrayList.size();i++){
            Log.d("123123",arrayList.get(i).getId()+"");
        }

    }

    private void saveData(){
        SharedPreferences shared_preference =getSharedPreferences("shared preference",MODE_PRIVATE);
        SharedPreferences.Editor editor = shared_preference.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("obItemjDownloads",json);
        editor.apply();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotifiChanel();
        Intent intentService = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentService, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        if(intent !=null){
            ArrayList<ObItemjDownload> arrayList = intent.getParcelableArrayListExtra("obItemjDownloads");
            getArrayList(arrayList);

        }
        getData();

//        new AysnService().execute();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    public class AysnService extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            saveData();
            for (int i =0;i<arrayList.size();i++){
                if(arrayList.get(i).getPercent()<100){
                    Log.d("12344444",arrayList.get(i).getPercent()+"");
                }
            }
            return null;

        }
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

    public ArrayList<ObItemjDownload> getArrayList(ArrayList<ObItemjDownload> arrayList) {
        return arrayList;
    }
}
