package com.example.dowloadfile;

import android.content.Context;
import android.widget.ProgressBar;

public interface CommunicationAdapter {
    void getInfoDownload(int id, long percent);
    void getPosition(int position);
}
