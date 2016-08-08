package de.mpg.mpdl.labcam.Upload;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import de.mpg.mpdl.labcam.Folder.MainActivity;
import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.MetaData;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;

/**
 * Created by kiran on 29.09.15.
 */

public class LatestImage {

private Context context;

    MainActivity just;

    public int maxId;

    public LatestImage() {

    }
    public LatestImage(Context context) {
        this.context = context;
    }

    private String collectionID = DeviceStatus.collectionID;

    private int latestId;


    /*
        returns the ImageID of the latest image(which is added to the file system)
     */
    public int getId() {


        String[] columns = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION};
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, columns, null, null, MediaStore.Images.Media._ID + " DESC");

        // no image in file system
        if (!cursor.moveToFirst()) {
            return -1;
        }

        //ColumnIndex for image ID
        latestId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        setMaxIdFromDatabase();
        int maxId = getMaxId();

        String orientation = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));

        if (orientation == null) {
            setMaxId(latestId);
            return -1;
        }

        setMaxId(latestId);

        return latestId;
    }

    /*
        get the latest image
     */
        public DataItem getLatestItem() {

        DataItem item = null;
        MetaData meta = new MetaData();

        String columns[] = new String[]{ MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.MINI_THUMB_MAGIC };

        SharedPreferences preferences = context.getSharedPreferences("folder", Context.MODE_PRIVATE);

        while (true) {

            Uri image = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, latestId);

            //
            Cursor cursor = context.getContentResolver().query(image,columns,null,null,null);

            //boolean moveToFirst
            if(cursor.moveToFirst()) {


                    String folder = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                    if(preferences.contains(folder)) {

                        String state = preferences.getString(folder,"");
                        if(state.equalsIgnoreCase("On")) {

                            item = new DataItem();
                            item.setFilename(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
                            item.setLocalPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                          //  meta.setTags(null);
                          //  meta.setAddress("blabla");
                          //  meta.setTitle(item.getFilename());
                          //  meta.setCreator(user.getCompleteName());

                            item.setCollectionId(collectionID);

                          //  item.setMetadata(meta);

                          //  item.setCreatedBy(user);

                          //  meta.save();
                            item.save();
                            break;

                        }
                        else return item;


                    }
                    else
                        return item;

            }


        }

            return item;
    }


    /*
        returns the maximum ID (recent image) of the file system
     */
    private void setMaxIdFromDatabase()
    {

        //
        String columns[] = new String[]{ MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.MINI_THUMB_MAGIC };
        Cursor cursor    = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.Images.Media._ID + " DESC");

        //get latest image ID
        maxId            = cursor.moveToFirst() ? cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)) : -1;
    }

    public void setMaxId(int maxId)
    {
        this.maxId = maxId;
    }

    /**
     * Get highest image id
     *
     * @return Highest id
     */
    public int getMaxId()
    {
        return maxId;
    }

}
