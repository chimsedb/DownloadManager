package com.example.dowloadfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
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
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import lib.folderpicker.FolderPicker;


public class MainActivity extends AppCompatActivity implements CommunicationAdapter {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public ArrayList<ObItemjDownload> obItemjDownloads;
    private Button btnDownload;
    public int id = 0;
    public long process = 0;
    public long size = 0;
    public int position = 0;
    public static String et_url;

    TextView tv_saveFile;

    ListView listView;
    AdapterDownloadList adapterDownloadList;

    private static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();

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
//        final String url = "https://scontent-lga3-1.xx.fbcdn.net/v/t66.18014-6/10000000_1548742065257032_8533859838285840384_n.mp4?_nc_cat=111&efg=eyJ2ZW5jb2RlX3RhZyI6ImRhc2hfb2VwX2hxMV9mcmFnXzJfdmlkZW8ifQ==&_nc_oc=AQn6BfyOkalbKNQDAR1bp270f0P9R6A124epG_jo77OZLRQZMu-Pr1UANNqrVRmGQhQ&_nc_ht=scontent-lga3-1.xx&oh=ac09b107187ffc250cceb07fbed5f744&oe=5DBFCF1A";
//        final String url = "https://c2-sd-vdc.nixcdn.com/GoNCT1/DauDau-Isaac-6036608.mp4?st=h0Uh5zFc3yUf5hJ8HY2nmA&e=1565365868";
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayout.setLayoutParams(params);

        Button bt_setDir = new Button(this);
        bt_setDir.setText("Choose File");
        bt_setDir.setGravity(Gravity.CENTER);

        tv_saveFile = new TextView(this);
        tv_saveFile.setText("Save In : " + dirPath);
        tv_saveFile.setTextSize(16);
        tv_saveFile.setGravity(Gravity.CENTER);

        linearLayout.addView(bt_setDir);
        linearLayout.addView(tv_saveFile, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        final EditText et_input = new EditText(this);
        et_input.setHint("Fill URL To Download");
        et_input.setText(url);

        parent.addView(linearLayout);
        parent.addView(et_input);

        alertDialog.setTitle("Download File");
        alertDialog.setView(parent);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        et_url = et_input.getText().toString();
                        boolean checkURL = true;
                        for (int i = 0; i < obItemjDownloads.size(); i++) {
                            if (et_input.getText().toString().equals(obItemjDownloads.get(i).getUrl())) {
                                checkURL = false;
                            }

                        }
                        if (checkURL == true) {
                            obItemjDownloads.add(new ObItemjDownload(et_url, 0, 0, tv_saveFile.getText().toString(), 0));
                        } else {
                            Toast.makeText(MainActivity.this, "File Was Existed", Toast.LENGTH_SHORT).show();
                        }
//                        adapterDownloadList.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

        bt_setDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FolderPicker.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            String folderLocation = intent.getExtras().getString("data");
            tv_saveFile.setText("Save In : " + folderLocation);
            Log.i("folderLocation", folderLocation);

        }
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
    public void getInfoDownload(int id, long process, long size) {
        this.id = id;
        this.process = process;
        this.size = size;
        obItemjDownloads.get(position).setId(id);
        obItemjDownloads.get(position).setPercent(process);
        obItemjDownloads.get(position).setSize(size);
        Log.d("1321312", obItemjDownloads.get(position).getPercent() + "");
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

}
