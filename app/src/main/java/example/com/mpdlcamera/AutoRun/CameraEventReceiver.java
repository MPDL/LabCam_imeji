package example.com.mpdlcamera.AutoRun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import example.com.mpdlcamera.Folder.UploadService;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.Utils.DeviceStatus;
import example.com.mpdlcamera.Utils.ImageFileFilter;


/**
 * Created by yingli on 12/16/15.
 */
public class CameraEventReceiver extends BroadcastReceiver implements UploadResultReceiver.Receiver{
    @Override
    public void onReceive(Context context, Intent intent) {

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

        //taskId


        Toast.makeText(context, "New Photo:" + imageName + "is Saved as : -" + imagePath, Toast.LENGTH_LONG).show();

        Log.i("CameraEventReceiver", imageName);
        Log.i("CameraEventReceiver",imagePath );
        Log.i("CameraEventReceiver", fileSize + "kb");
        Log.i("CameraEventReceiver", String.valueOf(createTime));

        try{

        //store image in local database
        Image photo = new Image();
        photo.setImageName(imageName);
        photo.setImagePath(imagePath);
        photo.setLongitude(longitude);
        photo.setLatitude(latitude);
        photo.setCreateTime(createTime);
        photo.setSize(fileSize);
        photo.setState(imageState);
        photo.setTaskId(getTask().getTaskId());
        photo.save();

        //get current Task id


            Task task = new Select().from(Task.class).where("uploadMode = ?","AU").orderBy("startDate DESC").executeSingle();

            task.setTotalItems(task.getTotalItems() + 1);

            task.save();

            Log.v("taskid", getTask().getTaskId());
            Log.v("taskId", getImage().getTaskId());
            Log.v("taskNum", getTask().getTotalItems() + "");

            UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            Intent uploadIntent = new Intent(context, TaskUploadService.class);
            intent.putExtra("receiver", mReceiver);
            context.startService(uploadIntent);

        }catch (Exception e){
            Log.v("CameraEventReceiver","didn't set task");
        }

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

    }

    //get latest task
    public static Task getTask() {
        return new Select()
                .from(Task.class)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    //get latest image
    public static Image getImage() {
        return new Select()
                .from(Image.class)
                .orderBy("createTime DESC")
                .executeSingle();
    }
}
