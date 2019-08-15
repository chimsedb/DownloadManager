package com.example.dowloadfile;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;

import com.downloader.Progress;
import com.downloader.Status;

import java.util.List;


public class AdapterDownloadList extends BaseAdapter {

    private List<ObItemjDownload> data;
    private Context context;
    private LayoutInflater layoutInflater;

    CommunicationAdapter communicationAdapter;

    public AdapterDownloadList(List<ObItemjDownload> data, Context context, CommunicationAdapter communicationAdapter) {
        this.data = data;
        this.context = context;
        this.communicationAdapter = communicationAdapter;
        layoutInflater = LayoutInflater.from(context);
    }

    private static String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();


    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        notifyDataSetChanged();
        final ProgressBar progressBar;
        final Button btCancel, btStatus;
        final int[] downloadId = {0};
        final long[] process = {0};
        view = layoutInflater.inflate(R.layout.iteam_download, null);
        progressBar = view.findViewById(R.id.progress);
        btCancel = view.findViewById(R.id.bt_cancel);
        btStatus = view.findViewById(R.id.bt_status);

        progressBar.setProgress((int) data.get(i).getPercent());
        if (Status.PAUSED == PRDownloader.getStatus(data.get(i).getId())) {
            btStatus.setEnabled(true);
            btStatus.setText("Resume");
        }
        if (data.get(i).getPercent() == 100) {
            btStatus.setEnabled(false);
            btStatus.setText("Completed");
        }

        setEvents(progressBar, btStatus, btCancel, i, downloadId, process);

        return view;
    }


    private void setEvents(final ProgressBar progressBar, final Button btStatus, final Button btCancel, final int position, final int[] downloadId, final long[] process) {
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRDownloader.cancel(downloadId[0]);
            }
        });
        btStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDownload(progressBar, btStatus, btCancel, position, downloadId, process);
            }
        });

    }

    private void requestDownload(final ProgressBar progressBar, final Button btStatus, final Button btCancel, final int position, final int[] downloadId, final long[] process) {

        if (Status.RUNNING == PRDownloader.getStatus(downloadId[0])) {
            PRDownloader.pause(downloadId[0]);
            return;
        }

        if (Status.PAUSED == PRDownloader.getStatus(downloadId[0])) {
            PRDownloader.resume(downloadId[0]);
            return;
        }
        downloadId[0] = PRDownloader.download(data.get(position).getUrl(), dirPath, position + "")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
//                        progressBar.setIndeterminate(false);
                        btStatus.setText("Pause");
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        btStatus.setText("Resume");
                        progressBar.setProgress((int) data.get(position).getPercent());
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        progressBar.setProgress(0);
                        btStatus.setText("Start");
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                        progressBar.setProgress((int) progressPercent);
                        communicationAdapter.getPosition(position);
                        communicationAdapter.getInfoDownload(downloadId[0], progressPercent);
//

                        if (Status.PAUSED == PRDownloader.getStatus(downloadId[0])) {
                            process[0] = progressPercent;
                            communicationAdapter.getInfoDownload(downloadId[0], progressPercent);
                        }
                    }


                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        btStatus.setEnabled(false);
                        btStatus.setText("Completed");
                        communicationAdapter.getInfoDownload(downloadId[0], 100);
                        Log.d("123123", "Succesful");
//                        communicationAdapter.removeDownload(position);
                        Toast.makeText(context, "Succesful Download ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("123123", "Error : " + error.getServerErrorMessage());
                        Toast.makeText(context, "Error : " + error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

}