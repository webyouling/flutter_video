package com.test.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.test.bms_video_player.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import other.DownLoadBean;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


/**
 * 选集
 */
public class SelectionsAdapter extends BaseAdapter {

    private Context mConext;
    private List<DownLoadBean> mDatas;
    private LayoutInflater inflater;
    private DownLoadBean mCurrentVideo;

    public SelectionsAdapter(Context mConext) {
        this.mConext = mConext;
        this.mDatas = new ArrayList<>();
        this.inflater = LayoutInflater.from(mConext);
    }

    public void update(List<DownLoadBean> data, DownLoadBean currentVideo) {
        this.mCurrentVideo = currentVideo;
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }

    public void updateSelectBean(DownLoadBean currentVideo) {
        this.mCurrentVideo = currentVideo;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_video_selections, parent, false);
            holder.relativeLayout = convertView.findViewById(R.id.video_item_bg);
            holder.avatar = convertView.findViewById(R.id.avatar);
            holder.title = convertView.findViewById(R.id.title);
            holder.views = convertView.findViewById(R.id.views);
            holder.time = convertView.findViewById(R.id.time);
            holder.resolution = convertView.findViewById(R.id.resolution);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DownLoadBean movieHLS = mDatas.get(position);
        if (isShowHd(movieHLS.downLoad_hls)) {
            holder.resolution.setBackgroundResource(R.drawable.corners_bg_textview_color_red);
        } else {
            holder.resolution.setBackgroundResource(R.drawable.corners_bg_textview_color);
        }
        if (mCurrentVideo == null) {
            holder.relativeLayout.setBackgroundResource(R.drawable.corners_bg_color_normal);
        } else {
            if (mCurrentVideo.video_id == movieHLS.video_id) {
                holder.relativeLayout.setBackgroundResource(R.drawable.corners_bg_color);
            } else {
                holder.relativeLayout.setBackgroundResource(R.drawable.corners_bg_color_normal);
            }
        }
        holder.resolution.setText(movieHLS.downLoad_hls + "p");
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(4);
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        RequestOptions options = RequestOptions.bitmapTransform(roundedCorners).override(300, 300);
        Glide.with(mConext).load(movieHLS.icon).apply(options).into(holder.avatar);
        holder.title.setText(movieHLS.name);
        holder.views.setText(getViewers(movieHLS.views) + " Views");
        holder.time.setText(getVideoTime(Long.parseLong(movieHLS.videoTime)));
        return convertView;
    }

    private boolean isShowHd(int resolution) {
        if (resolution > 360) {
            return true;
        }
        return false;
    }

    static class ViewHolder {
        RelativeLayout relativeLayout;
        ImageView avatar;
        TextView title;
        TextView views;
        TextView time;
        TextView resolution;

    }

    //获取观看数
    static String getViewers(String views) {
        String num = "0";
        if (views == null || TextUtils.isEmpty(views.trim())) return num;
        double viewer = Double.parseDouble(views);
        if (viewer >= 1000.0 && viewer < 1000000.0) {
            double video = Double.parseDouble(views) / 1000.0;
            BigDecimal bd = new BigDecimal(video);
            Double d = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            num = d.toString() + 'k';
        } else if (viewer >= 1000000.0) {
            double video = Double.parseDouble(views) / 1000000.0;
            BigDecimal bd = new BigDecimal(video);
            Double d = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            num = d.toString() + 'M';
        } else {
            num = views;
        }
        return num;
    }

    //获取视频时长
    private String getVideoTime(long duration) {
        String videotime = "0sec";
        double second = duration / 1000; //秒数
        double temp = second % 3600;
        if (second < 60) {
            videotime = (int) second + "sec";
        } else {
            if (second > 3600) {
                double hour = second / 3600; //小数
                if (temp != 0) {
                    if (temp > 60) {
                        double minute = temp / 60; //分
                        videotime = (int) hour +
                                "h" +
                                (int) minute +
                                "min";
                    }
                } else {
                    videotime = (int) hour + "h";
                }
            } else {
                double minute = second / 60; //分
                videotime = (int) minute + "min";
            }
        }
        return videotime;
    }
}
