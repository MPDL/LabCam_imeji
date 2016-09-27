package de.mpg.mpdl.labcam.ImejiFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mpg.mpdl.labcam.ItemDetails.ItemsActivity;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.CustomImageDownaloder;

/**
 * Created by allen on 06/04/15.
 */
public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.FolderListViewHolder>{
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> folderItems;
    private final String LOG_TAG = FolderListAdapter.class.getSimpleName();

    private SharedPreferences mPrefs;
    private String apiKey;

    private Map<String, String> headers = new HashMap<String,String>() {};

    public FolderListAdapter(Activity activity, List<ImejiFolder> folderItems) {
        this.activity = activity;
        this.folderItems = folderItems;

        if(activity==null){
            return;
        }

        mPrefs = activity.getSharedPreferences("myPref", 0);
        apiKey = mPrefs.getString("apiKey","");

        this.headers.put("Authorization","Bearer "+apiKey);

    }

    @Override
    public FolderListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.folder_list_cell, parent, false);


        return new FolderListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FolderListViewHolder holder, final int position) {

        // displaysize
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        //Log.v(size.x  + " width", size.y  + " height");

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


//
//        if (size.x > size.y) {
//            holder.imageView.getLayoutParams().height = 2 * size.y /3;
//        } else {
//            holder.imageView.getLayoutParams().height = 2 * size.y /3;
//        }
        ViewGroup.LayoutParams layoutParams =  holder.cardView.getLayoutParams();
        layoutParams.height =size.y*1/3;
        holder.cardView.setLayoutParams(layoutParams);

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
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(configuration);


            //显示图片的配置
            DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(R.drawable.progress_image)
                    .showImageOnFail(R.drawable.error_alert)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .extraForDownloader(headers)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();


            imageLoader.displayImage(collection.getCoverItemUrl(), holder.imageView, options);

//            Picasso.with(activity)
//                            .load(collection.getCoverItemUrl())
//                            .into(imageView);
//                }
//            }

            //number
//            holder.number.setText("");

            //title
            holder.title.setText(collection.getTitle());
//            String title = "";
//            if(collection.getTitle().length()>30){
//                title = collection.getTitle().substring(0,30)+"...";
//            }else {
//                title = collection.getTitle();
//            }
//            holder.title.setText(title);

            // date
            String str = collection.getCreatedDate();
            if (null != str && str.length() > 0 )
            {
                int endIndex = str.lastIndexOf("T");
                if (endIndex != -1)
                {
                    String newstr = str.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
                    holder.date.setText(newstr);
                }
            }

            // description
            holder.description.setText(collection.getDescription());
//            String description = "";
//            if(collection.getDescription().length()>100){
//                description = collection.getDescription().substring(0,100)+"...";
//            }else {
//                description = collection.getDescription();
//            }
//            holder.description.setText(description);

            // author full name

            holder.author.setText("By: " + collection.getCreatedBy().getFullname());
            // go to the cell
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImejiFolder folder = (ImejiFolder) folderItems.get(position);

                    Intent showItemsIntent = new Intent(activity, ItemsActivity.class);
                    showItemsIntent.putExtra(Intent.EXTRA_TEXT, folder.id);
                    showItemsIntent.putExtra("folderTitle", folder.getTitle());
                    activity.startActivity(showItemsIntent);
                }
            });

            // date
            //date.setText(String.valueOf(m.getCreatedDate()).split("\\+")[0]);
        }

    }

    @Override
    public int getItemCount() {
        return folderItems.size();
    }

    public static class FolderListViewHolder extends RecyclerView.ViewHolder{

        protected ImageView imageView;
//        protected TextView number;
        protected TextView title;
        protected TextView date;
        protected TextView author;
        protected TextView description;
        protected CardView cardView;


        public FolderListViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.list_item_cell_thumbnail);
//            number = (TextView) itemView.findViewById(R.id.list_item_num);
            title = (TextView) itemView.findViewById(R.id.list_item_cell_title);
            date = (TextView) itemView.findViewById(R.id.list_item_date);
            author = (TextView) itemView.findViewById(R.id.list_item_author);
            description = (TextView) itemView.findViewById(R.id.list_item_description);
            cardView = (CardView) itemView.findViewById(R.id.folderlist_cardview);
        }
    }
}
