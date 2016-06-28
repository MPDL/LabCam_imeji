package example.com.mpdlcamera.AutoRun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Settings;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.Utils.DeviceStatus;


/**
 * Created by yingli on 12/16/15.
 */
public class CameraEventReceiver extends BroadcastReceiver implements UploadResultReceiver.Receiver{

    private String userId;
    private SharedPreferences mPrefs;
    static CountDownTimer timer =null;
    Toast toast;

    @Override
    public void onReceive(Context context, Intent intent) {


        mPrefs = context.getSharedPreferences("myPref", 0);
        userId = mPrefs.getString("userId","");

//        // customized toast
//        LinearLayout  layout = new LinearLayout(context);
//
//
//        TextView  tv =new TextView (context);
//        // set the TextView properties like color, size etc
//        tv.setTextColor(Color.RED);
//        tv.setTextSize(15);
//
//        tv.setGravity(Gravity.CENTER_VERTICAL);
//
//        // set the text you want to show in  Toast
//        tv.setText("Automatic upload to "+DeviceStatus.getAuTask(userId).getCollectionName());
//
//        ImageView img=new ImageView(context);
//
//        // give the drawble resource for the ImageView
////        img.setImageResource(R.drawable.myimage);
//
//        // add both the Views TextView and ImageView in layout
//        layout.addView(img);
//        layout.addView(tv);
//
//        toast=new Toast(context); //context is object of Context write "this" if you are an Activity
//        // Set The layout as Toast View
//        toast.setView(layout);
//
//        // Position you toast here toast position is 50 dp from bottom you can give any integral value
//        toast.setGravity(Gravity.TOP, 0, 50);
//
//        timer =new CountDownTimer(20000, 1000)
//        {
//            public void onTick(long millisUntilFinished)
//            {
//                toast.show();
//            }
//            public void onFinish()
//            {
//                toast.cancel();
//            }
//
//        }.start();
//



        //isAutoUpload
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();
        // if auto is turned off
        if (settings == null){
            return;
        }

        if(settings!=null && !settings.isAutoUpload()){
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

        //taskId


//        Toast.makeText(context, "New Photo:" + imageName + "is Saved as : -" + imagePath, Toast.LENGTH_LONG).show();

        Log.i("CameraEventReceiver", imageName);
        Log.i("CameraEventReceiver",imagePath );
        Log.i("CameraEventReceiver", fileSize + "kb");
        Log.i("CameraEventReceiver", String.valueOf(createTime));

        try{
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
            photo.setTaskId(DeviceStatus.getAuTask(userId).getTaskId());
            photo.save();

            //get current Task id


            Task task = new Select().from(Task.class).where("uploadMode = ?","AU").orderBy("startDate DESC").executeSingle();

            task.setTotalItems(task.getTotalItems() + 1);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.save();

            Log.v("taskid", DeviceStatus.getAuTask(userId).getTaskId());
            Log.v("taskId", getImage().getTaskId());
            Log.v("taskNum", DeviceStatus.getAuTask(userId).getTotalItems() + "");

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



    //get latest image
    public static Image getImage() {
        return new Select()
                .from(Image.class)
                .orderBy("createTime DESC")
                .executeSingle();
    }
}
