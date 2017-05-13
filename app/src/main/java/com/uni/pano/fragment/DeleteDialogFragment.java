package com.uni.pano.fragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.uni.pano.R;
import com.uni.pano.base.BaseDialogFragment;
import com.uni.pano.event.DeleteFilesEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

public class DeleteDialogFragment extends BaseDialogFragment {
    public final static String TAG = DeleteDialogFragment.class.getSimpleName();
    @BindView(R.id.tv_sure)
    TextView tv_sure;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.tv_content)
    TextView tv_content;
    private String delete_file_prompt;

    public static void show(int select_number, DeleteListener deleteListener, FragmentManager fragmentManager){
        DeleteDialogFragment deleteDialogFragment = new DeleteDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("FileNumber", select_number);
        deleteDialogFragment.setArguments(bundle);
        deleteDialogFragment.setDeleteListener(deleteListener);
        deleteDialogFragment.show(fragmentManager, DeleteDialogFragment.TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void initView() {
        delete_file_prompt  =   getString(R.string.delete_file_prompt);
        tv_content.setText(String.format(delete_file_prompt, getArguments().getInt("FileNumber", 0)));
    }
    @OnClick({R.id.tv_sure, R.id.tv_cancel})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_sure:
                if(this.deleteListener!= null){
                    this.deleteListener.onYes();
                }
                break;
            case R.id.tv_cancel:
                break;
        }
        dismiss();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        viewInject(inflater, container, R.layout.dfm_delete);
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public void setDeleteListener(DeleteListener deleteListener){
        this.deleteListener = deleteListener;
    }
    private DeleteListener deleteListener;
    public interface DeleteListener{
        public void onYes();
    }
}
