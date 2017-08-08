package de.mpg.mpdl.labcam.code.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;

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


public abstract class BaseMvpWithPhotoActivity<T extends BasePresenter> extends BaseCompatActivity implements
                                                                                                   BaseView, TakePhoto.TakeResultListener, InvokeListener {
    protected TakePhoto mTakePhoto;

    protected InvokeParam mInvokeParam;

    protected String mTempFileName;

    protected File mFileTemp;

    @Inject
    protected T mPresenter;

    protected abstract void injectComponent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        injectComponent();
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.stop();
    }

    @Override
    protected void onDestroy() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionsResult(this, type, mInvokeParam, this);
    }

    public TakePhoto getTakePhoto() {
        if (mTakePhoto == null) {
            mTakePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, this));
            CompressConfig compressConfig=new CompressConfig.Builder().setMaxSize(50*1024).setMaxPixel(800).create();
            mTakePhoto.onEnableCompress(compressConfig,true);
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
            mFileTemp = new File(getFilesDir(), mTempFileName);
        }
    }

    public void showPhotoDialog() {
        PhotoDialog.Builder builder = new PhotoDialog.Builder(this);
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
