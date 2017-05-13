package com.uni.vr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;

import com.uni.common.config.PathConfig;
import com.uni.common.util.FileUtil;
import com.uni.common.util.PreferenceModel;
import com.uni.pano.config.EnumElement;
import com.uni.pano.event.ScreenShotEvent;
import com.uni.pano.event.VideoGLViewCreateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;

public class PanoramaVideoView extends GLSurfaceView {
    public final static String TAG  =   PanoramaVideoView.class.getSimpleName();
    private PanoramaVideoRender mPanoramaVideoRender;
    protected String mFilePath;
    protected AssetManager mAssetManager;
    public PanoramaVideoView(Context context) {
        this(context, null);
    }
    public PanoramaVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        nativeOnCreate(this);
        String logoFilePath = PreferenceModel.getString("logoFileName", "pano_logo_alpha.png");
        if(logoFilePath.lastIndexOf("/") == -1){
            nativeLoadLogoImage(context.getAssets(), logoFilePath);
        }else {
            if(FileUtil.isFileExist(logoFilePath)) {
                nativeLoadLogoImage(null, logoFilePath);
            }else{
                PreferenceModel.putString("logoFileName", "pano_logo_alpha.png");
                nativeLoadLogoImage(context.getAssets(), "pano_logo_alpha.png");
            }
        }
        int zoom = PreferenceModel.getInt("ZOOM", 15);
        nativeUpdateLogoAngle(zoom);
        init();
    }
    protected void init() {
        setEGLContextClientVersion(2);
        mPanoramaVideoRender = new PanoramaVideoRender();
        setRenderer(mPanoramaVideoRender);
        initGesture();
    }
    public void registerOnProgressCallBack(Activity activity){
        this.nativeSetOnProgressCallback(activity);
    }
    @Override
    public void onResume() {
        super.onResume();
        this.setRenderMode(RENDERMODE_CONTINUOUSLY);
        EventBus.getDefault().register(this);
        openSensor();
        doSetPlaying(true);
        mPanoramaVideoRender.nativeOnResume();
    }
    @Override
    public void onPause() {
        mPanoramaVideoRender.nativeOnPause();
        doSetPlaying(false);
        closeSensor();
        EventBus.getDefault().unregister(this);
        this.setRenderMode(RENDERMODE_WHEN_DIRTY);
        super.onPause();
    }

    public void onDestroy(){
        mPanoramaVideoRender.onDestroy();
        nativeOnDestroy();
    }
    public void loadVideo(String filePath, AssetManager assetManager){
        mFilePath       =   filePath;
        mAssetManager   =   assetManager;
        final SurfaceTexture st = mPanoramaVideoRender.getSurfaceTexture();
        if(st != null){
            runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    Surface s = new Surface(st);
                    nativeLoadVideo(s, mAssetManager, mFilePath);
                    s.release();
                }
            });
        }
    }
    public void runOnGLThread(final Runnable pRunnable) {
        this.queueEvent(pRunnable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VideoGLViewCreateEvent videoGLViewCreateEvent) {

        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                SurfaceTexture st = mPanoramaVideoRender.getSurfaceTexture();
                Surface s = new Surface(st);
                nativeLoadVideo(s, mAssetManager, mFilePath);
                s.release();
            }
        });
    }
    private EnumElement.RENDER_MODE render_mode = EnumElement.RENDER_MODE.SINGLE;
    public void doUpdateRenderMode(EnumElement.RENDER_MODE renderMode){
        render_mode = renderMode;
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeUpdateRenderMode(render_mode.ordinal());
            }
        });
    }
    private EnumElement.VIEW_MODE view_mode = EnumElement.VIEW_MODE.FISH;
    public void doUpdateViewMode(EnumElement.VIEW_MODE viewMode){
        view_mode = viewMode;
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeUpdateViewMode(view_mode.ordinal());
            }
        });
    }
    private EnumElement.OPTION_MODE option_mode = EnumElement.OPTION_MODE.FINGER;
    public void doUpdateOptionMode(EnumElement.OPTION_MODE optionMode){
        option_mode = optionMode;
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeUpdateOptionMode(option_mode.ordinal());
            }
        });
        if(option_mode == EnumElement.OPTION_MODE.GYROSCOPE ){
            openSensor();
        }else{
            closeSensor();
        }
    }
    public void doSetPlaying(final boolean isPlaying){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeSetPlaying(isPlaying);
            }
        });
    }
    public void doUpdateProgress(final float progress){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeUpdateProgress(progress);
            }
        });
    }
    public void doRestart(){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeRestart();
            }
        });
    }
    public void doUpdateLutFilter(final AssetManager assetManager, final String filePath, final EnumElement.FILTER_TYPE filterType){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeUpdateLutFilter(assetManager, filePath, filterType.ordinal());
            }
        });
    }

    public void doLoadLogoImage(final AssetManager assetManager, final String logoFilePath){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                if(logoFilePath.lastIndexOf("/") == -1){
                    nativeLoadLogoImage(assetManager, logoFilePath);
                }else {
                    nativeLoadLogoImage(null, logoFilePath);
                }
            }
        });
    }

    public void doScreenShot(final Context context){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeScreenShot(PathConfig.getScreenShotDir());
            }
        });
    }

    public void doUpdateLogoAngle(final float angle){
        runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeUpdateLogoAngle(angle);
            }
        });
    }

    public void callbackScreenShot(final String fileName){
        EventBus.getDefault().post(new ScreenShotEvent(fileName));
    }

    /** Native methods, implemented in jni folder */
    private static native void nativeOnCreate(PanoramaVideoView panoramaVideoView);
    private static native void nativeLoadVideo(Surface surface, AssetManager assetMgr, String filename);
    private static native void nativeLoadLogoImage(AssetManager assetManager, String filePath);
    private static native void nativeSetPlaying(boolean isPlaying);
    private static native void nativeSetOnProgressCallback(final Activity callback);
    private static native void nativeOnDestroy();
    private static native void nativeScreenShot(String screenDir);
    private static native void nativeUpdateProgress(float progress);
    private static native void nativeRestart();
    private static native void nativeUpdateScale(float scale);
    private static native void nativeUpdateRotate(float xMove, float yMove);
    private static native void nativeUpdateRotateFling(float velocityX, float velocityY);
    private static native void nativeUpdateSensorRotate(float[] sRotateMat);
    private static native void nativeUpdateOptionMode(int optionMode);
    private static native void nativeUpdateViewMode(int viewMode);
    private static native void nativeUpdateRenderMode(int vrMode);
    private static native void nativeUpdateLutFilter(AssetManager am, String filePath, int filterType);
    private static native void nativeUpdateLogoAngle(float angle);
    private OnClickListener mOnClickListener;
    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }
    public void initGesture(){
        setLongClickable(true);
        setOnTouchListener(new TouchTracker(getContext(), new TouchTracker.OnGestureListener() {
            @Override
            public boolean onClick() {
                eCallOnClick();
                return true;
            }

            @Override
            public boolean onScale(float angle) {
                nativeUpdateScale(angle);
                return true;
            }

            @Override
            public boolean onDrag(float yaw, float pitch) {
                nativeUpdateRotate(yaw, pitch);
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                nativeUpdateRotateFling(-velocityX/100.f, -velocityY/100.f);
                return true;
            }

            private void eCallOnClick() {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(PanoramaVideoView.this);
                }
            }
        }));
    }
    private SensorManager sensorManager = null;
    private Sensor sensor = null;
    private RotateSensorEventListener rotateSensorEventListener = new RotateSensorEventListener();
    private final float[] rotationMatrix = new float[16];
    public class RotateSensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            nativeUpdateSensorRotate(rotationMatrix);
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
    public void openSensor(){
        if(option_mode == EnumElement.OPTION_MODE.FINGER){
            return;
        }
        if (sensorManager == null) {
            sensorManager = (SensorManager) this.getContext().getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensor == null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        }
        if(sensor != null) {
            sensorManager.registerListener(rotateSensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }
    public void closeSensor(){
        if (sensor != null) {
            sensorManager.unregisterListener(rotateSensorEventListener);
            sensor     =  null;
        }
    }
}