package com.wode369.videodemo.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wode369.videodemo.R;
import com.wode369.videodemo.adapter.LocalVideoAdapter;
import com.wode369.videodemo.bean.LocalVideoBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class VideoLocalAllActivity extends AppCompatActivity {
    private static final String TAG = "VideoLocalAllActivity";
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_local_all);
        ButterKnife.bind(this);

        LocalVideoAdapter videoAdapter = new LocalVideoAdapter(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(videoAdapter);

        List<LocalVideoBean> localVideoList = getLocalAllVideo();

       /* Log.i(TAG, "onCreate: size=" + localVideoList.size());
        for (LocalVideoBean videoBean : localVideoList) {
            Log.i(TAG, "onCreate: localVideoList getPath=" + videoBean.getPath());
            Log.i(TAG, "onCreate: localVideoList getThumbPath=" + videoBean.getThumbPath());
            Log.i(TAG, "onCreate: localVideoList getDuration=" + videoBean.getDuration());
        }*/

        if (localVideoList != null && localVideoList.size() > 0) {
            videoAdapter.setNewData(localVideoList);
        } else {
            videoAdapter.setNewData(null);
            videoAdapter.setEmptyView(R.layout.comment_empty_view, (ViewGroup) mRecyclerView.getParent());
            View emptyView = videoAdapter.getEmptyView();
            TextView tvTip = emptyView.findViewById(R.id.tv_tip);
            tvTip.setText("没有扫描到您的本地视频！");
        }


        videoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                LocalVideoBean adapterItem = videoAdapter.getItem(position);
                long duration = adapterItem.getDuration();
                if (duration / 1000 > 15) {
                    Toast.makeText(VideoLocalAllActivity.this, "视频太大，暂不支持上传", Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent();
                    intent.putExtra("video_path", adapterItem.getPath());
                    intent.putExtra("poster_path", adapterItem.getThumbPath());
                    setResult(11, intent);
                    finish();
                    //上传视频
                    /*File videoFile = new File(adapterItem.getPath());
                    File posterFile = new File(adapterItem.getThumbPath());
                    downloadImage(videoFile, posterFile);*/
                }
            }
        });

    }


    /**
     * 重要说明：
     * <p>
     * 疑问一：
     * 该方法，查到的 视频路径 和 视频的缩略图 是一样的，并且格式都是 .mp4 。视频的缩略图应该是 .jpg或.png的后缀啊，
     * 为什么会这样呢？抱歉，本人查了不少资料，还不知道具体原因。如果您知道原因，欢迎留言，评论。
     * <p>
     * <p>
     * 疑问二：
     * 除此之外，还有一个问题就是：明明手机的本地是有很多视频，但是有的机型就是查不到。
     * <p>
     * 亲测：小米2s，本地是很多视频的，但是获取不到，要么只查到几个，
     * 而 小米8，魅族note8，就可以查到本地所有的视频。
     * <p>
     * 我又下载了 抖音和快手 app，他们也是存在同样的情况：
     * 小米2s中，该方法从本地查到了2个视频，他们也是只查到了2个视频，其实本地是不止2个视频的。
     * <p>
     * 至于为什么有的机型，查不到本地所有的视频呢？ 抱歉，同样也是不清楚具体原因。如果您知道原因，欢迎留言，评论。
     */
    private List<LocalVideoBean> getLocalAllVideo() {

        List<LocalVideoBean> sysVideoList = new ArrayList<>();
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
       /* String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID};*/

        // 视频其他信息的查询条件
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Thumbnails.DATA};

        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, MediaStore.Video.VideoColumns.DATE_ADDED + " DESC");

        if (cursor == null) {
            return sysVideoList;
        }
        if (cursor.moveToFirst()) {
            do {
                LocalVideoBean info = new LocalVideoBean();

                info.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                info.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
                info.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
                info.setThumbPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)));

                sysVideoList.add(info);
            } while (cursor.moveToNext());

        }
        cursor.close();

        return sysVideoList;


    }


    public void downloadImage(final File videoFile, File posterFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Context context = getApplicationContext();
                    FutureTarget<Bitmap> target = Glide.with(context)
                            .asBitmap()
                            .load(posterFile)
                            .submit();

                    final Bitmap imageBit = target.get();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void uploadFileToObs(File file, Bitmap bitmap) {

        ArrayList<String> urls = new ArrayList<>();

        Observable.just(urls)
                .observeOn(Schedulers.io())
                .map(new Func1<ArrayList<String>, ArrayList<String>>() {

                    @Override
                    public ArrayList<String> call(ArrayList<String> urls) {
                        return new ArrayList<>();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> list) {


                    }

                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError: e" + e.getMessage());
                    }

                });


    }
}
