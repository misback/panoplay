package com.uni.pano.widget;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.uni.common.env.Env;
import com.uni.common.util.DipPixelUtil;
import com.uni.common.util.ResourceUtil;

/**
 * 通用的Toast
 * PS:原则上我们程序中使用Toast都使用这个定制类
 */
public class CToast {
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    private Toast mToast;

    /**
     * 构造函数
     *
     * @param context
     * @param text
     * @param duration
     */
    protected CToast(Context context, CharSequence text, int duration) {
        mToast = Toast.makeText(context, text, duration);

        // PS: 后面的偏移量针对前面的Gravity来设置的
        mToast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, DipPixelUtil.dip2px(Env.getContext(), 90));

    }

    /**
     * 构造新的CDToast
     *
     * @param context
     * @param text     Toast需要显示的文案
     * @param duration Toast显示的时长，两种：CToast.LENGTH_SHORT/CDToast.LENGTH_LONG,对应Toast相应的那两种
     * @return
     */
    public static CToast makeText(Context context, CharSequence text, int duration) {
        CToast backToast = new CToast(context, text, duration);

        return backToast;
    }

    /**
     * 构造新的CDToast
     *
     * @param context
     * @param resId    Toast需要显示的文案的资源ID
     * @param duration Toast显示的时长，使用Toast以前的定义的那两种：Toast.LENGTH_SHORT/Toast.LENGTH_LONG;
     * @return
     */
    public static CToast makeText(Context context, int resId, int duration) {
        CharSequence charSequence = Env.getContext().getText(resId);
        CToast backToast = new CToast(context, charSequence, duration);

        return backToast;
    }

    /**
     * 简易函数：显示toast
     */
    public static void showToast(String msg) {
        CToast.makeText(Env.getContext(), msg, CToast.LENGTH_SHORT).show();
    }

    /**
     * 简易函数：显示toast
     */
    public static void showToast(int resid) {
        String msg = ResourceUtil.getString(resid);
        CToast.makeText(Env.getContext(), msg, CToast.LENGTH_SHORT).show();
    }

    /**
     * 显示Toast
     */
    public void show() {
        mToast.show();
    }

    // del: 会导致线程异常
    /**
     * 将当前线程的副本添加到本地线程变量中,加入消息队列中。
     * @param msg
     */
//	public static void displayToast(String msg){
//		if(Looper.myLooper() == null){
//			Looper.loop();
//		}
//		showToast(msg);
//		Looper.loop();
//	}

    // del: 会导致线程异常
    /**
     * 在子线程中Toast.
     * @param resid
     */
//	public static void displayToast(int resid){
//		if(Looper.myLooper() == null){
//			Looper.loop();
//		}
//		showToast(resid);
//		Looper.loop();
//	}

}
