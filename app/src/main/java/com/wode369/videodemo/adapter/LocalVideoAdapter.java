package com.wode369.videodemo.adapter;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wode369.videodemo.R;
import com.wode369.videodemo.bean.LocalVideoBean;
import com.wode369.videodemo.utils.TimeUtils;
import com.wode369.videodemo.utils.VideoThumbUtils;

import java.io.File;
import java.util.List;

/**
 * Created by ypk on 2019/2/23 0023  10:32
 * <p>
 * Description:
 */
public class LocalVideoAdapter extends BaseQuickAdapter<LocalVideoBean, BaseViewHolder> {


    public LocalVideoAdapter(@Nullable List<LocalVideoBean> data) {
        super(R.layout.item_local_video, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, LocalVideoBean item) {

        TextView tvTip = helper.getView(R.id.video_tip);

        ImageView imageView = helper.getView(R.id.image_view);

        RequestOptions options = new RequestOptions();
        options.error(R.drawable.default_image)
                .placeholder(R.drawable.default_image)
                .override(130, 130);


        //说明：虽然获取的视频缩略图的路径，但是真实返回的还是一个视频的路径
        //因此，这里的file其实是一个视频文件
        File file = new File(item.getThumbPath());

        //glid虽然加载的为视频file，但是只会把视频的第一帧的图片加载显示出来
        //如果这里不想用glide，去显示图片，也可以通过media.getFrameAtTime()方法，获取视频的第一帧，
        // 参考文章地址：https://blog.csdn.net/bzlj2912009596/article/details/80446256
        Glide.with(mContext)
                .load(Uri.fromFile(file))//这个方法在华为手机上，没问题。

                //经过测试发现，在华为手机上，这个方法的加载，直接把手机卡死了，小米8未出现同样问题，
                //初步判断是太耗内存，上面的方法就没问题。
                //.load(new File(item.getThumbPath()))

                .apply(options)
                .into(imageView);


        //bitmap 太大，加载卡顿，不理想
        //Bitmap videoThumbBit = VideoThumbUtils.getVideoThumb(item.getThumbPath());
        //Bitmap videoThumbBit = VideoThumbUtils.getVideoThumbnail(item.getThumbPath(), 100, 100);
       /* Glide.with(mContext)
                .load(videoThumbBit)
                .apply(options)
                .into(imageView);*/


        helper.setText(R.id.video_duration, TimeUtils.convertSecondsToTime(item.getDuration() / 1000));

        if (item.getDuration() / 1000 > 15) {
            tvTip.setVisibility(View.VISIBLE);
        } else {
            tvTip.setVisibility(View.GONE);
        }

    }


}
