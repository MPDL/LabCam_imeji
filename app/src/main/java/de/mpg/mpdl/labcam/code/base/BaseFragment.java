package de.mpg.mpdl.labcam.code.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.injection.component.ApplicationComponent;
import de.mpg.mpdl.labcam.code.injection.module.ActivityModule;
import de.mpg.mpdl.labcam.code.utils.ToastUtils;


public abstract class BaseFragment extends Fragment {
    private ProgressDialog progressDialog;
    private View mRootView;

    Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public Context getContext(){
        return mContext;
    }

    public void startActivity(Class<? extends Activity> activityClass) {
        startActivity(getLocalIntent(activityClass, null));
    }

    public void startActivity(Class<? extends Activity> activityClass, Bundle bd) {
        startActivity(getLocalIntent(activityClass, bd));
    }

    public void showMessage(Object msg) {
        Toast.makeText(getActivity(), msg + "", Toast.LENGTH_SHORT).show();
    }

    public void showLongMessage(String msg) {
        ToastUtils.showLongMessage(getActivity(), msg);
    }

    public void showToast(String msg) {
        ToastUtils.showShortMessage(getActivity(), msg);
    }

    public void showToast(int msgId){
        ToastUtils.showLongMessage(getActivity(),msgId);
    }

    public Intent getLocalIntent(Class<? extends Context> localIntent, Bundle bd) {
        Intent intent = new Intent(getActivity(), localIntent);
        if (null != bd) {
            intent.putExtras(bd);
        }
        return intent;
    }

    protected Intent getBackOnNewIntent(){
        Intent intent = getActivity().getIntent();
        try {
            intent.setClass(getActivity(), Class.forName(intent.getStringExtra(Constants.KEY_CLASS_NAME)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    protected Intent getStartOnNewIntent(Class activityClass){
        Intent intent = new Intent();
        intent.setClass(getActivity(), activityClass);
        intent.putExtra(Constants.KEY_CLASS_NAME, getActivity().getClass().getName());
        return intent;
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((BaseApplication)getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    public void showLoading(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.setMessage("");
        progressDialog.show();
    }

    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
            ButterKnife.bind(this, mRootView);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        initContentView(savedInstanceState);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected abstract int getLayoutId();

    protected abstract void initContentView(Bundle savedInstanceState);

}
