package com.uni.pano.share;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.uni.pano.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.uni.pano.widget.CToast;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 分享面板对话框,通过自定义dialog实现
 * <p>
 * <p>通过监听器{@link OnSharePanelItemClickListener}回调选择的社交平台,将具体的实现交给外部处理</p>
 * <p>
 * Created by ZachLi on 2016/6/28.
 */
public class ShareDialog extends CBaseDialog {

    private View dlgView;
    private OnSharePanelItemClickListener mOnSharePanelItemClickListener;

    public ShareDialog(Context context) {
        super(context, R.style.share_dialog);
        initUI();
    }

    public void fullScreen() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * ui初始化
     */
    private void initUI() {
        dlgView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share, null);
        ButterKnife.bind(this, dlgView);
        // 设置对话框内容
        setContentView(dlgView);

    }

    @OnClick(R.id.top_space_view)
    public void doCloseClick(View view) {
        dismiss();
    }

    @OnClick({R.id.share_wechat_layout, R.id.share_wxcircle_layout,
            R.id.share_qq_layout, R.id.share_qzone_layout,
            R.id.share_sina_layout, R.id.share_twitter_layout,
            R.id.share_facebook_layout, R.id.ll_youtube})
    public void doShareClick(View view) {

        SHARE_MEDIA media = null;

        // 通过被点击的view来选定分享平台
        switch (view.getId()) {
            case R.id.share_wechat_layout:
                media = SHARE_MEDIA.WEIXIN;
                break;
            case R.id.share_wxcircle_layout:
                media = SHARE_MEDIA.WEIXIN_CIRCLE;
                break;
            case R.id.share_qq_layout:
                media = SHARE_MEDIA.QQ;
                break;
            case R.id.share_qzone_layout:
                media = SHARE_MEDIA.QZONE;
                break;
            case R.id.share_sina_layout:
                media = SHARE_MEDIA.SINA;
                break;
            case R.id.share_twitter_layout:
                media = SHARE_MEDIA.TWITTER;
                break;
            case R.id.share_facebook_layout:
            case R.id.ll_youtube: {
                media = SHARE_MEDIA.FACEBOOK;
                CToast.showToast(R.string.not_function);
                return;
            }
            default:
                break;
        }

        // 分享面板点击事件有被注册且点击了面板上的某一项
        if (mOnSharePanelItemClickListener != null && media != null) {

            mOnSharePanelItemClickListener.onSharePanelItemClick(this, media);
        }

    }

    /**
     * 设置分享面板项点击监听器
     *
     * @param l 分享面板项目点击监听器
     */
    public CBaseDialog setOnSharePanelItemClickListener(OnSharePanelItemClickListener l) {
        this.mOnSharePanelItemClickListener = l;
        return this;
    }


    /**
     * 分享面板项目点击监听器
     */
    public interface OnSharePanelItemClickListener {

        /**
         * 注册回调用于执行平台的点击事件
         *
         * @param dialog 接收点击事件的对话框
         * @param media  社交平台
         */
        public void onSharePanelItemClick(Dialog dialog, SHARE_MEDIA media);
    }


}
