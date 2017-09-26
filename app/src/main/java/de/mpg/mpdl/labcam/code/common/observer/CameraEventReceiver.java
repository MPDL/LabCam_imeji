package de.mpg.mpdl.labcam.code.common.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.List;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.code.common.service.TaskUploadService;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;


/**
 * Created by yingli on 12/16/15.
 */
public class CameraEventReceiver extends BroadcastReceiver implements UploadResultReceiver.Receiver{

    private String userId;
    private String serverName;

    @Override
    public void onReceive(Context context, Intent intent) {

        userId =  PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverName = PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

        //isAutoUpload
        Settings settings = DBConnector.getSettingsByUserId(userId);
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

        Image newImage = new Image();
        newImage.setImageName(imageName);
        newImage.setImagePath(imagePath);
        newImage.setLongitude(longitude);
        newImage.setLatitude(latitude);
        newImage.setCreateTime(createTime);
        newImage.setSize(fileSize);
        newImage.setUserId(userId);
        newImage.setServerName(serverName);
        newImage.save();

        //get current Task id

        Task task = DBConnector.getAuTask(userId, serverName);

        task.setTotalItems(task.getTotalItems() + 1);
        List<String> imagePaths = task.getImagePaths();
        imagePaths.add(imagePath);
        task.setImagePaths(imagePaths);
        task.setState(String.valueOf(DeviceStatus.state.WAITING));
        task.save();

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
