package com.uni.pano;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import com.uni.common.env.Env;
import com.uni.common.util.PreferenceModel;
import com.uni.pano.config.EnumElement;
import com.uni.pano.utils.CommonUtil;

public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    public static boolean bSensorRotate = false;
    private static MainApplication instance;
    static {
        System.loadLibrary("unipano");
    }
    public static EnumElement.RENDER_MODE render_mode = EnumElement.RENDER_MODE.SINGLE;
    public boolean mCameraConnectStatus =   false;
    public void updateRotationByConnectStatus(Activity activity){
        CommonUtil.openRotateScreenInSetting(this);
        if (mCameraConnectStatus){
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }else{
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    public static synchronized MainApplication getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        bSensorRotate = CommonUtil.isHaveSensorRotate(this);
        Env.setContext(this);
        Env.setAppStartTime();
        iniPreference();
    }
    private void iniPreference() {
        PreferenceModel.init(this);
    }
}