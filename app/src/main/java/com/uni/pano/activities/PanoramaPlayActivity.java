package com.uni.pano.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.uni.common.util.DateTimeUtil;
import com.uni.common.util.FileUtil;
import com.uni.common.util.TimeUtil;
import com.uni.pano.MainApplication;
import com.uni.pano.R;
import com.uni.pano.base.BaseActivity;
import com.uni.pano.bean.ArrayMediaInfo;
import com.uni.pano.bean.FilterInfo;
import com.uni.pano.bean.MediaInfo;
import com.uni.pano.config.EnumElement;
import com.uni.pano.event.ScreenShotEvent;
import com.uni.pano.fragment.DeleteDialogFragment;
import com.uni.pano.share.ShareUtil;
import com.uni.pano.utils.CommonUtil;
import com.uni.pano.widget.CToast;
import com.uni.vr.PanoramaImageView;
import com.uni.vr.PanoramaVideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

/**
 * Created by DELL on 2017/3/13.
 */

public class PanoramaPlayActivity extends BaseActivity {
    @BindView(R.id.piv_play)
    PanoramaImageView piv_play;
    @BindView(R.id.pvv_play)
    PanoramaVideoView pvv_play;
    @BindView(R.id.ll_set)
    LinearLayout ll_set;
    @BindView(R.id.ib_cancel)
    ImageButton ib_cancel;
    @BindView(R.id.s_vr)
    Switch s_vr;
    @BindView(R.id.tv_delete)
    TextView tv_delete;
    @BindView(R.id.ll_top)
    LinearLayout ll_top;
    @BindView(R.id.ib_back)
    ImageButton ib_back;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.ib_share)
    ImageButton ib_share;
    @BindView(R.id.ib_set)
    ImageButton ib_set;
    @BindView(R.id.ib_last)
    ImageButton ib_last;
    @BindView(R.id.ib_next)
    ImageButton ib_next;
    @BindView(R.id.ll_filter)
    LinearLayout ll_filter;
    @BindView(R.id.rv_filter)
    RecyclerView rv_filter;
    @BindView(R.id.ll_image)
    LinearLayout ll_image;
    @BindView(R.id.cb_filter_image)
    CheckBox cb_filter_image;
    @BindView(R.id.tv_view_mode)
    TextView tv_view_mode;
    @BindView(R.id.tv_option_mode)
    TextView tv_option_mode;
    @BindView(R.id.ll_video)
    LinearLayout ll_video;
    @BindView(R.id.sb_progress)
    SeekBar sb_progress;
    @BindView(R.id.cb_play)
    CheckBox cb_play;
    @BindView(R.id.tv_progress)
    TextView tv_progress;
    @BindView(R.id.cb_filter_video)
    CheckBox cb_filter_video;
    @BindView(R.id.ib_view_mode)
    ImageButton ib_view_mode;
    @BindView(R.id.ib_option_mode)
    ImageButton ib_option_mode;

    @BindView(R.id.tv_screenshot)
    TextView tv_screenshot;
    @BindView(R.id.iv_screen_shot_animation)
    ImageView iv_screen_shot_animation;

    private MediaInfo mediaInfo;
    private int mediaInfoIndex = 0;
    private List<MediaInfo> mediaInfoList = new ArrayList<>();
    private boolean isTouchEnd = true;
    EnumElement.VIEW_MODE view_mode = EnumElement.VIEW_MODE.FISH;
    EnumElement.OPTION_MODE option_mode = EnumElement.OPTION_MODE.FINGER;
    public static void startActivity(Activity activity, MediaInfo mediaInfo, ArrayMediaInfo arrayMediaInfo){
        if (mediaInfo.isExist()) {
            Intent intent = new Intent(activity, PanoramaPlayActivity.class);
            intent.putExtra(MediaInfo.TAG, mediaInfo);
            intent.putExtra(ArrayMediaInfo.TAG, arrayMediaInfo);
            MainApplication.render_mode = EnumElement.RENDER_MODE.SINGLE;
            activity.startActivity(intent);
        }else{
            CToast.showToast(activity.getString(R.string.exception));
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.av_panorama_play);
        if (MainApplication.render_mode == EnumElement.RENDER_MODE.DOUBLE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //LogcatHelper.getInstance(this).start();
        piv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFullScreen();
            }
        });
        pvv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFullScreen();
            }
        });
        init();
        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fProgress = progress;
            }
            float fProgress = 0.0f;
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouchEnd = false;
                cb_play.setChecked(false);
                pvv_play.doSetPlaying(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouchEnd = true;
                pvv_play.doUpdateProgress((float)fProgress*0.001f);
            }
        });
        initFilter();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init();
    }
    private void init(){
        tv_option_mode.setVisibility(MainApplication.bSensorRotate?View.VISIBLE:View.GONE);
        ib_option_mode.setVisibility(MainApplication.bSensorRotate?View.VISIBLE:View.GONE);
        mediaInfo  =   (MediaInfo)getIntent().getSerializableExtra(MediaInfo.TAG);
        mediaInfoList = ((ArrayMediaInfo)getIntent().getSerializableExtra(ArrayMediaInfo.TAG)).mediaInfoList;
        if (mediaInfoList.size()>2){
            Collections.sort(mediaInfoList, new MediaInfo.SortComparator());
        }
        updateView();
    }
    private void updateView(){
        updateLastNextBtn();
        s_vr.setChecked(MainApplication.render_mode== EnumElement.RENDER_MODE.DOUBLE);
        option_mode = s_vr.isChecked()?EnumElement.OPTION_MODE.GYROSCOPE:EnumElement.OPTION_MODE.FINGER;
        updateOptionMode(option_mode);
        ll_set.setVisibility(View.GONE);
        tv_time.setText(DateTimeUtil.getModifiedDate(mediaInfo.lastModified));
        switch (mediaInfo.type){
            case MP4:{
                piv_play.setVisibility(View.GONE);
                pvv_play.setVisibility(View.VISIBLE);
                pvv_play.loadVideo(mediaInfo.filePath, mediaInfo.isAsset?getAssets():null);
                ll_image.setVisibility(View.GONE);
                ll_video.setVisibility(View.VISIBLE);
            }
                break;
            default:{
                piv_play.setVisibility(View.VISIBLE);
                pvv_play.setVisibility(View.GONE);
                pvv_play.doSetPlaying(false);
                piv_play.loadImage(mediaInfo.filePath, mediaInfo.isAsset?getAssets():null);
                ll_image.setVisibility(View.VISIBLE);
                ll_video.setVisibility(View.GONE);
            }
                break;
        }
        updateViewMode(view_mode);
        updateProgress(0);
        if (MainApplication.render_mode == EnumElement.RENDER_MODE.DOUBLE){
            ll_image.setVisibility(View.GONE);
            ll_video.setVisibility(View.GONE);
        }
        piv_play.doUpdateRenderMode(MainApplication.render_mode);
        pvv_play.doUpdateRenderMode(MainApplication.render_mode);
    }
    private void switchFullScreen(){
        ll_set.setVisibility(View.GONE);
        ll_filter.setVisibility(View.GONE);
        cb_filter_image.setChecked(false);
        cb_filter_video.setChecked(false);
        if (ll_top.getVisibility() == View.VISIBLE){
            ll_top.setVisibility(View.GONE);
            ll_image.setVisibility(View.GONE);
            ll_video.setVisibility(View.GONE);
            ib_last.setVisibility(View.GONE);
            ib_next.setVisibility(View.GONE);
        }else{
            ll_top.setVisibility(View.VISIBLE);
            if(mediaInfo.isVideo()){
                ll_video.setVisibility(View.VISIBLE);
                ll_image.setVisibility(View.GONE);
            }else{
                ll_video.setVisibility(View.GONE);
                ll_image.setVisibility(View.VISIBLE);
            }
            ib_last.setVisibility(mediaInfoIndex>0?View.VISIBLE:View.GONE);
            ib_next.setVisibility(mediaInfoList.size()>(mediaInfoIndex+1)?View.VISIBLE:View.GONE);
        }
        if (MainApplication.render_mode == EnumElement.RENDER_MODE.DOUBLE){
            ll_image.setVisibility(View.GONE);
            ll_video.setVisibility(View.GONE);
        }
    }
    private void updateLastNextBtn(){
        int last = 0;
        int next = 0;
        mediaInfoIndex = 0;
        for (MediaInfo mediaInfo:mediaInfoList){
            if(mediaInfo.isSameRes(this.mediaInfo)){
                break;
            }else{
                last++;
                mediaInfoIndex ++;
            }
        }
        next = mediaInfoList.size() - last - 1;
        ib_last.setVisibility(last>0?View.VISIBLE:View.GONE);
        ib_next.setVisibility(next>0?View.VISIBLE:View.GONE);
    }

    public void updateViewMode(final EnumElement.VIEW_MODE view_mode){
        this.view_mode = view_mode;
        if (mediaInfo.isVideo()){
            pvv_play.doUpdateViewMode(view_mode);
            switch (view_mode){
                case FISH: {
                    ib_view_mode.setImageResource(R.mipmap.player_ic_fisheyes_n);
                }
                break;
                case PERSPECTIVE:{
                    ib_view_mode.setImageResource(R.mipmap.player_ic_lay_n);
                }
                break;
                case PLANET:{
                    ib_view_mode.setImageResource(R.mipmap.player_ic_planet_n);
                }
                break;
                case CRYSTAL_BALL:{
                    ib_view_mode.setImageResource(R.mipmap.player_ic_magicball_n);
                }
                break;
            }
        }else{
            piv_play.doUpdateViewMode(view_mode);
            switch (view_mode){
                case FISH: {
                    Drawable drawableViewMode = getDrawable(R.mipmap.player_ic_fisheyes_n);
                    drawableViewMode.setBounds(0, 0, drawableViewMode.getMinimumWidth(), drawableViewMode.getMinimumHeight());
                    tv_view_mode.setCompoundDrawables(drawableViewMode, null, null, null);
                    tv_view_mode.setText(R.string.fish_eye);
                }
                break;
                case PERSPECTIVE:{
                    Drawable drawableViewMode   =   getDrawable(R.mipmap.player_ic_lay_n);
                    drawableViewMode.setBounds(0, 0, drawableViewMode.getMinimumWidth(), drawableViewMode.getMinimumHeight());
                    tv_view_mode.setCompoundDrawables(drawableViewMode, null, null, null);
                    tv_view_mode.setText(R.string.perspective);
                }
                break;
                case PLANET:{
                    Drawable drawableViewMode   =   getDrawable(R.mipmap.player_ic_planet_n);
                    drawableViewMode.setBounds(0, 0, drawableViewMode.getMinimumWidth(), drawableViewMode.getMinimumHeight());
                    tv_view_mode.setCompoundDrawables(drawableViewMode, null, null, null);
                    tv_view_mode.setText(R.string.little_planet);
                }
                break;
                case CRYSTAL_BALL:{
                    Drawable drawableViewMode   =   getDrawable(R.mipmap.player_ic_magicball_n);
                    drawableViewMode.setBounds(0, 0, drawableViewMode.getMinimumWidth(), drawableViewMode.getMinimumHeight());
                    tv_view_mode.setCompoundDrawables(drawableViewMode, null, null, null);
                    tv_view_mode.setText(R.string.crystal_ball);
                }
                break;
            }
        }
    }
    public void updateOptionMode(final EnumElement.OPTION_MODE optionMode){
        option_mode = optionMode;
        piv_play.doUpdateOptionMode(option_mode);
        pvv_play.doUpdateOptionMode(option_mode);
        switch (option_mode){
            case FINGER: {
                Drawable drawableOptionMode = getDrawable(R.mipmap.pic_finger);
                drawableOptionMode.setBounds(0, 0, drawableOptionMode.getMinimumWidth(), drawableOptionMode.getMinimumHeight());
                tv_option_mode.setCompoundDrawables(drawableOptionMode, null, null, null);
                tv_option_mode.setText(R.string.finger_drag);
                ib_option_mode.setImageResource(R.mipmap.pic_finger);
            }
                break;
            case GYROSCOPE:{
                Drawable drawableOptionMode = getDrawable(R.mipmap.pic_rotation);
                drawableOptionMode.setBounds(0, 0, drawableOptionMode.getMinimumWidth(), drawableOptionMode.getMinimumHeight());
                tv_option_mode.setCompoundDrawables(drawableOptionMode, null, null, null);
                tv_option_mode.setText(R.string.gyroscope);
                ib_option_mode.setImageResource(R.mipmap.pic_rotation);
            }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        piv_play.onResume();
        pvv_play.onResume();
        pvv_play.registerOnProgressCallBack(this);
    }

    @Override
    protected void onPause() {
        pvv_play.registerOnProgressCallBack(null);
        super.onPause();
        piv_play.onPause();
        pvv_play.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        piv_play.onDestroy();
        pvv_play.onDestroy();
    }
    public void onProgress(long duration){
        updateProgress(duration);
    }
    public void updateProgress(final long duration){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (duration<0){
                    cb_play.setChecked(true);
                    pvv_play.doRestart();
                }
                if(isTouchEnd) {
                    sb_progress.setProgress((int) (((float) duration / 1000.f) / ((float) mediaInfo.duration) * 1000.f));
                }
                tv_progress.setText(TimeUtil.convTimeMinuteSecond(duration>0?duration/1000000:mediaInfo.duration/1000) + "/" +  TimeUtil.convTimeMinuteSecond(mediaInfo.duration/1000));
            }
        });
    }
    @OnClick({R.id.ib_back, R.id.ib_share, R.id.ib_set, R.id.ib_cancel, R.id.s_vr, R.id.tv_delete, R.id.ib_last, R.id.ib_next, R.id.cb_filter_image, R.id.tv_screenshot, R.id.tv_view_mode, R.id.tv_option_mode, R.id.cb_play, R.id.cb_filter_video, R.id.ib_screenshot, R.id.ib_view_mode, R.id.ib_option_mode})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ib_back:
                onBackPressed();
                break;
            case R.id.ib_share:
                ShareUtil.openSharingPanel(this, mediaInfo, true, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                    }
                });
                break;
            case R.id.ib_set: {
                switchFullScreen();
                ll_set.setVisibility(View.VISIBLE);
            }
                break;
            case R.id.ib_cancel: {
                ll_set.setVisibility(View.GONE);
            }
                break;
            case R.id.s_vr:{
                MainApplication.render_mode = s_vr.isChecked()? EnumElement.RENDER_MODE.DOUBLE: EnumElement.RENDER_MODE.SINGLE;
                if (CommonUtil.isPortrait(this)){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }else{
                    MainApplication.getInstance().updateRotationByConnectStatus(this);
                }
            }
                break;
            case R.id.tv_delete:{
                DeleteDialogFragment deleteDialogFragment = new DeleteDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("FileNumber", 1);
                deleteDialogFragment.setArguments(bundle);
                deleteDialogFragment.setDeleteListener(new DeleteDialogFragment.DeleteListener() {
                    @Override
                    public void onYes() {
                        removeCurRes();
                    }
                });
                deleteDialogFragment.show(getFragmentManager(), DeleteDialogFragment.TAG);
            }
                break;
            case R.id.ib_last: {
                mediaInfoIndex--;
                mediaInfo = mediaInfoList.get(mediaInfoIndex);
                updateView();
            }
                break;
            case R.id.ib_next: {
                mediaInfoIndex++;
                mediaInfo = mediaInfoList.get(mediaInfoIndex);
                updateView();
            }
                break;
            case R.id.cb_filter_image:{
                cb_filter_video.setChecked(cb_filter_image.isChecked());
                ll_filter.setVisibility(cb_filter_image.isChecked()?View.VISIBLE:View.GONE);
            }
                break;
            case R.id.tv_screenshot:
            case R.id.ib_screenshot:{
                if (screenShot){
                    return;
                }
                startScreenShot();
                switch (mediaInfo.type) {
                    case MP4: {
                        pvv_play.doScreenShot(this);
                    }
                    break;
                    default: {
                        piv_play.doScreenShot(this);
                    }
                    break;
                }
            }
                break;
            case R.id.tv_view_mode:
            case R.id.ib_view_mode:{
                int vMode = view_mode.ordinal() + 1;
                view_mode = EnumElement.VIEW_MODE.values()[vMode % (EnumElement.VIEW_MODE.CRYSTAL_BALL.ordinal()+1)];
                updateViewMode(view_mode);
            }
                break;
            case R.id.tv_option_mode:
            case R.id.ib_option_mode:{
                int oMode = option_mode.ordinal() + 1;
                option_mode = EnumElement.OPTION_MODE.values()[oMode % (EnumElement.OPTION_MODE.GYROSCOPE.ordinal()+1)];
                updateOptionMode(option_mode);
            }
                break;
            case R.id.cb_play:{
                pvv_play.doSetPlaying(cb_play.isChecked());
            }
                break;
            case R.id.cb_filter_video:{
                cb_filter_image.setChecked(cb_filter_video.isChecked());
                ll_filter.setVisibility(cb_filter_video.isChecked()?View.VISIBLE:View.GONE);
            }
                break;
        }
    }
    private boolean screenShot = false;
    AnimatorSet animatorSet = new AnimatorSet();
    private void startScreenShot(){
        screenShot = true;
        iv_screen_shot_animation.setClickable(true);
        iv_screen_shot_animation.setAlpha(1.f);
        SoundPool soundPool = new SoundPool.Builder().build();
        soundPool.play(soundPool.load(this, R.raw.camera_click, 1), 1, 1, 0, 0, 1);
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(iv_screen_shot_animation, "alpha", 1, 0.f)
        );
        animatorSet.setDuration(2 * 1000).start();
    }
    private void endScreenShot(){
        iv_screen_shot_animation.setClickable(false);
        iv_screen_shot_animation.setAlpha(0.0f);
        screenShot = false;
        animatorSet.end();
    }

    private void removeCurRes(){
        if(FileUtil.deleteFile(mediaInfo.filePath)) {
            MediaScannerConnection.scanFile(MainApplication.getInstance(), new String[]{ mediaInfo.filePath },
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(final String path, Uri uri) {

                        }
                    });
            int size = mediaInfoList.size();
            if(size == mediaInfoIndex){
                finish();
                return;
            }else if (size > (mediaInfoIndex + 1)) {
                mediaInfoList.remove(mediaInfoIndex);
                mediaInfo = mediaInfoList.get(mediaInfoIndex);
            }else if(size == (mediaInfoIndex + 1)){
                mediaInfoList.remove(mediaInfoIndex);
                mediaInfoIndex --;
                mediaInfo = mediaInfoList.get(mediaInfoIndex);
            }
            updateView();
        }else{
            CToast.showToast("删除失败!");
        }
    }

    @Override
    protected void handleOrientation() {
        switch (MainApplication.render_mode){
            case SINGLE:{
                MainApplication.getInstance().updateRotationByConnectStatus(this);
            }
                break;
            case DOUBLE:{

            }
                break;
        }
    }
    ArrayList<FilterInfo> filterInfos = new ArrayList<FilterInfo>();
    FilterAdapter mFilterAdapter;
    private void initFilter(){
        LinearLayoutManager linearLayoutManager    =   new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_filter.setLayoutManager(linearLayoutManager);
        rv_filter.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 20, 0);
            }
        });
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_original), R.drawable.ic_filter_original, "", true));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_budapest), R.drawable.ic_filter_budapest, "1.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_chaplin), R.drawable.ic_filter_chaplin, "2.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_autumn), R.drawable.ic_filter_autumn, "3.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_oak), R.drawable.ic_filter_oak, "4.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_monroe), R.drawable.ic_filter_monroe, "5.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_dejavu), R.drawable.ic_filter_dejiavu, "6.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_lavie), R.drawable.ic_filter_lavie, "7.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_lucifer), R.drawable.ic_filter_lucifer, "8.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_memo), R.drawable.ic_filter_memo, "9.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_confess), R.drawable.ic_filter_confess, "10.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_sapporo), R.drawable.ic_filter_sapporo, "11.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_lp), R.drawable.ic_filter_lp, "12.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_blossom), R.drawable.ic_filter_blossom, "13.bmp", false));
        filterInfos.add(new FilterInfo(getString(R.string.filter_name_gray_relief), R.drawable.ic_filter_gray_relief, "", false, EnumElement.FILTER_TYPE.GRAY_RELIEF));
        mFilterAdapter = new FilterAdapter();
        rv_filter.setAdapter(mFilterAdapter);
        mFilterAdapter.notifyDataSetChanged();
    }
    private RoundedImageView roundedImageView = null;
    class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.GalleryViewHolder> {
        @Override
        public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            GalleryViewHolder holder = new GalleryViewHolder(LayoutInflater.from(PanoramaPlayActivity.this).inflate(R.layout.item_filter, parent, false));
            return holder;
        }
        @Override
        public void onBindViewHolder(GalleryViewHolder holder, int position) {
            FilterInfo filterInfo = filterInfos.get(position);
            holder.riv_filter.setTag(filterInfo);
            holder.riv_filter.setImageResource(filterInfo.resId);
            holder.tv_name.setText(filterInfo.filterName);
            if(filterInfo.isSelected) {
                holder.riv_filter.setBorderColor(Color.YELLOW);
                roundedImageView = holder.riv_filter;
            }else{
                holder.riv_filter.setBorderColor(R.color.com_bg_transparent);
            }
            holder.riv_filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    roundedImageView.setBorderColor(R.color.com_bg_transparent);
                    FilterInfo roundFilterInfo = (FilterInfo)roundedImageView.getTag();
                    roundFilterInfo.isSelected = false;
                    FilterInfo filterInfo = (FilterInfo)v.getTag();
                    filterInfo.isSelected = true;
                    roundedImageView = (RoundedImageView)v;
                    roundedImageView.setBorderColor(Color.YELLOW);
                    piv_play.doUpdateLutFilter(PanoramaPlayActivity.this.getAssets(), filterInfo.filterFileName, filterInfo.filterType);
                    pvv_play.doUpdateLutFilter(PanoramaPlayActivity.this.getAssets(), filterInfo.filterFileName, filterInfo.filterType);
                }
            });
        }
        @Override
        public int getItemCount() {
            return filterInfos.size();
        }
        class GalleryViewHolder extends RecyclerView.ViewHolder {
            View view;
            RoundedImageView riv_filter;
            TextView tv_name;
            public GalleryViewHolder(View view) {
                super(view);
                this.view = view;
                riv_filter = (RoundedImageView)view.findViewById(R.id.riv_filter);
                tv_name = (TextView)view.findViewById(R.id.tv_name);
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScreenShotEvent(ScreenShotEvent screenShotEvent) {
        endScreenShot();
        File file  = new File(screenShotEvent.fileName);
        if (!screenShotEvent.fileName.isEmpty()){
            try {
                MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), screenShotEvent.fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 最后通知图库更新
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
        }
    }
}
