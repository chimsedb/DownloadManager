package com.example.dowloadfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.le.AdvertiseData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements CommunicationAdapter {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public ArrayList<ObItemjDownload> obItemjDownloads;
    private Button btnDownload;
    public int id = 0;
    public long process = 0;
    public int position = 0;
    public static String et_url;

    ListView listView;
    AdapterDownloadList adapterDownloadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= 23) { // Marshmallow

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOCATION_PERMISSION_REQUEST_CODE);
            }

        }

        setControllers();
        setEvents();
        adapterDownloadList.notifyDataSetChanged();
    }


    private void startServiceDownload() {
        saveData();
        Intent intent = new Intent(this, ServiceDownload.class);
        intent.putParcelableArrayListExtra("obItemjDownloads", obItemjDownloads);
        ContextCompat.startForegroundService(this, intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        new AsynService().execute();
        startServiceDownload();
    }

    public void ConfigDownload() {
        PRDownloader.initialize(getApplicationContext());
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);
    }

    private void createDialog() {
        final String url = "https://www.wallpaperup.com/uploads/wallpapers/2014/01/15/228439/9c3928b843a01ec6d3b796583a707704-1000.jpg";
//        final String url = "https://c2-sd-vdc.nixcdn.com/PreNCT10/ChapCanhUocMoKaraoke-V.A-4190063.mp4?st=zhvDR-3RgHMMBKSIDy4RgQ&e=1565930823";
//        final String url = "https://c2-sd-vdc.nixcdn.com/GoNCT1/DauDau-Isaac-6036608.mp4?st=h0Uh5zFc3yUf5hJ8HY2nmA&e=1565365868";
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        final EditText et_input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        et_input.setLayoutParams(lp);
        et_input.setText(url);

        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Alert message to be shown");
        alertDialog.setView(et_input);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        et_url = et_input.getText().toString();
                        obItemjDownloads.add(new ObItemjDownload(et_url, 0, 0));
                        new AsyncGetInfoDownload().execute();
//                        adapterDownloadList.notifyDataSetChanged();
                        dialog.dismiss();

                    }
                });
        alertDialog.show();
    }

    private void saveData() {
        SharedPreferences shared_preference = getSharedPreferences("shared preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = shared_preference.edit();
        Gson gson = new Gson();
        String json = gson.toJson(obItemjDownloads);
        editor.putString("obItemjDownloads", json);
        editor.apply();

    }

    private void getData() {
        SharedPreferences shared_preference = getSharedPreferences("shared preference", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = shared_preference.getString("obItemjDownloads", null);
        Type type = new TypeToken<ArrayList<ObItemjDownload>>() {
        }.getType();
        obItemjDownloads = gson.fromJson(json, type);

        if (obItemjDownloads == null) {
            obItemjDownloads = new ArrayList<>();
        }


    }

    private void setControllers() {
        ConfigDownload();

        getData();
        btnDownload = findViewById(R.id.btn_download);
        listView = findViewById(R.id.lv_download);

        adapterDownloadList = new AdapterDownloadList(obItemjDownloads, getApplicationContext(), this);
        listView.setAdapter(adapterDownloadList);
    }

    private void setEvents() {
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createDialog();
            }
        });

    }

    @Override
    public void getInfoDownload(int id, long process) {
        this.id = id;
        this.process = process;
        obItemjDownloads.get(position).setId(id);
        obItemjDownloads.get(position).setPercent(process);
//        Log.d("1321312",position+"");
    }

    @Override
    public void getPosition(int position) {
        this.position = position;
    }

    //    private void stopServiceDownload() {
//        Intent intent = new Intent(this,ServiceDownload.class);
//        stopService(intent);
//    }
    public class AsynService extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            saveData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapterDownloadList.notifyDataSetChanged();
        }
    }

    public class AsyncGetInfoDownload extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            for (int i = 0; i < obItemjDownloads.size(); i++) {
//                PRDownloader.resume(obItemjDownloads.get(i).getId());
                PRDownloader.pause(obItemjDownloads.get(i).getId());
            }

            saveData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapterDownloadList.notifyDataSetChanged();
        }
    }

}
