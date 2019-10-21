package com.test.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.bms_video_player.R;

import java.util.ArrayList;
import java.util.List;

import other.MovieHLS;

/**
 * 清晰度
 */
public class ClarityAdapter extends BaseAdapter {

    private Context mConext;
    private List<MovieHLS> mDatas;
    private LayoutInflater inflater;
    private int mCurrentClarity;

    public ClarityAdapter(Context mConext) {
        this.mConext = mConext;
        this.mDatas = new ArrayList<>();
        this.inflater = LayoutInflater.from(mConext);
    }

    public void update(List<MovieHLS> data, int currentClarity) {
        this.mCurrentClarity = currentClarity;
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }

    public void updateSelectClarity(int currentClarity) {
        this.mCurrentClarity = currentClarity;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_video_clarity, parent, false);
            holder.check = convertView.findViewById(R.id.check);
            holder.resolution = convertView.findViewById(R.id.resolution);
            holder.hd = convertView.findViewById(R.id.hd);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MovieHLS movieHLS = mDatas.get(position);
        if (!TextUtils.isEmpty(movieHLS.resolution) && isShowHd(movieHLS.resolution)) {
            holder.hd.setVisibility(View.VISIBLE);
        } else {
            holder.hd.setVisibility(View.INVISIBLE);
        }
        if (mCurrentClarity == Integer.parseInt(movieHLS.resolution)) {
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.check.setVisibility(View.INVISIBLE);
        }
        holder.resolution.setText(movieHLS.resolution);
        return convertView;
    }

    private boolean isShowHd(String resolution) {
        int reso = Integer.parseInt(resolution);
        if (reso > 360) {
            return true;
        }
        return false;
    }

    static class ViewHolder {
        ImageView check;
        TextView resolution;
        TextView hd;
    }
}
