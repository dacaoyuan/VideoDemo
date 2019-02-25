package com.wode369.videodemo.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ypk on 2019/2/23 0023  17:14
 * <p>
 * Description:
 */
public class VideoThumbUtils {

    /**
     * 获取视频文件第一帧原尺寸图片
     *
     * 返回的bitmap会比较大，不要直接进行显示
     * 建议这里对bitmap进行压缩一下，或者用glide去加载显示
     *
     * @param path 视频文件的路径
     * @return Bitmap 返回获取的Bitmap
     */

    public static Bitmap getVideoThumb(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();

        media.setDataSource(path);

        return media.getFrameAtTime();
    }

    /**
     * 获取视频的缩略图
     *  返回的bitmap会比较大，不要直接进行显示
     *  建议这里对bitmap进行压缩一下，或者用glide去加载显示
     *
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images(Video).Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height) {//, int kind
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND); //调用ThumbnailUtils类的静态方法createVideoThumbnail；获取视频的截图
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);//调用ThumbnailUtils类的静态方法 extractThumbnail 將原图片（即上方截取的图片）转化为指定大小；
        }
        return bitmap;
    }


    /**
     * Bitmap保存成File
     *
     * @param bitmap input bitmap
     * @param name   output file's name
     * @return String output file's path
     */

    public static String bitmap2File(Bitmap bitmap, String name) {

        File f = new File(Environment.getExternalStorageDirectory() + name + ".jpg");
        if (f.exists()) f.delete();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            fOut.flush();

            fOut.close();

        } catch (IOException e) {
            return null;
        }
        return f.getAbsolutePath();
    }


}
