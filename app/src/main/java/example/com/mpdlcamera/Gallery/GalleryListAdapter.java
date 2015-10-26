package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.R;

/**
 * Created by kiran on 22.10.15.
 */

public class GalleryListAdapter extends BaseAdapter {

    private Activity activity;
    private List<Gallery> galleryList;
    private final String LOG_TAG = GalleryListAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private ArrayList<Gallery> galleries = new ArrayList<Gallery>();
    private String localPath;
    Boolean matchGallery = false;


    private void setLocalPath(String path) {
        this.localPath = path;
    }

    public GalleryListAdapter(Activity activity) {
        this.activity = activity;
    }

    public GalleryListAdapter(Activity activity, List<Gallery> galleryList) {
        this.activity = activity;
        this.galleryList = galleryList;
    }

    @Override
    public int getCount() {
        return galleryList.size();
    }

    @Override
    public Object getItem(int location) {
        return galleryList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);


        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.gallery_list_cell, null);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_gallery_cell_thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.list_item_gallery_title);


        if (size.x > size.y) {
            imageView.getLayoutParams().height = 1 * size.y /3;
        } else {
            imageView.getLayoutParams().height = 1 * size.y /3;
        }

       // for(int i=0; i<galleryList.size();i++) {
            // getting item data for the row
            Gallery gallery = galleryList.get(position);
            //Gallery gallery = galleryList.get(i);
            Log.v(LOG_TAG, gallery.getGalleryName());
            String imPath = getLatestImage(gallery);
            //ListGalleries(gallery);

            if (gallery.getItems() != null) {
                if (gallery.getItems().size() > 0) {
                    Gallery m = gallery.getItems().get(0);

                }
            }
            title.setText(gallery.getGalleryName());

            String iPath = this.localPath;


           // File imgFile = new File(iPath);
            File imgFile1 = new File(imPath);
            if(imgFile1.exists()) {
                Picasso.with(activity)
                        .load(imgFile1)
                        .into(imageView);

            }

            //title
            title.setText(gallery.getGalleryName());

            // user
           // description.setText(collection.getDescription());

            // date
            //date.setText(String.valueOf(m.getCreatedDate()).split("\\+")[0]);
   //     }
        return convertView;
    }


    public String getLatestImage(Gallery gallery) {

        matchGallery = false;

        String imPath = null;

        while(true) {

            String columns[] = new String[]{ MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,};

            Uri image = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Cursor cursor = activity.getContentResolver().query(image,columns,null,null,null);


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

                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    Uri uri1 = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(cursor.getColumnIndex(MediaStore.Images.Media._ID)));


                 /*   File file = new File(uri1.getPath());
                    String path = file.getAbsolutePath();

                    File dir = new File(path);
                    File[] files = dir.listFiles(); */
                    // int numberOfImages = files.length;


                }

            }
            cursor.close();
            }


        }

    public void ListGalleries(Gallery gallery) {

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        ArrayList<String> ids = new ArrayList<String>();
        //galleries.clear();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Gallery album = new Gallery();

                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                album.setBucketId(cursor.getString(columnIndex));

                if (!ids.contains(album.getBucketId())) {
                    columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                    album.setGalleryName(cursor.getString(columnIndex));

                    columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    album.setCoverId(cursor.getLong(columnIndex));
                    Uri uri1 = Uri.withAppendedPath( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(columnIndex));

                  /*  try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } */

                    galleries.add(album);
                    ids.add(album.getBucketId());
                } else {
                     //Gallery gallery2 = galleries.get(ids.indexOf(album.getBucketId()));

                }
            }
            cursor.close();


        }

    }


}
