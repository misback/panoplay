package com.uni.pano.fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.lwm.shapeimageview.RoundedImageView;
import com.umeng.socialize.utils.Log;
import com.uni.common.config.PathConfig;
import com.uni.common.util.DateTimeUtil;
import com.uni.common.util.FileSizeUtil;
import com.uni.common.util.FileUtil;
import com.uni.pano.MainApplication;
import com.uni.pano.R;
import com.uni.pano.activities.PanoramaPlayActivity;
import com.uni.pano.base.BaseFragment;
import com.uni.pano.bean.ArrayMediaInfo;
import com.uni.pano.bean.MediaInfo;
import com.uni.pano.event.CreateFileEvent;
import com.uni.pano.event.GalleryMediaInfoListUpdateEvent;
import com.uni.pano.utils.CommonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import butterknife.BindView;

/**
 * Created by DELL on 2017/2/24.
 */

public class GalleryFragment extends BaseFragment {
    public final static String TAG = GalleryFragment.class.getSimpleName();
    @BindView(R.id.srl_gallery)
    SwipeRefreshLayout srl_gallery;
    @BindView(R.id.rv_gallery)
    RecyclerView rv_gallery;
    private TextView tv_cancel_select;
    private GalleryAdapter mGalleryAdapter;
    private List<MediaInfo> mediaInfoList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private String select_num;
    private boolean mUseGlide   =   true;

    @Override
    protected void initUI(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.fm_gallery);
        mLinearLayoutManager    =   new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_gallery.setLayoutManager(mLinearLayoutManager);
        rv_gallery.setHasFixedSize(true);
        // rv_gallery.setItemAnimator(new DefaultItemAnimator());
        rv_gallery.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(40, 0, 40, 20);
            }
        });
        rv_gallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mUseGlide   =   true;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        // 这句话是为了，第一次进入页面的时候显示加载进度条
        srl_gallery.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        srl_gallery.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srl_gallery.setRefreshing(false);
