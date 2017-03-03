package de.mpg.mpdl.labcam.AutoRun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Upload.UploadResultReceiver;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


/**
 * Created by yingli on 12/16/15.
 */
public class CameraEventReceiver extends BroadcastReceiver implements UploadResultReceiver.Receiver{

    private String userId;
    private String serverName;
    private SharedPreferences mPrefs;
    static CountDownTimer timer =null;
    Toast toast;

    @Override
    public void onReceive(Context context, Intent intent) {


        mPrefs = context.getSharedPreferences("myPref", 0);
        userId = mPrefs.getString("userId","");
        serverName = mPrefs.getString("serverName","");

        //isAutoUpload
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();
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

        Cursor cursor = context.getContentResolver().query(intent.getData(), null, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex("_data"));

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

        //store image in local database
        String imageId = UUID.randomUUID().toString();
        Image newImage = new Image();
        newImage.setImageId(imageId);
        newImage.setImageName(imageName);
        newImage.setImagePath(imagePath);
        newImage.setLongitude(longitude);
        newImage.setLatitude(latitude);
        newImage.setCreateTime(createTime);
        newImage.setSize(fileSize);
        newImage.setState(imageState);
        newImage.setTaskId(DBConnector.getAuTask(userId,serverName).getTaskId());
        newImage.setUserId(userId);
        newImage.setServerName(serverName);
        newImage.save();

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
