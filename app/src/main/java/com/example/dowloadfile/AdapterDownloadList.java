package com.example.dowloadfile;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ProgressBar progressBar;
        final Button btnDownload;
        final TextView tvName, tvDownloadPerSize, tvStatus;
        final ImageView ivIcon;
        final int[] downloadId = {0};
        final long[] process = {0};
        view = layoutInflater.inflate(R.layout.item_download, null);
        progressBar = view.findViewById(R.id.progressBar);
        btnDownload = view.findViewById(R.id.btnDownload);
        tvName = view.findViewById(R.id.tvName);
        tvDownloadPerSize = view.findViewById(R.id.tvDownloadPerSize);
        ivIcon = view.findViewById(R.id.ivIcon);

        final ParamDownload paramDownload = new ParamDownload(progressBar, btnDownload, tvName, tvDownloadPerSize, ivIcon);
        progressBar.setProgress((int) data.get(i).getPercent());
        if (Status.PAUSED == PRDownloader.getStatus(data.get(i).getId())) {
//            btStatus.setEnabled(true);
            btnDownload.setText("Resume");
        }

        if (data.get(i).getPercent() == 100) {
            btnDownload.setEnabled(false);
            btnDownload.setText("Completed");
        }

        final String nameOfFile = URLUtil.guessFileName(data.get(i).getUrl(), null,
                MimeTypeMap.getFileExtensionFromUrl(data.get(i).getUrl()));
        paramDownload.getTvName().setText(nameOfFile);
        final String typeFile = nameOfFile.substring(nameOfFile.lastIndexOf(".") + 1);
        switch (typeFile) {
            case "mp4":
                paramDownload.getIvIcon().setImageResource(R.drawable.mp4);
                break;
            case "mp3":
                paramDownload.getIvIcon().setImageResource(R.drawable.mp3);
                break;
            case "png":
                paramDownload.getIvIcon().setImageResource(R.drawable.png);
                break;
            case "jpg":
                paramDownload.getIvIcon().setImageResource(R.drawable.jpg);
                break;
        }

        updateDownloadPerSize(tvDownloadPerSize,data.get(i).getPercent(),data.get(i).getSize());

        setEvents(nameOfFile, paramDownload, i, downloadId, process);

        return view;
    }


    private void setEvents(final String nameOfFile, final ParamDownload paramDownload, final int position, final int[] downloadId, final long[] process) {
//        requestDownload(progressBar, btStatus, btCancel, position, downloadId, process);
        for (int i = 0; i < data.size(); i++) {
//            PRDownloader.pause(data.get(i).getId());
            if (position == i && data.get(i).getPercent() < 100) {
                if (Status.PAUSED != PRDownloader.getStatus(data.get(i).getId())) {
                    PRDownloader.pause(data.get(i).getId());
                    requestDownload(nameOfFile, paramDownload, i, downloadId, process);
                }

            }
        }
//        btCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                PRDownloader.cancel(downloadId[0]);
//            }
//        });
        paramDownload.getBtnDownload().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDownload(nameOfFile, paramDownload, position, downloadId, process);
            }
        });

    }

    private void requestDownload(final String nameOfFile, final ParamDownload paramDownload, final int position, final int[] downloadId, final long[] process) {

        if (Status.RUNNING == PRDownloader.getStatus(downloadId[0])) {
            PRDownloader.pause(downloadId[0]);
            return;
        }

        if (Status.PAUSED == PRDownloader.getStatus(downloadId[0])) {
            PRDownloader.resume(downloadId[0]);
            return;
        }
        downloadId[0] = PRDownloader.download(data.get(position).getUrl(), data.get(position).getDir().substring(10), nameOfFile + "")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
//                        progressBar.setIndeterminate(false);
                        paramDownload.getBtnDownload().setText("Pause");
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        paramDownload.getBtnDownload().setText("Resume");
                        paramDownload.getProgressBar().setProgress((int) data.get(position).getPercent());
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        paramDownload.getProgressBar().setProgress(0);
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                        paramDownload.getProgressBar().setProgress((int) progressPercent);
                        updateDownloadPerSize(paramDownload.getTvDownloadPerSize(), progressPercent, progress.totalBytes);
                        communicationAdapter.getPosition(position);
                        communicationAdapter.getInfoDownload(downloadId[0], progressPercent, progress.totalBytes);
//
                        if (Status.PAUSED == PRDownloader.getStatus(downloadId[0])) {
                            process[0] = progressPercent;
                            communicationAdapter.getInfoDownload(downloadId[0], progressPercent, progress.totalBytes);
//                            updateDownloadPerSize(paramDownload.getTvDownloadPerSize(),progressPercent,progress.totalBytes);
                        }
                    }


                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        paramDownload.getBtnDownload().setEnabled(false);
                        paramDownload.getBtnDownload().setText("Completed");
                        paramDownload.getProgressBar().setProgress(100);
                        data.get(position).setPercent(100);
                        Log.d("123123", "Succesful");
//                        Log.d("123123", positionComplete+"");
                        Toast.makeText(context, "Succesful Download ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("123123", "Error : " + error.getServerErrorMessage());
                        Toast.makeText(context, "Error : " + error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void updateDownloadPerSize(TextView tvDownloadPerSize, long percent, long size) {
        if (size >= (1024 * 1024)) {
            tvDownloadPerSize.setText((percent * size / (1024 * 1024)) / 100 + "MB" + " / " + size / (1024 * 1024) + "MB");
        } else if (size >= 1024) {
            tvDownloadPerSize.setText((percent * size / (1024)) / 100 + "KB" + " / " + size / 1024 + "KB");
        } else if (size < 1024) {
            tvDownloadPerSize.setText(percent * size / 100 + "Byte" + " / " + size + "Byte");
        }

    }

    private void checkTypeFile(ParamDownload paramDownload) {
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