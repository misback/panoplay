package com.uni.vr;

import android.app.usage.UsageEvents;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.uni.pano.event.VideoGLViewCreateEvent;

import org.greenrobot.eventbus.EventBus;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by DELL on 2017/3/13.
 */

public class PanoramaVideoRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{
    private final static String TAG 				= 	PanoramaVideoRender.class.getSimpleName();
    private SurfaceTexture mSurface               =   null;
    private boolean updateSurface = false;
    private float width;
    private float height;
    private float photoWidth    =   2560;
    private float photoHeight   =   1280;
    private boolean bDestroy    =   false;
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        EGLDisplay mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1);
        android.opengl.EGLConfig eglConfig = null;
        int[] configsCount = new int[1];
        android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
        int[] configSpec = new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 0,
                EGL14.EGL_STENCIL_SIZE, 0,
                EGL14.EGL_NONE
        };
        if (!EGL14.eglChooseConfig(mEGLDisplay, configSpec, 0, configs, 0, configs.length, configsCount, 0)) {
            throw new IllegalArgumentException("Failed to choose config: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        nativeInitGL(width,height,photoWidth,photoHeight);
        mSurface = new SurfaceTexture(nativeGetTextureId());
        mSurface.setOnFrameAvailableListener(this);
        EventBus.getDefault().post(new VideoGLViewCreateEvent());
        synchronized(this) {
            updateSurface = false;
        }
    }
    public void onDestroy(){
        bDestroy = true;
    }
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        if(bDestroy){
            return;
        }
        this.width = width;
        this.height = height;
        nativeOnSurfaceChanged(width, height);
    }
    @Override
    public void onDrawFrame(GL10 glUnused) {
        if(bDestroy){
            return;
        }
        synchronized(this) {
            if (updateSurface) {
                mSurface.updateTexImage();
                updateSurface = false;
            }
        }
        nativeDrawFrame();
    }
    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        /* For simplicity, SurfaceTexture calls here when it has new
         * data available.  Call may come in from some random thread,
         * so let's be safe and use synchronize. No OpenGL calls can be done here.
         */
        updateSurface = true;
    }
    public SurfaceTexture getSurfaceTexture() {
        return mSurface;
    }
    public static native int  nativeGetTextureId();
    public static native void nativeInitGL(float widgetWidth, float widgetHeight, float takePhotoWidth, float takePhotoHeight);
    public static native void nativeOnSurfaceChanged(int pNewSurfaceWidth, int pNewSurfaceHeight);
    public static native void nativeDrawFrame();
    public static native void nativeOnResume();
    public static native void nativeOnPause();
}
