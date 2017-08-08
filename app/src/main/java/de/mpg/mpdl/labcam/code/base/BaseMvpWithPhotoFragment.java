package de.mpg.mpdl.labcam.code.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import de.mpg.mpdl.labcam.code.common.widget.PhotoDialog;

import java.io.File;

import javax.inject.Inject;

public abstract class BaseMvpWithPhotoFragment<T extends BasePresenter> extends BaseFragment implements
                                                                                             BaseView, TakePhoto.TakeResultListener, InvokeListener {

    protected InvokeParam mInvokeParam;

    protected TakePhoto mTakePhoto;

    protected String mTempFileName;

    protected File mFileTemp;

    @Inject
    protected T mPresenter;

    protected abstract void injectComponent();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        injectComponent();
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
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public void onError(String msg) {
        showToast(msg);
    }

    @Override
    public void onSuccess(String msg) {
        showToast(msg);
    }

    @Override
    public void onThrowable(Throwable e) {

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

    @Override
    public void onSuccess() {

    }
}
