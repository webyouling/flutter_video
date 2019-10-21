package com.test.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.adapter.ClarityAdapter;
import com.test.adapter.SelectionsAdapter;
import com.test.bms_video_player.R;
import com.zx.flutter_download.DownloadModel;

import java.util.List;

import other.DownLoadBean;
import other.MovieHLS;

/**
 * Created by lingjianzhong on 2017/12/18.
 */

public class VideoPlayerController extends NiceVideoPlayerController
        implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
    private static final String TAG = "VideoPlayerController";

    private Activity mActivity;
    private final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    private LinearLayout mLoading;//加载动画
    private TextView mLoadText;//加载显示文本

    private LinearLayout mError;//播放错误
    private TextView mRetry;//点击重试

    private LinearLayout mTop;//顶部控制区
    private ImageView mBack;
    private TextView mTitle;//视频标题

    private RelativeLayout mDefaultMode;//默认模式
    private TextView mDefaultPosition;//视频播放当前时间
    private SeekBar mDefaultSeek;//视频播放进度条
    private TextView mDefaultTotalTime;//视频总时间
    private ImageView mFullScreen;//进入全屏
    private ImageView mDefaultPlay;//播放按钮
    private ImageView mDefaultVolume;//音量

    private LinearLayout mBottom;//全屏底部控制区
    private TextView mPosition;//视频播放当前时间
    private SeekBar mSeek;//视频播放进度条
    private TextView mTotalTime;//视频总时间
    private ImageView mShrinkScreen;//退出全屏
    private ImageView mPlayScreen;//播放按钮
    private TextView mSelections;//选集
    private TextView mClarity;//清晰度

    private RelativeLayout mFullCenter;//全屏中间布局
    private ImageView mLock;//屏幕锁
    private ImageView mPlay;//播放或暂停
    private LinearLayout mCopyAndVolume;
    private ImageView mCopy;//复制
    private ImageView mVolume;//音量

    private RelativeLayout gesture_volume_layout;// 音量控制布局
    private TextView geture_tv_volume_percentage;// 音量百分比
    private ProgressBar geture_tv_volume_percentage_progress;// 音量百分比
    private ImageView gesture_iv_player_volume;// 音量图标
    private RelativeLayout gesture_light_layout;// 亮度布局
    private TextView geture_tv_light_percentage;// 亮度百分比
    private ProgressBar geture_tv_light_percentageProgress;// 亮度百分比
    private RelativeLayout gesture_progress_layout;// 进度图标
    private TextView geture_tv_progress_time;// 播放时间进度
    private ImageView gesture_iv_progress;// 快进或快退标志

    private RelativeLayout video_condition_layout;//video列表
    private ListView condition_lv;
    private TextView emptyView;

    private boolean mIsLock = false;
    private boolean topBottomVisible;//底部是否显示
    private boolean lockVisible;//底部是否显示
    private CountDownTimer mDismissTopBottomCountDownTimer;//底部计算器
    private CountDownTimer mDismissLockCountDownTimer;//底部计算器
    private int mPlayMode = NiceVideoPlayer.MODE_NORMAL;//播放模式

    //    private SimpleOrientationEventListener mOrientationEventListener;
    private VolumeBroadcastReceiver mVolumeChangeObserver;
    private int mVolumeChange = 0;

    private DownLoadBean mCurrentVideo;//当前播放的视频
    private int mCurrentClarity = -1;//当前分辨率
    private ClarityAdapter mClarityAdapter;
    private SelectionsAdapter mSelectionsAdapter;
    private boolean isShowLayout = false;

    public LinearLayout getBottom_() {
        return mBottom;
    }

    public RelativeLayout getDefaultBottom_() {
        return mDefaultMode;
    }

    public int getCurrentClarity() {
        return mCurrentClarity;
    }

    public void unRequestOrientationEventListener() {
//        if (mOrientationEventListener != null)
//            mOrientationEventListener.disable();
        if (mVolumeChangeObserver != null) {
            mActivity.unregisterReceiver(mVolumeChangeObserver);
        }
    }

    public VideoPlayerController(Activity context) {
        super(context);
        mActivity = context;
//        requestOrientationEventListener();
        init();
        //初始化手势
        initGesture();
        initBoardCastReceiver();
    }

    private void initGesture() {
        //音量的布局
        gesture_volume_layout = findViewById(R.id.mn_gesture_volume_layout);
        geture_tv_volume_percentage = findViewById(R.id.mn_gesture_tv_volume_percentage);
        geture_tv_volume_percentage_progress = findViewById(R.id.mn_gesture_tv_volume_percentage_progress);
        gesture_iv_player_volume = findViewById(R.id.mn_gesture_iv_player_volume);
        //进度的布局
        gesture_progress_layout = findViewById(R.id.mn_gesture_progress_layout);
        geture_tv_progress_time = findViewById(R.id.mn_gesture_tv_progress_time);
        gesture_iv_progress = findViewById(R.id.mn_gesture_iv_progress);

        //亮度的布局
        gesture_light_layout = findViewById(R.id.mn_gesture_light_layout);
        geture_tv_light_percentage = findViewById(R.id.mn_geture_tv_light_percentage);
        geture_tv_light_percentageProgress = findViewById(R.id.mn_geture_tv_light_percentage_progress);

        video_condition_layout = findViewById(R.id.popup_video_condition_layout);
        condition_lv = findViewById(R.id.popup_condition_lv);
        emptyView = findViewById(R.id.emptyView);
        hideVideoListViewLayout(false);
        hideAllGestureLayout();
    }

    private void hideVideoListViewLayout(boolean isVisible) {
        video_condition_layout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        isShowLayout = isVisible;
        if (isVisible) {
            setTopBottomVisible(false);
        }
    }

    private void initBoardCastReceiver() {
        mVolumeChangeObserver = new VolumeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOLUME_CHANGED_ACTION);
        mActivity.registerReceiver(mVolumeChangeObserver, filter);
    }

    long currentMS;
    float DownX, DownY, moveX, moveY, DownMX, DownMY;
    public static final int THRESHOLD = 80;
    boolean mChangeVolume = false;
    boolean mChangePosition = false;
    boolean mChangeBrightness = false;
    protected long mGestureDownPosition;
    protected int mGestureDownVolume;
    protected float mGestureDownBrightness;
    protected long mSeekTimePosition;

    int state;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mIsLock) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Loger.e("onTouch surfaceContainer actionDown [" + this.hashCode() + "] ");
                    DownX = event.getX();//float DownX
                    DownY = event.getY();//float DownY
                    moveX = 0;
                    moveY = 0;
                    mChangeVolume = false;
                    mChangePosition = false;
                    mChangeBrightness = false;
                    currentMS = System.currentTimeMillis();//long currentMS     获取系统时间
                    break;
                case MotionEvent.ACTION_MOVE:
                    DownMX = event.getX();
                    DownMY = event.getY();
                    float deltaX = DownMX - DownX;
                    float deltaY = DownMY - DownY;
                    moveX += Math.abs(deltaX);//X轴距离
                    moveY += Math.abs(deltaY);//y轴距离
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                        if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                            if (absDeltaX >= THRESHOLD) {
                                cancelUpdateProgressTimer();
                                if (state != NiceVideoPlayer.STATE_ERROR && state != NiceVideoPlayer.STATE_COMPLETED) {
                                    mChangePosition = true;
                                    mGestureDownPosition = getCurrentPositionWhenPlaying();//当前位置
                                    Loger.e("mGestureDownPosition " + mGestureDownPosition);
                                }
                            } else {
                                if (DownX < mScreenWidth * 0.5f) {
                                    mChangeBrightness = true;
                                    WindowManager.LayoutParams lp = JZUtils.getWindow(getContext()).getAttributes();
                                    if (lp.screenBrightness < 0) {
                                        try {
                                            mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                            Log.i(TAG, "current system brightness: " + mGestureDownBrightness);
                                        } catch (Settings.SettingNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        mGestureDownBrightness = lp.screenBrightness * 255;
                                        Log.i(TAG, "current activity brightness: " + mGestureDownBrightness);
                                    }
                                } else {
                                    mChangeVolume = true;
                                    mGestureDownVolume = mNiceVideoPlayer.getVolume();//当前音量
                                }
                            }
                        }
                    }
                    if (mChangePosition) {
                        hideAllGestureLayout();
                        long totalTimeDuration = mNiceVideoPlayer.getDuration();
                        mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                        Loger.e("mSeekTimePosition " + mSeekTimePosition);
                        if (mSeekTimePosition > totalTimeDuration)
                            mSeekTimePosition = totalTimeDuration;
                        String seekTime = JZUtils.stringForTime(mSeekTimePosition);
                        String totalTime = JZUtils.stringForTime(totalTimeDuration);
                        showProgressViev(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                    }
                    if (mChangeVolume) {
                        hideAllGestureLayout();
                        deltaY = -deltaY;
                        int max = mNiceVideoPlayer.getMaxVolume();
                        int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                        mNiceVideoPlayer.setVolume(mGestureDownVolume + deltaV);
                        //dialog中显示百分比
                        int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
                        showVolumeViev(-deltaY, volumePercent);
                    }
                    if (mChangeBrightness) {
                        hideAllGestureLayout();
                        deltaY = -deltaY;
                        int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
                        WindowManager.LayoutParams params = JZUtils.getWindow(getContext()).getAttributes();
                        if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                            params.screenBrightness = 1;
                        } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                            params.screenBrightness = 0.01f;
                        } else {
                            params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
                        }
                        JZUtils.getWindow(getContext()).setAttributes(params);
                        //dialog中显示百分比
                        int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
                        showBrightnessView(brightnessPercent);
                    }
                    Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "onTouch surfaceContainer actionUp [" + this.hashCode() + "] ");
                    long moveTime = System.currentTimeMillis() - currentMS;//移动时间
                    //判断是否继续传递信号
                    hideAllGestureLayout();
                    if (moveTime > 200 && (moveX > 20 || moveY > 20)) {
                        if (mChangePosition) {
                            mNiceVideoPlayer.seekTo(mSeekTimePosition);
                            long duration = mNiceVideoPlayer.getDuration();
                            int progress = (int) mSeekTimePosition;
                            mDefaultSeek.setMax((int) duration);
                            mSeek.setMax((int) duration);
                            mDefaultSeek.setProgress(progress);
                            mSeek.setProgress(progress);
                        }
                        startUpdateProgressTimer();
                        return true; //不再执行后面的事件，在这句前可写要执行的触摸相关代码。点击事件是发生在触摸弹起后
                    }
                    break;
            }
        }
        return false;
    }

    private void showBrightnessView(int brightnessPercent) {
        gesture_light_layout.setVisibility(View.VISIBLE);
        if (brightnessPercent > 100) {
            brightnessPercent = 100;
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0;
        }
        geture_tv_light_percentage.setText(brightnessPercent + "%");
        geture_tv_light_percentageProgress.setProgress(brightnessPercent);
    }

    private void showVolumeViev(float v, int volumePercent) {
        gesture_volume_layout.setVisibility(View.VISIBLE);
        if (volumePercent <= 0) {
            gesture_iv_player_volume.setBackgroundResource(R.mipmap.ic_round_volume_off);
        } else {
            gesture_iv_player_volume.setBackgroundResource(R.mipmap.ic_round_volume_up);
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        geture_tv_volume_percentage.setText(volumePercent + "%");
        geture_tv_volume_percentage_progress.setProgress(volumePercent);
    }

    private void hideAllGestureLayout() {
        gesture_volume_layout.setVisibility(View.GONE);
        gesture_progress_layout.setVisibility(View.GONE);
        gesture_light_layout.setVisibility(View.GONE);
    }

    private void showProgressViev(float deltaX, String seekTime, long mSeekTimePosition, String totalTime, long totalTimeDuration) {
        gesture_progress_layout.setVisibility(View.VISIBLE);
        mPosition.setText(seekTime);
        mTotalTime.setText(totalTime);
        mDefaultPosition.setText(seekTime);
        mDefaultTotalTime.setText(totalTime);
        mSeek.setMax((int) totalTimeDuration);
        mDefaultSeek.setMax((int) totalTimeDuration);
        mSeek.setProgress(totalTimeDuration <= 0 ? 0 : (int) mSeekTimePosition);
        mDefaultSeek.setProgress(totalTimeDuration <= 0 ? 0 : (int) mSeekTimePosition);
        if (deltaX > 0) {
            gesture_iv_progress.setImageResource(R.drawable.jz_forward_icon);
        } else {
            gesture_iv_progress.setImageResource(R.drawable.jz_backward_icon);
        }
        geture_tv_progress_time.setText(seekTime + "/" + totalTime);
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (state == NiceVideoPlayer.STATE_PLAYING ||
                state == NiceVideoPlayer.STATE_PAUSED) {
            try {
                position = mNiceVideoPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    protected int mScreenWidth;
    protected int mScreenHeight;

    private void init() {
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        LayoutInflater.from(mActivity).inflate(R.layout.tx_video_palyer_controller, this, true);
        //顶部控制区
        mTop = findViewById(R.id.top);
        //返回小窗口模式
        mBack = findViewById(R.id.back);
        //视频标题
        mTitle = findViewById(R.id.title);

        //全屏底部控制区
        mBottom = findViewById(R.id.full_bottom);
        //视频播放当前时间
        mPosition = findViewById(R.id.full_position);
        //视频播放进度条
        mSeek = findViewById(R.id.full_seek);
        //退出全屏
        mShrinkScreen = findViewById(R.id.full_bottom_exit);
        //视频总时间
        mTotalTime = findViewById(R.id.full_total_time);
        //底部播放
        mPlayScreen = findViewById(R.id.full_bottom_play);
        //选集
        mSelections = findViewById(R.id.full_selections);
        //清晰度
        mClarity = findViewById(R.id.full_clarity);
        //中部布局
        mFullCenter = findViewById(R.id.rl_full_center);
        //锁
        mLock = findViewById(R.id.full_lock);
        //播放或暂停
        mPlay = findViewById(R.id.full_play);
        //音量和复制
        mCopyAndVolume = findViewById(R.id.full_copy_volume);
        //复制
        mCopy = findViewById(R.id.full_copy);
        //音量
        mVolume = findViewById(R.id.full_volume);

        //加载动画
        mLoading = findViewById(R.id.loading);
        mLoadText = findViewById(R.id.load_text);

        //播放错误
        mError = findViewById(R.id.error);
        //点击重试
        mRetry = findViewById(R.id.retry);

        //默认模式
        mDefaultMode = findViewById(R.id.rl_default_bottom);
        //视频播放当前时间
        mDefaultPosition = findViewById(R.id.default_position);
        //视频总时间
        mDefaultTotalTime = findViewById(R.id.default_total_time);
        //进入全屏
        mFullScreen = findViewById(R.id.default_bottom_fullscreen);
        //视频播放进度
        mDefaultSeek = findViewById(R.id.default_seek);
        //播放
        mDefaultPlay = findViewById(R.id.default_bottom_play);
        //音量
        mDefaultVolume = findViewById(R.id.default_volume);

        //设置监听
        //返回小屏模式
        mBack.setOnClickListener(this);
        mDefaultPlay.setOnClickListener(this);
        //进入全屏
        mFullScreen.setOnClickListener(this);
        //音量
        mDefaultVolume.setOnClickListener(this);
        //点击重试
        mRetry.setOnClickListener(this);
        //退出全屏
        mShrinkScreen.setOnClickListener(this);
        //全屏音量
        mVolume.setOnClickListener(this);
        //复制
        mCopy.setOnClickListener(this);
        //播放或暂停或播放完成
        mPlay.setOnClickListener(this);
        //锁
        mLock.setOnClickListener(this);
        //清晰度
        mClarity.setOnClickListener(this);
        //选集
        mSelections.setOnClickListener(this);
        //底部播放
        mPlayScreen.setOnClickListener(this);
        //进度条
        mSeek.setOnSeekBarChangeListener(this);
        mDefaultSeek.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);
        this.setOnTouchListener(this);
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);//设置视频标题
    }

    @Override
    public void setResolution(String resolution) {//设置视频分辨率
        mCurrentClarity = Integer.parseInt(resolution);
        setTextViewClarity(mCurrentClarity);
    }

    public void setDownLoadBean(DownLoadBean bean) {
        this.mCurrentVideo = bean;
    }

    @Override
    public void setLenght(long length) {
        //设置视频长度
//        mLength.setText(NiceUtil.formatTime(length));
    }

    public boolean getIsLock() {
        return mIsLock;
    }

    @Override
    public void setNiceVideoPlayer(INiceVideoPlayer niceVideoPlayer) {
        super.setNiceVideoPlayer(niceVideoPlayer);
    }

    /**
     * @param playState 播放状态：
     *                  <ul>
     *                  <li>{@link NiceVideoPlayer#STATE_IDLE}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_PREPARING}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_PREPARED}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_PLAYING}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_PAUSED}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_BUFFERING_PLAYING}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_BUFFERING_PAUSED}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_ERROR}</li>
     *                  <li>{@link NiceVideoPlayer#STATE_COMPLETED}</li>
     */
    @Override
    protected void onPlayStateChanged(int playState) {
        state = playState;
        switch (playState) {
            case NiceVideoPlayer.STATE_IDLE:
                //播放未开始
                break;
            case NiceVideoPlayer.STATE_PREPARING:
                //播放准备中
                mLoading.setVisibility(View.VISIBLE);//加载布局
                mLoadText.setText(R.string.loading);
                mError.setVisibility(View.GONE);//加载错误布局
                mFullCenter.setVisibility(View.GONE);//全屏播放布局
                mTop.setVisibility(View.GONE);//顶部控制区布局
                mBottom.setVisibility(View.GONE);//全屏底部控制区布局
                mDefaultMode.setVisibility(View.GONE);//默认模式布局
                mDefaultVolume.setVisibility(View.GONE);//默认模式音量
                mPlay.setVisibility(View.GONE);
                break;
            case NiceVideoPlayer.STATE_PREPARED:
                if (mSelectionController != null) {
                    mSelectionController.onVideoPrepared();
                }
                //播放准备就绪
                startUpdateProgressTimer();//开启进度更新
                break;
            case NiceVideoPlayer.STATE_PLAYING:
                setTopBottomVisible(true);
                //开始播放
                mLoading.setVisibility(View.GONE);//隐藏加载布局
                //改变播放按钮图标
                mPlay.setVisibility(View.INVISIBLE);
                mPlay.setImageResource(R.mipmap.ic_round_pause_circle_outline);
                mDefaultPlay.setImageResource(R.mipmap.ic_round_pause);
                mPlayScreen.setImageResource(R.mipmap.ic_round_pause);
                startDismissTopBottomTimer();
                startUpdateProgressTimer();
                break;
            case NiceVideoPlayer.STATE_PAUSED:
                //播放暂停
                mLoading.setVisibility(View.GONE);
                //改变播放按钮图标
                mPlay.setVisibility(View.VISIBLE);
                mPlay.setImageResource(R.mipmap.ic_round_play_circle_outline);
                mDefaultPlay.setImageResource(R.mipmap.ic_round_play_arrow);
                mPlayScreen.setImageResource(R.mipmap.ic_round_play_arrow);
                cancelDismissTopBottomTimer();
                cancelUpdateProgressTimer();
                break;
            case NiceVideoPlayer.STATE_BUFFERING_PLAYING:
                //缓冲播放
                mLoading.setVisibility(View.VISIBLE);
                //改变播放按钮图标
//                mPlay.setImageResource(R.mipmap.icon_play_pause);
//                mLoadText.setText(R.string.ply90003);
                startDismissTopBottomTimer();
                break;
            case NiceVideoPlayer.STATE_BUFFERING_PAUSED:
                //缓冲暂停
                mLoading.setVisibility(View.VISIBLE);
                //改变播放按钮图标
//                mPlay.setImageResource(R.mipmap.icon_play_start);
//                mLoadText.setText(R.string.ply90003);
                cancelDismissTopBottomTimer();
                break;
            case NiceVideoPlayer.STATE_ERROR:
//                if (mIsLock) {
//                    lockTouchLogic();
//                    mLock.setVisibility(GONE);
//                }
                //播放错误
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                if (mNiceVideoPlayer.isTinyWindow()) {
                    mError.setVisibility(View.VISIBLE);
                } else {
                    mError.setVisibility(View.VISIBLE);
                    if (mPlayMode == NiceVideoPlayer.MODE_FULL_SCREEN)
                        mTop.setVisibility(View.VISIBLE);
                }
                break;
            case NiceVideoPlayer.STATE_COMPLETED:
//                if (mIsLock) {
//                    lockTouchLogic();
//                    mLock.setVisibility(GONE);
//                }
                //播放完成
                setTopBottomVisible(true);
                cancelUpdateProgressTimer();
//                    nextVideo();
                //改变播放按钮图标
                mPlay.setVisibility(View.VISIBLE);
                mPlay.setImageResource(R.mipmap.ic_round_replay);
                break;
        }
    }

    /**
     * @param playMode 播放器的模式：
     *                 {@link NiceVideoPlayer#MODE_NORMAL}
     *                 {@link NiceVideoPlayer#MODE_FULL_SCREEN}
     *                 {@link NiceVideoPlayer#MODE_TINY_WINDOW}
     */
    @Override
    protected void onPlayModeChanged(int playMode) {
        mPlayMode = playMode;
        switch (playMode) {
            case NiceVideoPlayer.MODE_NORMAL:
                mTop.setVisibility(View.GONE);
                mFullCenter.setVisibility(View.GONE);
                mBottom.setVisibility(View.GONE);
                mDefaultMode.setVisibility(View.VISIBLE);
                mDefaultVolume.setVisibility(View.VISIBLE);
                break;
            case NiceVideoPlayer.MODE_FULL_SCREEN:
                mTop.setVisibility(View.VISIBLE);
                mDefaultMode.setVisibility(View.GONE);
                mDefaultVolume.setVisibility(View.GONE);
                mFullCenter.setVisibility(View.VISIBLE);
                mBottom.setVisibility(View.VISIBLE);
                mCopyAndVolume.setVisibility(View.VISIBLE);
                mLock.setVisibility(View.VISIBLE);
                break;
            case NiceVideoPlayer.MODE_TINY_WINDOW:
                mTop.setVisibility(View.GONE);
                mBottom.setVisibility(View.GONE);
                mDefaultMode.setVisibility(View.GONE);
                mFullCenter.setVisibility(View.GONE);
                mDefaultVolume.setVisibility(View.GONE);
                break;
        }
    }


    /**
     * 重置控制器，将控制器恢复到初始状态
     */
    @Override
    protected void reset() {
        topBottomVisible = false;
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
        cancelDismissLockTimer();
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);
        mDefaultSeek.setProgress(0);
        mDefaultSeek.setSecondaryProgress(0);
        if (mPlayMode == NiceVideoPlayer.MODE_NORMAL) {
            mTop.setVisibility(View.GONE);
            mDefaultMode.setVisibility(View.VISIBLE);
            mDefaultVolume.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.VISIBLE);
            mFullCenter.setVisibility(View.GONE);
            mBottom.setVisibility(View.GONE);
        } else if (mPlayMode == NiceVideoPlayer.MODE_FULL_SCREEN) {
            mDefaultMode.setVisibility(View.GONE);
            mDefaultVolume.setVisibility(View.GONE);
            mBottom.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.VISIBLE);
            mFullCenter.setVisibility(View.VISIBLE);
            mTop.setVisibility(View.VISIBLE);
            mCopyAndVolume.setVisibility(View.VISIBLE);
            mLock.setVisibility(View.VISIBLE);
        }
        mPlay.setImageResource(R.mipmap.ic_round_play_circle_outline);
        mDefaultPlay.setImageResource(R.mipmap.ic_round_play_arrow);
        mPlayScreen.setImageResource(R.mipmap.ic_round_play_arrow);
        mLoading.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);
    }

    /**
     * 开启监听器
     */
    private void requestOrientationEventListener() {
//        mOrientationEventListener = new SimpleOrientationEventListener(mActivity) {
//            @Override
//            public void onChanged(int lastOrientation, int orientation) {
//                if ((lastOrientation == 1 && orientation == 3) || (lastOrientation == 3 && orientation == 1))
//                    return;
//                if (!SensorUtil.isOpenSensor(mActivity))
//                    return;
//                if (orientation == SimpleOrientationEventListener.ORIENTATION_PORTRAIT) {
//                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                    exFullScreen();
//                } else if (orientation == SimpleOrientationEventListener.ORIENTATION_LANDSCAPE) {
//                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                    fullScreen();
//                } else if (orientation == SimpleOrientationEventListener.ORIENTATION_LANDSCAPE_REVERSE) {
//                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//                    fullScreen();
//                }
//            }
//        };
//        mOrientationEventListener.enable();
    }

    public void back() {
        if (mPlayMode == NiceVideoPlayer.MODE_FULL_SCREEN) {//退出全屏
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mNiceVideoPlayer.exitFullScreen();
        } else if (mPlayMode == NiceVideoPlayer.MODE_NORMAL) {
            //关闭播放界面返回上一个界面
            mActivity.finish();
        }
    }

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到{@link #onPlayStateChanged}和{@link #onPlayModeChanged}中处理.
     */
    @Override
    public void onClick(View v) {
        if (v == mBack) {
            back();
        } else if (v == mFullScreen) {//进入全屏
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            mOrientationEventListener.lastOrientation = SimpleOrientationEventListener.ORIENTATION_LANDSCAPE;
            fullScreen();
        } else if (v == mShrinkScreen) {//退出全屏
//            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            mOrientationEventListener.lastOrientation = SimpleOrientationEventListener.ORIENTATION_PORTRAIT;
//            exFullScreen();
            back();
        } else if (v == mRetry) {//点击重新播放
            mNiceVideoPlayer.restart();
        } else if (v == mLock) {//锁
            if (!mIsLock) {
                if (mNiceVideoPlayer.isCompleted() ||
                        mNiceVideoPlayer.isError()) {
                    return;
                }
            }
            lockTouchLogic();
        } else if (v == mDefaultVolume || v == mVolume) {//点击音量
            int volume = mNiceVideoPlayer.getVolume();//当前音量
            int maxVolume = mNiceVideoPlayer.getMaxVolume();
            if (volume != 0) {
                mNiceVideoPlayer.setVolume(0);
            } else {
                mNiceVideoPlayer.setVolume(maxVolume);
            }
        } else if (v == mPlay || v == mDefaultPlay || v == mPlayScreen) {//播放或暂停
            if (mNiceVideoPlayer.isPlaying() || mNiceVideoPlayer.isBufferingPlaying()) {
                //播放状态，就暂停
                mNiceVideoPlayer.pause();
            } else if (mNiceVideoPlayer.isPaused() || mNiceVideoPlayer.isBufferingPaused()) {
                mNiceVideoPlayer.restart();
            } else if (mNiceVideoPlayer.isCompleted()) {
                mNiceVideoPlayer.releasePlayer();
                mNiceVideoPlayer.start();
            } else if (mNiceVideoPlayer.isIdle()) {//刚开始
                mNiceVideoPlayer.start();
            }
        } else if (v == mSelections) {//选集
            List<DownLoadBean> mVideoBeans = DownloadModel.getMyVideo(mActivity);
            hideVideoListViewLayout(false);
            showSelectionsView(mVideoBeans);
        } else if (v == mClarity) {//清晰度
            if (mCurrentVideo != null) {
                hideVideoListViewLayout(false);
                showClarityView(mCurrentVideo);
            }
        } else if (v == mCopy) {//复制
            if (mSelectionController != null) {
                mSelectionController.onCopy();
            }
        } else if (v == this) {
            if (isShowLayout) {
                hideVideoListViewLayout(false);
            } else if (mIsLock) {
                setLockVisible(!lockVisible);
            } else {
                if (mNiceVideoPlayer.isPlaying()
                        || mNiceVideoPlayer.isPaused()
                        || mNiceVideoPlayer.isBufferingPlaying()
                        || mNiceVideoPlayer.isBufferingPaused() || mNiceVideoPlayer.isCompleted()) {
                    setTopBottomVisible(!topBottomVisible);
                    if (mNiceVideoPlayer.isPlaying() || mNiceVideoPlayer.isBufferingPlaying()) {
                        mPlay.setVisibility(View.INVISIBLE);
                    }
                    if (mNiceVideoPlayer.isTinyWindow()) {
                        mNiceVideoPlayer.exitTinyWindow();
                    }
                }
            }
        }
    }


    private void volumeChangeVolume(int volume) {
        if (volume != 0) {
            if (mVolumeChange == 0) {
                mDefaultVolume.setImageResource(R.mipmap.ic_round_volume_up);
                mVolume.setImageResource(R.mipmap.ic_round_volume_up);
                mVolumeChange = 1;
            }
        } else {
            mVolumeChange = 0;
            mDefaultVolume.setImageResource(R.mipmap.ic_round_volume_off);
            mVolume.setImageResource(R.mipmap.ic_round_volume_off);
        }
    }

    private void lockTouchLogic() {
        if (!mIsLock) {
            mLock.setImageResource(R.mipmap.ic_round_lock);
            setEnableLock(true);
            hideAllWidget();
        } else {
            mLock.setImageResource(R.mipmap.ic_round_lock_open);
            setEnableLock(false);
            cancelDismissLockTimer();
            setTopBottomVisible(true);
        }
    }

    private void hideAllWidget() {
        mTop.setVisibility(View.INVISIBLE);
        mPlay.setVisibility(View.INVISIBLE);
        mBottom.setVisibility(View.INVISIBLE);
        setLockVisible(true);
    }

    public void setEnableLock(boolean isLock) {
//        if (mOrientationEventListener == null || mPlayMode != NiceVideoPlayer.MODE_FULL_SCREEN)
//            return;
        this.mIsLock = isLock;
//        if (isLock) {
//            mOrientationEventListener.disable();
//        } else {
//            mOrientationEventListener.enable();
//        }
    }

    /**
     * 进入全屏
     */
    public void fullScreen() {
        if (mNiceVideoPlayer != null)
            mNiceVideoPlayer.enterFullScreen();
    }

    /**
     * 退出全屏模式
     */
    public void exFullScreen() {
        if (isShowLayout)
            hideVideoListViewLayout(false);
        if (mNiceVideoPlayer != null)
            mNiceVideoPlayer.exitFullScreen();
    }

    public void enterTinyWindow() {
        if (mNiceVideoPlayer != null)
            mNiceVideoPlayer.enterTinyWindow();
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setTopBottomVisible(boolean visible) {
        if (mPlayMode == NiceVideoPlayer.MODE_FULL_SCREEN) {//全屏模式
            if (!mIsLock) {
                mTop.setVisibility(visible ? View.VISIBLE : View.GONE);//显示顶部布局
                mDefaultMode.setVisibility(View.GONE);//隐藏默认底部布局
                mDefaultVolume.setVisibility(View.GONE);//隐藏默认音量
                mPlay.setVisibility(visible ? View.VISIBLE : View.GONE);
                mFullCenter.setVisibility(visible ? View.VISIBLE : View.GONE);
                mCopyAndVolume.setVisibility(visible ? View.VISIBLE : View.GONE);
                mLock.setVisibility(visible ? View.VISIBLE : View.GONE);
                mBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        } else if (mPlayMode == NiceVideoPlayer.MODE_TINY_WINDOW) {
            mTop.setVisibility(View.GONE);
            mDefaultMode.setVisibility(View.GONE);
            mDefaultVolume.setVisibility(View.GONE);
            mFullCenter.setVisibility(View.GONE);
            mBottom.setVisibility(View.GONE);
            mPlay.setVisibility(View.GONE);
            mCopyAndVolume.setVisibility(View.GONE);
            mLock.setVisibility(View.GONE);
        } else if (mPlayMode == NiceVideoPlayer.MODE_NORMAL) {//默认布局
            mTop.setVisibility(View.GONE);
            mDefaultMode.setVisibility(visible ? View.VISIBLE : View.GONE);
            mDefaultVolume.setVisibility(visible ? View.VISIBLE : View.GONE);
            mPlay.setVisibility(visible ? View.VISIBLE : View.GONE);
            mFullCenter.setVisibility(View.GONE);
            mBottom.setVisibility(View.GONE);
            mCopyAndVolume.setVisibility(View.GONE);
            mLock.setVisibility(View.GONE);
        }
        topBottomVisible = visible;
        if (visible) {
            if (!mNiceVideoPlayer.isPaused() && !mNiceVideoPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setLockVisible(boolean visible) {
        mFullCenter.setVisibility(View.VISIBLE);
        mCopyAndVolume.setVisibility(View.INVISIBLE);
        mLock.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        lockVisible = visible;
        if (visible) {
            startDismissLockTimer();
        } else {
            cancelDismissLockTimer();
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private void startDismissLockTimer() {
        cancelDismissLockTimer();
        if (mDismissLockCountDownTimer == null) {
            mDismissLockCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setLockVisible(false);
                }
            };
        }
        mDismissLockCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissLockTimer() {
        if (mDismissLockCountDownTimer != null) {
            mDismissLockCountDownTimer.cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Loger.e("seekBar.getProgress() " + seekBar.getProgress());
//        if (mNiceVideoPlayer.isBufferingPaused() || mNiceVideoPlayer.isPaused()) {
//            mNiceVideoPlayer.restart();
//        }
//        long position = (long) (mNiceVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        mNiceVideoPlayer.seekTo(seekBar.getProgress());
        startDismissTopBottomTimer();
    }

    @Override
    protected void updateProgress() {
        long position = mNiceVideoPlayer.getCurrentPosition();
        long duration = mNiceVideoPlayer.getDuration();
        int bufferPercentage = mNiceVideoPlayer.getBufferPercentage();//第二进度

        mSeek.setMax((int) duration);
        mDefaultSeek.setMax((int) duration);
        mSeek.setSecondaryProgress(bufferPercentage);
        mDefaultSeek.setSecondaryProgress(bufferPercentage);
        mSeek.setProgress((int) position);
        mDefaultSeek.setProgress((int) position);

        String seekTime = JZUtils.stringForTime(position);
        String totalTime = JZUtils.stringForTime(duration);
        mPosition.setText(seekTime);
        mTotalTime.setText(totalTime);
        mDefaultPosition.setText(seekTime);
        mDefaultTotalTime.setText(totalTime);
    }

    private class VolumeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //媒体音量改变才通知
            if (VOLUME_CHANGED_ACTION.equals(intent.getAction())) {
                int volume = mNiceVideoPlayer.getVolume();//当前音量
                volumeChangeVolume(volume);
            }
        }
    }

    private ISelectionController mSelectionController;

    //选集
    public void setOnSelectionController(ISelectionController playController) {
        mSelectionController = playController;
    }

    public interface ISelectionController {

        void onVideoPrepared();

        void onSelectVideo(DownLoadBean bean);//选择播放的视频

        void onSelectClarity(DownLoadBean bean, String videoPath, long playPosition);//选择播放的视频

        void onCopy();//复制
    }

    //选择清晰度播放
    private void showClarityView(DownLoadBean videoBean) {
        final List<MovieHLS> movieHLSListView = new Gson().fromJson(videoBean.movieHLS, new TypeToken<List<MovieHLS>>() {
        }.getType());
        for (int j = 0; j < movieHLSListView.size(); j++) {
            MovieHLS movieHLS1 = movieHLSListView.get(j);
            if (!TextUtils.isEmpty(movieHLS1.resolution) && videoBean.downLoad_hls == Integer.parseInt(movieHLS1.resolution)) {
                movieHLSListView.get(j).resolutionUrl = videoBean.path;
                if (mCurrentClarity == -1)
                    mCurrentClarity = Integer.parseInt(movieHLS1.resolution);
                movieHLSListView.get(j).isSelect = true;
                movieHLSListView.get(j).isLocalPath = true;
            } else {
                movieHLSListView.get(j).isSelect = false;
                movieHLSListView.get(j).isLocalPath = false;
            }
        }
        if (movieHLSListView.size() > 0) {
            isShowLayout = true;
            mClarityAdapter = new ClarityAdapter(mActivity);
            mClarityAdapter.update(movieHLSListView, mCurrentClarity);
            condition_lv.setAdapter(mClarityAdapter);
            hideVideoListViewLayout(true);
            condition_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MovieHLS movieHLS = movieHLSListView.get(position);
                    if (mCurrentClarity != Integer.parseInt(movieHLS.resolution)) {//防止选重
                        checkClarityPosition(position, movieHLSListView);
                    }
                    hideVideoListViewLayout(false);
                }
            });
        }
    }

    //设置选中的位置，将其他位置设置为未选
    public void checkClarityPosition(int position, List<MovieHLS> movieHLSListView) {
        for (int i = 0; i < movieHLSListView.size(); i++) {
            MovieHLS movieHLS = movieHLSListView.get(position);
            if (position == i) {// 设置已选位置
                movieHLS.isSelect = true;
                mCurrentClarity = Integer.parseInt(movieHLS.resolution);
            } else {
                movieHLS.isSelect = false;
            }
        }
        if (mSelectionController != null) {
            mSelectionController.onSelectClarity(mCurrentVideo, movieHLSListView.get(position).resolutionUrl, mNiceVideoPlayer.getCurrentPosition());
        }
        mClarityAdapter.updateSelectClarity(mCurrentClarity);
        setTextViewClarity(mCurrentClarity);
    }

    //选集
    private void showSelectionsView(final List<DownLoadBean> mVideoBeans) {
        if (mVideoBeans != null) {
            isShowLayout = true;
            mSelectionsAdapter = new SelectionsAdapter(mActivity);
            mSelectionsAdapter.update(mVideoBeans, mCurrentVideo);
            condition_lv.setAdapter(mSelectionsAdapter);
            if (mVideoBeans.size()<=0){
                emptyView.setVisibility(View.VISIBLE);
            }else {
                emptyView.setVisibility(View.GONE);
            }
            hideVideoListViewLayout(true);
            condition_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mCurrentVideo == null) return;
                    DownLoadBean bean = mVideoBeans.get(position);
                    if (mCurrentVideo.video_id != bean.video_id)//防止选重
                        checkSelectionsPosition(position, bean);
                    hideVideoListViewLayout(false);
                }
            });
        }
    }

    //设置选中的位置，将其他位置设置为未选
    public void checkSelectionsPosition(int position, DownLoadBean bean) {
        mSelectionsAdapter.updateSelectBean(bean);
        mCurrentVideo = bean;
        mCurrentClarity = bean.downLoad_hls;
        if (mSelectionController != null) {
            mSelectionController.onSelectVideo(bean);
        }
        setTextViewClarity(mCurrentClarity);
    }

    @SuppressLint("SetTextI18n")
    private void setTextViewClarity(int clarity) {
        mClarity.setText(clarity + "p");
    }
}