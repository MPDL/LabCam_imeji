package example.com.mpdlcamera.Gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

import example.com.mpdlcamera.Model.Gallery;

/**
 * Created by kiran on 26.10.15.
 */
public class Thumbnail {

    Boolean matchGallery = false;

    private Context context;

    public Thumbnail(Context context) {
        this.context = context;
    }


    public String getLatestImage(Gallery gallery) {

        matchGallery = false;

        String imPath = null;

        while(true) {

            String columns[] = new String[]{ MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,};

            Uri image = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Cursor cursor = context.getContentResolver().query(image,columns,null,null,null);


            while (cursor.moveToNext()) {

                String folder = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                if(matchGallery.equals(true) && !folder.equalsIgnoreCase(gallery.getGalleryName())) {
                    return imPath;
                }

                if (folder.equalsIgnoreCase(gallery.getGalleryName())) {


                    matchGallery = true;

                    imPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    //  setLocalPath(localPath);



                    //  String magic = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MINI_THUMB_MAGIC));

                    //String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    Integer id = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    Uri uri1 = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));


                    gallery.incrementCount();


                    File file = new File(imPath);
                    String dir = file.getParent();
                    //String path = file.getAbsolutePath();

                    gallery.setGalleryPath(dir);


                 /*   File dir = new File(path);
                    File[] files = dir.listFiles(); */
                    // int numberOfImages = files.length;


                }

            }
            cursor.close();
        }


    }
}
