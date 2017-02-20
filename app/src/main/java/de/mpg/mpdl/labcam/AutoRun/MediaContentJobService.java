package de.mpg.mpdl.labcam.AutoRun;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Upload.UploadResultReceiver;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.data.db.LiteOrmManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by yingli on 2/14/17.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MediaContentJobService extends JobService implements UploadResultReceiver.Receiver{

    private String userId;
    private String serverName;
    private SharedPreferences mPrefs;
    private Context context = this;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onStartJob(JobParameters params) {
        StringBuilder sb = new StringBuilder();
        sb.append("Media content has changed:\n");
        if (params.getTriggeredContentAuthorities() != null) {
            sb.append("Authorities: ");
            boolean first = true;
            for (String auth :
                    params.getTriggeredContentAuthorities()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(auth);
            }
            if (params.getTriggeredContentUris() != null) {
                for (Uri uri : params.getTriggeredContentUris()) {
                    sb.append("\n");
                    sb.append(uri);
                    cameraEventHandling(convertMediaUriToPath(uri));
                }
            }
        } else {
            sb.append("(No content)");
        }
        Log.i("MediaContentJobService", sb.toString());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    protected String convertMediaUriToPath(Uri uri) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj,  null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }


    private void cameraEventHandling(String imagePath){
        mPrefs = context.getSharedPreferences("myPref", 0);
        userId = mPrefs.getString("userId","");
        serverName = mPrefs.getString("server","");

        //isAutoUpload
        Settings settings = DBConnector.getSettingsByUserId(context, userId);
        // if auto is turned off
        if (settings == null){
            Log.e("cameraEvent","settings is null");
            return;
        } else if(!settings.isAutoUpload()){
            Log.e("cameraEvent","!isAutoUpload");
            return;
        } else if(DBConnector.getAuTask(userId,serverName)==null){ //no auto task
            Log.e("cameraEvent","can't get auTask");
            return;
        }


        String imageName = imagePath.substring(imagePath.lastIndexOf('/') + 1);

        //imageSize
        File file = new File(imagePath);
        String fileSize = String.valueOf(file.length() / 1024);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //createTime
        String createTime = exif.getAttribute(ExifInterface.TAG_DATETIME);

        //latitude
        String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

        //longitude
        String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

        //state
        String imageState = String.valueOf(DeviceStatus.state.WAITING);

        //taskId


//        Toast.makeText(context, "New Photo:" + imageName + "is Saved as : -" + imagePath, Toast.LENGTH_LONG).show();

        Log.i("CameraEventReceiver", imageName);
        Log.i("CameraEventReceiver",imagePath );
        Log.i("CameraEventReceiver", fileSize + "kb");
        Log.i("CameraEventReceiver", String.valueOf(createTime));


        //store image in local database

        String imageId = UUID.randomUUID().toString();
        Image photo = new Image();
        photo.setImageId(imageId);
        photo.setImageName(imageName);
        photo.setImagePath(imagePath);
        photo.setLongitude(longitude);
        photo.setLatitude(latitude);
        photo.setCreateTime(createTime);
        photo.setSize(fileSize);
        photo.setState(imageState);
        photo.setTaskId(DBConnector.getAuTask(userId,serverName).getTaskId());
        photo.save();

        //get current Task id

        Task task = new Select().from(Task.class).where("uploadMode = ?","AU").orderBy("startDate DESC").executeSingle();

        task.setTotalItems(task.getTotalItems() + 1);
        task.setState(String.valueOf(DeviceStatus.state.WAITING));
        task.save();
        Log.e("<>", task.getTotalItems()+"");

        Log.v("taskId_task", DBConnector.getAuTask(userId,serverName).getTaskId());
        Log.v("taskId_Image", DBConnector.getImage().getTaskId());
        Log.v("taskNum", DBConnector.getAuTask(userId,serverName).getTotalItems() + "");


        // start service when finished item 0, total item 1
        if(task.getTotalItems()==1 && task.getFinishedItems() == 0) {
            UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent uploadIntent = new Intent(context, TaskUploadService.class);
            uploadIntent.putExtra("receiver", mReceiver);
            context.startService(uploadIntent);
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

    }
}
