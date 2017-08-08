package de.mpg.mpdl.labcam.code.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;

import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.PhotoDialog;
import de.mpg.mpdl.labcam.code.injection.component.ApplicationComponent;
import de.mpg.mpdl.labcam.code.injection.module.ActivityModule;
import de.mpg.mpdl.labcam.code.utils.ToastUtils;

import java.io.File;


public class BaseWithPhotoFragment extends Fragment implements TakePhoto.TakeResultListener, InvokeListener {
    protected ProgressDialog progressDialog;

    protected InvokeParam mInvokeParam;

    private TakePhoto mTakePhoto;

    protected String mTempFileName;

    protected File mFileTemp;

    Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionsResult(getActivity(), type, mInvokeParam, this);
    }

    public TakePhoto getTakePhoto() {
        if (mTakePhoto == null) {
            mTakePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, this));
            CompressConfig compressConfig = new CompressConfig.Builder().setMaxSize(50 * 1024).setMaxPixel(800).create();
            mTakePhoto.onEnableCompress(compressConfig, true);
        }
        return mTakePhoto;
    }

    @Override
    public void takeSuccess(TResult result) {
    }

    @Override
    public void takeFail(TResult result, String msg) {
    }

    @Override
    public void takeCancel() {
    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.getMethod());
        if (PermissionManager.TPermissionType.WAIT.equals(type)) {
            this.mInvokeParam = invokeParam;
        }
        return type;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void startActivity(Class<? extends Activity> activityClass) {
        startActivity(getLocalIntent(activityClass, null));
        //   me.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void startActivity(Class<? extends Activity> activityClass, Bundle bd) {
        startActivity(getLocalIntent(activityClass, bd));
        //    me.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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

    public void showToast(int msgId) {
        ToastUtils.showLongMessage(getActivity(), msgId);
    }

    public Intent getLocalIntent(Class<? extends Context> localIntent, Bundle bd) {
        Intent intent = new Intent(getActivity(), localIntent);
        if (null != bd) {
            intent.putExtras(bd);
        }
        return intent;
    }

    protected Intent getBackOnNewIntent() {
        Intent intent = getActivity().getIntent();
        try {
            intent.setClass(getActivity(), Class.forName(intent.getStringExtra(Constants.KEY_CLASS_NAME)));
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    protected Intent getStartOnNewIntent(Class activityClass) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), activityClass);
        intent.putExtra(Constants.KEY_CLASS_NAME, getActivity().getClass().getName());
        return intent;
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((BaseApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    public void showLoading(String msg) {
        if (progressDialog == null) {
//            progressDialog = CustomizedProgressDialog.createInstance(getActivity());
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public void showLoading() {
        if (progressDialog == null) {
//            progressDialog = CustomizedProgressDialog.createInstance(getActivity());
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setMessage("");
        progressDialog.show();
    }

    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void callAlbum() {
        mTakePhoto.onPickFromGallery();
    }

    protected void callCamera() {
        createTempFile();
        mTakePhoto.onPickFromCapture(Uri.fromFile(mFileTemp));
    }

    private void createTempFile() {
        mTempFileName = System.currentTimeMillis() + ".png";
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp =
                    new File(Environment.getExternalStorageDirectory(),
                             mTempFileName);
        }
        else {
            mFileTemp = new File(getActivity().getFilesDir(), mTempFileName);
        }
    }

    public void showPhotoDialog() {
        PhotoDialog.Builder builder = new PhotoDialog.Builder(getActivity());
        builder.setPhotoAlbumButton(
                getResources().getString(R.string.photo_album),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        callAlbum();
                        dialog.dismiss();

                    }
                });
        builder.setCameraButton(getResources().getString(R.string.camera),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        callCamera();
                                        dialog.dismiss();

                                    }
                                });
        builder.setCancelButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
        builder.create().show();

    }

}
