package com.example.dowloadfile;

import android.os.Parcel;
import android.os.Parcelable;

public class ObItemjDownload implements Parcelable{

    private String url;
    private int id;
    private long percent;
    private String dir;
    private long size;
    //Contructor

    public ObItemjDownload(String url, int id, long percent, String dir, long size) {
        this.url = url;
        this.id = id;
        this.percent = percent;
        this.dir = dir;
        this.size = size;
    }

    protected ObItemjDownload(Parcel in) {
        url = in.readString();
        id = in.readInt();
        percent = in.readLong();
        dir = in.readString();
        size = in.readLong();
    }

    public static final Creator<ObItemjDownload> CREATOR = new Creator<ObItemjDownload>() {
        @Override
        public ObItemjDownload createFromParcel(Parcel in) {
            return new ObItemjDownload(in);
        }

        @Override
        public ObItemjDownload[] newArray(int size) {
            return new ObItemjDownload[size];
        }
    };

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    //Getter and Setter
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getPercent() {
        return percent;
    }

    public void setPercent(long percent) {
        this.percent = percent;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeInt(id);
        parcel.writeLong(percent);
        parcel.writeString(dir);
        parcel.writeLong(size);
    }
}
