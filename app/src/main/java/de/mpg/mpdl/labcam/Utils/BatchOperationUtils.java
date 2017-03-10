package de.mpg.mpdl.labcam.Utils;
import android.app.FragmentManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;

import de.mpg.mpdl.labcam.Gallery.RemoteListDialogFragment;
import de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment.MicrophoneDialogFragment;
import de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment.NoteDialogFragment;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by yingli on 3/3/17.
 */

public class BatchOperationUtils {

    /**
     * addImages function creates a List<Image> for UPLOAD, BATCH_EDIT_NOTE, BATCH_EDIT_VOICE operations
     */

    public static List<Image> addImages(String[] imagePathArray, Long taskId, String userId, String serverName){
        List<Image> imageList = new ArrayList<>();
        List<String> imagePaths = new ArrayList<>();

        for (String filePath: imagePathArray) {
            String imageName = filePath.substring(filePath.lastIndexOf('/') + 1);
            Image image = DBConnector.getImageByPath(filePath, userId, serverName);
            imagePaths.add(filePath);

            if(image!=null){  //  already exist Images
                imageList.add(image);
                continue;
            }

            //imageSize
            File file = new File(filePath);
            String fileSize = String.valueOf(file.length() / 1024);

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }


            //createTime
            String createTime = exif.getAttribute(ExifInterface.TAG_DATETIME);

            //latitude
            String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

            //longitude
            String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            //store image in local database
            Image newImage = new Image();
            newImage.setImageName(imageName);
            newImage.setImagePath(filePath);
            newImage.setLongitude(longitude);
            newImage.setLatitude(latitude);
            newImage.setCreateTime(createTime);
            newImage.setSize(fileSize);
            newImage.setUserId(userId);
            newImage.setServerName(serverName);
            newImage.save();
            imageList.add(newImage);
        }

        Task task = DBConnector.getTaskById(taskId.toString(), userId, serverName);
        if(task!=null) {
            task.setImagePaths(imagePaths);
            task.save();
        }
        return imageList;
    }

    /**** take notes ****/
    public static NoteDialogFragment noteDialogNewInstance(String[] imagePathArray)
    {
        NoteDialogFragment noteDialogFragment = new NoteDialogFragment();

        Bundle args = new Bundle();

        args.putStringArray("imagePathArray", imagePathArray);
        noteDialogFragment.setArguments(args);    // pass imagePathArray to NoteDialogFragment
        return noteDialogFragment;
    }

    /**** record voice ****/
    public static MicrophoneDialogFragment voiceDialogNewInstance(String[] imagePathArray)
    {
        MicrophoneDialogFragment microphoneDialogFragment = new MicrophoneDialogFragment();
        Bundle args = new Bundle();

        args.putStringArray("imagePathArray", imagePathArray);
        microphoneDialogFragment.setArguments(args);
        return microphoneDialogFragment;
    }

}
