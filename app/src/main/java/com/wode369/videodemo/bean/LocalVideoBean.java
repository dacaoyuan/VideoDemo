package com.wode369.videodemo.bean;

import android.graphics.Bitmap;

/**
 * Created by ypk on 2019/2/23 0023  10:33
 * <p>
 * Description:
 */
public class LocalVideoBean {
    private String path;
    private long duration;
    private long size;
    private String thumbPath;
    private Bitmap bitmap;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
