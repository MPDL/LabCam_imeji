package example.com.mpdlcamera.Folder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.CustomImageDownaloder;
import example.com.mpdlcamera.Utils.camPicassoLoader;

/**
 * Created by allen on 06/04/15.
 */
public class FolderListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> folderItems;
    private final String LOG_TAG = FolderListAdapter.class.getSimpleName();

    public FolderListAdapter(Activity activity, List<ImejiFolder> folderItems) {
        this.activity = activity;
        this.folderItems = folderItems;
    }

    @Override
    public int getCount() {
        return folderItems.size();
    }

    @Override
    public Object getItem(int location) {
        return folderItems.get(location);
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

        //Log.v(size.x  + " width", size.y  + " height");

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.folder_list_cell, null);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_cell_thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.list_item_cell_title);
        TextView description = (TextView) convertView.findViewById(R.id.list_item_user);
        //TextView date = (TextView) convertView.findViewById(R.id.list_item_cell_date);

        if (size.x > size.y) {
            imageView.getLayoutParams().height = 2 * size.y /3;
        } else {
            imageView.getLayoutParams().height = 2 * size.y /3;
        }

        if(folderItems.size()>0) {
            // getting item data for the row
            ImejiFolder collection = folderItems.get(position);
            Log.v(LOG_TAG, collection.getTitle());
//
            //创建默认的ImageLoader配置参数
            ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(activity)
                    .imageDownloader(new CustomImageDownaloder(activity))
                    .writeDebugLogs() //打印log信息
                    .build();


            //Initialize ImageLoader with configuration.
//        ImageSize targetSize = new ImageSize(size.x / 2, size.y/3);
            ImageLoader imageLoader = ImageLoader.getInstance();
            ImageLoader.getInstance().init(configuration);


            //显示图片的配置
            DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.progress_image)
                    .showImageOnFail(R.drawable.error_alert)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();



            imageLoader.displayImage(collection.getCoverItemUrl(), imageView, options);

//            Picasso.with(activity)
//                            .load(collection.getCoverItemUrl())
//                            .into(imageView);
//                }
//            }
            title.setText(collection.getTitle());

            //title
            title.setText(collection.getTitle());

            // user
            description.setText(collection.getDescription());

            // date
            //date.setText(String.valueOf(m.getCreatedDate()).split("\\+")[0]);
        }
        return convertView;
    }

}