//                if(mediaInfoList.size()>0){
//                    srl_gallery.setRefreshing(false);
//                }else {
//                    srl_gallery.setRefreshing(true);
//                    requestMediaInfoList();
//                }
            }
        });
        if (mGalleryAdapter == null) {
            mGalleryAdapter = new GalleryAdapter();
            rv_gallery.setAdapter(mGalleryAdapter);
        }
        requestMediaInfoList();
        select_num = getString(R.string.select_number);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void set_tv_cancel_select(TextView tv_cancel_select){
        this.tv_cancel_select = tv_cancel_select;
    }
    private void requestMediaInfoList(){
        srl_gallery.setRefreshing(true);
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<MediaInfo> mediaInfoList = new ArrayList<MediaInfo>();
                CommonUtil.listMediaInfo(PathConfig.getMediaFolder(), mediaInfoList);
                EventBus.getDefault().post(new GalleryMediaInfoListUpdateEvent(mediaInfoList));
            }
        });
        singleThreadExecutor.shutdown();
    }
    public class GlideRoundTransform extends BitmapTransformation {

        private float radius = 0f;

        public GlideRoundTransform(Context context) {
            this(context, 4);
        }

        public GlideRoundTransform(Context context, int dp) {
            super(context);
            this.radius = Resources.getSystem().getDisplayMetrics().density * dp;
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return roundCrop(pool, toTransform);
        }

        private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
            canvas.drawRoundRect(rectF, radius, radius, paint);
            return result;
        }

        @Override public String getId() {
            return getClass().getName() + Math.round(radius);
        }
    }
    class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
        @Override
        public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            GalleryViewHolder holder = new GalleryViewHolder(LayoutInflater.from(GalleryFragment.this.getActivity()).inflate(R.layout.item_gallery, parent, false));
            return holder;
        }
        @Override
        public void onBindViewHolder(final GalleryViewHolder holder, final int position) {
            MediaInfo mediaInfo =   mediaInfoList.get(position);
            holder.view.setTag(mediaInfo);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaInfo mediaInfo1 = (MediaInfo) v.getTag();
                    if (tv_cancel_select.isShown()) {
                        if (mediaInfo1.selected) {
                            mediaInfo1.selected = false;
                            removeOneSelect(mediaInfo1.index);
                            mediaInfo1.index = 0;
                        } else {
                            mediaInfo1.selected = true;
                            mediaInfo1.index = getSelectedNumber();
                            TextView tv_index = (TextView) v.findViewById(R.id.tv_index);
                            ImageButton ib_preview = (ImageButton) v.findViewById(R.id.ib_preview);
                            tv_index.setText(String.valueOf(mediaInfo1.index));
                            tv_index.setVisibility(mediaInfo1.selected?View.VISIBLE:View.GONE);
                            //ib_preview.setVisibility(mediaInfo.selected?View.VISIBLE:View.GONE);
                            ib_preview.setVisibility(View.GONE);
                        }
                    }else{
                        PanoramaPlayActivity.startActivity(GalleryFragment.this.getActivity(), mediaInfo1, new ArrayMediaInfo(mediaInfoList));
                    }
                }
            });
            holder.tv_index.setText(String.valueOf(mediaInfo.index));
            holder.tv_name.setText(mediaInfo.name);
            holder.tv_date.setText(DateTimeUtil.getModifiedDate(mediaInfo.lastModified));
            holder.tv_path.setText(mediaInfo.fileDir);
            holder.tv_size.setText(FileSizeUtil.convertFileSize(mediaInfo.length, 2));
            if(mediaInfo.selected){
                holder.tv_index.setVisibility(View.VISIBLE);
                holder.ib_preview.setVisibility(View.GONE);
                holder.ib_preview.setTag(mediaInfo);
                holder.ib_preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaInfo mediaInfo = (MediaInfo) v.getTag();
                        PanoramaPlayActivity.startActivity(GalleryFragment.this.getActivity(), mediaInfo, new ArrayMediaInfo(mediaInfoList));
                    }
                });
            }else{
                holder.tv_index.setVisibility(View.INVISIBLE);
                holder.ib_preview.setVisibility(View.GONE);
            }
           // if(mUseGlide) {
                switch (mediaInfo.type) {
                    case MP4:
                        holder.tv_time.setVisibility(View.VISIBLE);
                        try {
                            holder.tv_time.setText(DateTimeUtil.getFormatTime(mediaInfo.duration/1000));
                            Glide.with(GalleryFragment.this.getActivity()).load(mediaInfo.filePath).crossFade(0).placeholder(R.drawable.ic_photo_default).into(holder.iv_media);
                        }catch (Exception e) {
                            Log.e("game", e.getMessage());
                            Glide.with(GalleryFragment.this.getActivity()).load(R.drawable.ic_photo_default).crossFade(0).placeholder(R.drawable.ic_photo_default).into(holder.iv_media);
                        }
                        break;
                    default:
                        holder.tv_time.setVisibility(View.INVISIBLE);
                        Glide.with(GalleryFragment.this.getActivity()).load(mediaInfo.filePath).crossFade(0).placeholder(R.drawable.ic_photo_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.iv_media);
                        break;
                }
            //}
        }
        @Override
        public int getItemCount() {
            return mediaInfoList.size();
        }
        class GalleryViewHolder extends RecyclerView.ViewHolder {
            View view;
            ImageView iv_media;
            TextView tv_index;
            TextView tv_name;
            TextView tv_size;
            TextView tv_time;
            TextView tv_date;
            TextView tv_path;
            ImageButton ib_preview;
            public GalleryViewHolder(View view) {
                super(view);
                iv_media = (ImageView) view.findViewById(R.id.iv_media);
                tv_index = (TextView) view.findViewById(R.id.tv_index);
                tv_name = (TextView) view.findViewById(R.id.tv_name);
                tv_size = (TextView) view.findViewById(R.id.tv_size);
                tv_time = (TextView) view.findViewById(R.id.tv_time);
                tv_date = (TextView) view.findViewById(R.id.tv_date);
                tv_path = (TextView) view.findViewById(R.id.tv_path);
                ib_preview = (ImageButton) view.findViewById(R.id.ib_preview);
                this.view = view;
            }
        }
    }
    public int getSelectedNumber(){
        int num = 0;
        for (MediaInfo mediaInfo:mediaInfoList){
            if (mediaInfo.selected){
                num++;
            }
        }
        return num;
    }
    public void removeOneSelect(int index){
        mUseGlide = false;
        for (MediaInfo mediaInfo : mediaInfoList) {
            if (mediaInfo.selected && mediaInfo.index > index) {
                mediaInfo.index++;
            }
        }
        int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        int lastPosition = mLinearLayoutManager.findLastVisibleItemPosition();
        View childrenView;
        for (int i = firstPosition; i <= lastPosition; i++) {
            childrenView = mLinearLayoutManager.findViewByPosition(i);
            MediaInfo mediaInfo = (MediaInfo)childrenView.getTag();
            if (childrenView != null && mediaInfo != null) {
                TextView tv_index = (TextView) childrenView.findViewById(R.id.tv_index);
                ImageButton ib_preview = (ImageButton) childrenView.findViewById(R.id.ib_preview);
                tv_index.setText(String.valueOf(mediaInfo.index));
                tv_index.setVisibility(mediaInfo.selected?View.VISIBLE:View.GONE);
                ib_preview.setVisibility(mediaInfo.selected?View.VISIBLE:View.GONE);
            }
        }
    }
    public void cancelSelect(){
        mUseGlide = false;
        for (MediaInfo mediaInfo:mediaInfoList){
            mediaInfo.index = 0;
            mediaInfo.selected = false;
        }
        mGalleryAdapter.notifyDataSetChanged();
        tv_cancel_select.setText(String.format(select_num, 0));
    }
    public void selectAll(){
        mUseGlide = false;
        int index   =   0;
        int num =   getSelectedNumber();
        for (MediaInfo mediaInfo:mediaInfoList){
            if (num==0){
                index ++;
                mediaInfo.index = index;
                mediaInfo.selected = true;
            }else{
                mediaInfo.index = 0;
                mediaInfo.selected = false;
            }
        }
        mGalleryAdapter.notifyDataSetChanged();
        tv_cancel_select.setText(String.format(select_num, index));
    }
    public void deleteSelected(){
        mUseGlide = false;
        int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        int lastPosition = mLinearLayoutManager.findLastVisibleItemPosition();
        View childrenView;
        for (int i = firstPosition; i <= lastPosition; i++) {
            childrenView = mLinearLayoutManager.findViewByPosition(i);
            MediaInfo mediaInfo = (MediaInfo)childrenView.getTag();
            if (childrenView != null && mediaInfo != null && mediaInfo.selected) {
                mGalleryAdapter.notifyItemRemoved(i);
            }
        }
        Iterator<MediaInfo> sListIterator = mediaInfoList.iterator();
        ArrayList<String> deleteFilePaths = new ArrayList<String>();
        while(sListIterator.hasNext()){
            MediaInfo mediaInfo = sListIterator.next();
            if(mediaInfo.selected){
                FileUtil.deleteFile(mediaInfo.filePath);
                sListIterator.remove();
                deleteFilePaths.add(mediaInfo.filePath);
            }
        }
        String[] arrString = (String[])deleteFilePaths.toArray(new String[deleteFilePaths.size()]) ;
        MediaScannerConnection.scanFile(MainApplication.getInstance(), arrString,
                null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(final String path, Uri uri) {

                    }
                });
        mGalleryAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCreateFile(CreateFileEvent createFileEvent) {
        String pathName = createFileEvent.fileName;
        File file = new File(pathName);
        MediaInfo mediaInfo = new MediaInfo(file);
        if(mediaInfo.valid) {
            mediaInfoList.add(0, mediaInfo);
            mGalleryAdapter.notifyItemInserted(0);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGalleryMediaInfoListUpdateEvent(GalleryMediaInfoListUpdateEvent galleryMediaInfoListUpdateEvent) {
        this.mediaInfoList = galleryMediaInfoListUpdateEvent.mediaInfoList;
        mGalleryAdapter.notifyDataSetChanged();
        srl_gallery.setRefreshing(false);
    }
}