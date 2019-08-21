package com.example.dowloadfile;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ParamDownload {
    private final ProgressBar progressBar;
    private final Button btnDownload;
    private final TextView tvName,tvDownloadPerSize;
    private final ImageView ivIcon;

    public ParamDownload(ProgressBar progressBar, Button btnDownload, TextView tvName, TextView tvDownloadPerSize, ImageView ivIcon) {
        this.progressBar = progressBar;
        this.btnDownload = btnDownload;
        this.tvName = tvName;
        this.tvDownloadPerSize = tvDownloadPerSize;
        this.ivIcon = ivIcon;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Button getBtnDownload() {
        return btnDownload;
    }


    public TextView getTvName() {
        return tvName;
    }


    public TextView getTvDownloadPerSize() {
        return tvDownloadPerSize;
    }

    public ImageView getIvIcon() {
        return ivIcon;
    }

}
