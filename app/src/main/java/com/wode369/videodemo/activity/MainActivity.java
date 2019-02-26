package com.wode369.videodemo.activity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.wode369.videocroplibrary.features.trim.VideoTrimmerActivity;
import com.wode369.videodemo.R;
import com.wode369.videodemo.bean.LocalVideoBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity123";
    @BindView(R.id.button01)
    Button button01;
    @BindView(R.id.button02)
    Button button02;
    @BindView(R.id.button03)
    Button button03;
    @BindView(R.id.tv_path)
    TextView tvPath;
    @BindView(R.id.getPermission)
    Button getPermission;

    private final static int VIDEO_REQUESTCODE = 22;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


    }

    @OnClick({R.id.button01, R.id.button02, R.id.button03})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.button01:
                intent = new Intent(MainActivity.this, VideoLocalAllActivity.class);
                startActivityIfNeeded(intent, VIDEO_REQUESTCODE);

                break;
            case R.id.button02:
                choiceVideo();

                break;
            case R.id.button03:
                intent = new Intent(MainActivity.this, VideoRecorderActivity.class);
                startActivityIfNeeded(intent, VIDEO_REQUESTCODE);

                break;
        }
    }

    //从系统相册中选择视频
    private void choiceVideo() {

        //方法一：在小米8,2s，华为荣耀4，魅族note8测试都可以
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("video/*");//选择视频 （mp4 3gp 是android支持的视频格式）
        startActivityForResult(i, 66);

        //方法二：在小米8,2s，华为荣耀4可以，但是 魅族note8 打开后，看到的都是图片，方法一，看到的都是视频
       /* Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 66);*/
    }

    /**
     * 比较重要的说明：
     * <p>
     * 测试后你可能会发现：不管是获取本地的全部视频，还是直接从系统相册选择视频，
     * 返回的 视频路径 和 视频的缩略图 是一样的。并且格式都是 .mp4 。至于为什么会这样 和  有的机型就是查不到本地的所有视频
     * 这两个疑问，在 VideoLocalAllActivity 中的getLocalAllVideo()方法上，做了说明。如果您知道原因，欢迎留言，评论。
     * <p>
     * 这里着重说，既然返回的 视频的缩略图 是 .mp4 的格式，而且和 视频路径 是一样的。
     * 那我们只关注 视频路径 这一个参数即可。
     * 现在问题就变成了，我们如何根据 视频的路径，来获取 视频的缩略图？
     * 思路：
     * 首先，知道了视频的路径，得到视频file，很简单吧。
     * 然后, 得到了视频file，获取视频的第一帧的图片，不就是视频的缩略图啦
     * <p>
     * 获取视频的第一帧的图片，
     * 方法一（推荐）：通过glide，直接去加载这个视频文件，glide自动会把视频的第一帧的图片加载显示出来
     * 需要注意的是 load 视频文件时，需要这样处理 .load(Uri.fromFile(videoFile)),
     * 不要这样 .load(videoFile) 直接去加载文件。具体原因和用法，在 VideoLocalAllActivity 中的adapter中，已做诠释！
     * <p>
     * 方法二：
     * 通过media.getFrameAtTime()方法，获取视频的第一帧，具体用法，见 VideoLocalAllActivity 中的adapter的方法。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode);

        if (resultCode == RESULT_OK && requestCode == 66) {
            if (data != null) {
                LocalVideoBean localVideoBean = getVideoPath(data);

                tvPath.setText("视频路径：\n" + localVideoBean.getPath() + "\n\n" + " 视频缩略图路径：\n" + localVideoBean.getThumbPath());
                videoCrop(localVideoBean.getPath());


            } else {
                Toast.makeText(this, "data is null", Toast.LENGTH_SHORT).show();
            }


        } else if (requestCode == VIDEO_REQUESTCODE && resultCode == 11) {

            String video_path = data.getStringExtra("video_path");
            String poster_path = data.getStringExtra("poster_path");


            tvPath.setText("视频路径：\n" + video_path + "\n\n" + " 视频缩略图路径：\n" + poster_path);
            videoCrop(video_path);

        } else if (requestCode == VIDEO_REQUESTCODE && resultCode == 22) {//拍摄的视频路径，可以返回到这里

        } else if (requestCode == VIDEO_TRIM_REQUEST_CODE) {

        }


    }

    public static final int VIDEO_TRIM_REQUEST_CODE = 0x001;
    private static final String VIDEO_PATH_KEY = "video-file-path";

    //得到视频路径，new 出视频file，我们就可以对视频进行剪裁处理了（文字，滤镜...）
    //这里推荐一个开源框架，Android-Video-Trimmer： https://github.com/iknow4/Android-Video-Trimmer 还不错。
    // 缺点是：使用ffmpeg进行视频裁剪。会让你的app增大许多,20-30M
    private void videoCrop(String videoPath) {

        if (!TextUtils.isEmpty(videoPath)) {
            Bundle bundle = new Bundle();
            bundle.putString(VIDEO_PATH_KEY, videoPath);

            Intent intent = new Intent(MainActivity.this, VideoTrimmerActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, VIDEO_TRIM_REQUEST_CODE);
        }


    }

    private LocalVideoBean getVideoPath(Intent data) {

        // 视频其他信息的查询条件
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Thumbnails.DATA};


        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, MediaStore.Video.VideoColumns.DATE_ADDED + " DESC");

        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();

        LocalVideoBean info = new LocalVideoBean();
        info.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
        info.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
        info.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
        info.setThumbPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)));

        cursor.close();

        return info;
    }


    @OnClick(R.id.getPermission)
    public void onViewClicked() {
        RxPermissions rxPermissions = new RxPermissions(this);

       /* Disposable subscribe1 = rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) {
                            // All requested permissions are granted
                            Log.i(TAG, "All requested permissions are granted");
                        } else {

                            // At least one permission is denied
                            Log.i(TAG, "At least one permission is denied");
                        }
                    }
                });*/


        Disposable subscribe2 = rxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            Log.i(TAG, "subscribe2 accept: All requested permissions are granted");
                            // `permission.name` is granted !
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // Denied permission without ask never again
                            Log.i(TAG, "subscribe2 accept: else if permission.name=" + permission.name);
                        } else {
                            Log.i(TAG, "subscribe2 accept: else permission.name=" + permission.name);
                            // Denied permission with ask never again
                            // Need to go to the settings
                        }
                    }
                });


    }
}
