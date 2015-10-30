package example.com.mpdlcamera.Gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.Gallery;

/**
 * Created by kiran on 26.10.15.
 */
public class Thumbnail {

    Boolean matchGallery = false;

    private Context context;

    private ArrayList<String> galleries = new ArrayList<>();

    public Thumbnail(Context context) {
        this.context = context;
    }


    public String getLatestImage(Gallery gallery, Boolean flag) {

        matchGallery = false;

        String imPath = null;



        while(true) {

            galleries.add(gallery.getGalleryName());

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


                    Integer id = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    Uri uri1 = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));


                    if(flag) {
                        gallery.incrementCount();
                    }


                    File file = new File(imPath);
                    String dir = file.getParent();


                    gallery.setGalleryPath(dir);


                }

            }
            cursor.close();
        }


    }
}
