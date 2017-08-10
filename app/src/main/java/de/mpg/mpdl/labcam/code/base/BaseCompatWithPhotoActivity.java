package de.mpg.mpdl.labcam.code.base;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;

import java.io.File;

import butterknife.ButterKnife;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.widget.PhotoDialog;

@SuppressLint("NewApi")
public abstract class BaseCompatWithPhotoActivity extends BaseActivity implements TakePhoto.TakeResultListener, InvokeListener {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected InvokeParam mInvokeParam;

    protected TakePhoto mTakePhoto;

    protected String mTempFileName;

    protected File mFileTemp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        super.setContentView(getLayoutId());
        ButterKnife.bind(this);
        initContentView(savedInstanceState);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    protected abstract int getLayoutId();

    protected abstract void initContentView(Bundle savedInstanceState);

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
        PermissionManager.handlePermissionsResult(this, type, mInvokeParam, this);
    }

    /**
     * 获取TakePhoto实例
     *
     * @return
     */
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


}
