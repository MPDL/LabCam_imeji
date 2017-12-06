package de.mpg.mpdl.labcam.code.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import de.mpg.mpdl.labcam.code.common.AppManager;
import de.mpg.mpdl.labcam.code.injection.component.ApplicationComponent;
import de.mpg.mpdl.labcam.code.injection.module.ActivityModule;
import de.mpg.mpdl.labcam.code.utils.ToastUtils;


/**
 * Base BaseActivity
 */
@SuppressLint("NewApi")
public class BaseActivity extends RxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        this.getApplicationComponent().inject(this);
    }

    /**
     * start activity without parameters
     *
     * @param activityClass
     */
    public void startActivity(Class<? extends Activity> activityClass) {
        startActivity(getLocalIntent(activityClass, null));
    }

    /**
     * start activity with parameters
     *
     * @param activityClass
     */
    public void startActivity(Class<? extends Activity> activityClass,
                              Bundle bd) {
        startActivity(getLocalIntent(activityClass, bd));
    }

    /**
     *
     * @param activityClass
     */
    public void startActivityForResult(Class<? extends Activity> activityClass, int requestCode, Bundle bd) {
        startActivityForResult(getLocalIntent(activityClass, bd), requestCode);
    }

    /**
     * Method show a toast, ca 3s.
     *
     * @param msg
     */
    public void showMessage(Object msg) {
        Toast.makeText(this, msg + "", Toast.LENGTH_SHORT).show();
    }

    /**
     * Method show long toast, ca 5s.
     *
     * @param msg
     */
    public void showLongMessage(String msg) {
        ToastUtils.showLongMessage(this, msg);
    }

    /**
     * Method show toast (avoid duplicates)
     *
     * @param msg
     */
    public void showToast(String msg) {
        ToastUtils.showShortMessage(this, msg);
    }

    public void showToast(int msgId) {
        ToastUtils.showLongMessage(this, msgId);
    }

    @Override
    protected void onDestroy() {
        AppManager.getAppManager().finishActivity(this);

        super.onDestroy();
    }

    /**
     * Method to get Local Intent
     *
     * @param localIntent
     * @return
     */
    public Intent getLocalIntent(Class<? extends Context> localIntent,
                                 Bundle bd) {
        Intent intent = new Intent(this, localIntent);
        if (null != bd) {
            intent.putExtras(bd);
        }
        return intent;
    }

    /**
     * Get the Main Application component for dependency injection.
     */
    protected ApplicationComponent getApplicationComponent() {
        return ((BaseApplication) getApplication()).getApplicationComponent();
    }

    /**
     * Get an Activity module for dependency injection.
     */
    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected View getContentView() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        FrameLayout
                content =
                (FrameLayout) view.findViewById(android.R.id.content);
        return content.getChildAt(0);
    }

    private ProgressDialog progressDialog;


    public void showLoading(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("");
        progressDialog.show();
    }

    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
