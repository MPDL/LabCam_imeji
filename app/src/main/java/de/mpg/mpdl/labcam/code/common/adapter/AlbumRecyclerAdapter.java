package de.mpg.mpdl.labcam.code.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.activity.DetailActivity;
import de.mpg.mpdl.labcam.code.activity.LocalImageActivity;
import me.grantland.widget.AutofitHelper;

/**
 * Created by kiran on 22.10.15.
 */

public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.AlbumRecyclerViewHolder> {

    private static final String LOG_TAG = AlbumRecyclerAdapter.class.getSimpleName();
    private Activity activity;

    // all albums
    private ArrayList<List<String[]>> galleryList;
    static ArrayList<String> itemPathList = new ArrayList<>();
    Point size;

    // album positionSet
    private Set<Integer> albumPositionSet = new HashSet<>();

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AlbumRecyclerAdapter(Activity activity) {
        this.activity = activity;
    }

    public AlbumRecyclerAdapter(Activity activity, ArrayList<List<String[]>> galleryList) {
        this.activity = activity;
        this.galleryList = galleryList;
    }

    @Override
    public AlbumRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.album_recycler_cell, parent, false);


        return new AlbumRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlbumRecyclerViewHolder holder, final int position) {
        //get display size
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        if (size.x > size.y) {
            holder.imageView1.getLayoutParams().height = size.x / 3;
        } else {
            holder.itemView.getLayoutParams().height = size.y / 3;
        }

        final List<String[]> gallery = galleryList.get(position);

        // display first 6 photos in an album
        // if album size less than 6, display differently
        int sizeConstrain = 6;
        if (gallery.size() < 7) {
            sizeConstrain = gallery.size();
        }

        // 3 images a row
        int pixels;
        final float scale = activity.getResources().getDisplayMetrics().density;
        if (sizeConstrain < 4) {
            pixels = (int) (size.x / 3 + 80 * scale + 0.5f);
        } else {
            pixels = (int) (size.x * 2 / 3 + 80 * scale + 0.5f);
        }

        RelativeLayout.LayoutParams reParam = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, pixels);
        reParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        holder.cell.setLayoutParams(reParam);

        ViewGroup.LayoutParams liParam1 = holder.layoutLayer1.getLayoutParams();
        liParam1.height = size.x / 3;
        holder.layoutLayer1.setLayoutParams(liParam1);
        ViewGroup.LayoutParams liParam2 = holder.layoutLayer2.getLayoutParams();
        liParam2.height = size.x / 3;
        holder.layoutLayer2.setLayoutParams(liParam2);

        // get current album

        final List<ImageView> imageViewList = new ArrayList<>();
        imageViewList.add(holder.imageView1);
        imageViewList.add(holder.imageView2);
        imageViewList.add(holder.imageView3);
        imageViewList.add(holder.imageView4);
        imageViewList.add(holder.imageView5);
        imageViewList.add(holder.imageView6);

        List<TextView> textViewList = new ArrayList<>();
        textViewList.add(holder.textView1);
        textViewList.add(holder.textView2);
        textViewList.add(holder.textView3);
        textViewList.add(holder.textView4);
        textViewList.add(holder.textView5);
        textViewList.add(holder.textView6);

        // first set everything invisible
        for (int i = 0; i <6 ; i++ ){
            textViewList.get(i).setVisibility(View.INVISIBLE);
            imageViewList.get(i).setVisibility(View.INVISIBLE);
        }

        // to the album view
        for (int i = 0; i < sizeConstrain; i++) {
            imageViewList.get(i).setVisibility(View.VISIBLE);
            if (i == 5 && gallery.size() != 6) { // if <=6 photo, no shadow on 6th photo; if >6, remain photos number begins with 2
                AutofitHelper.create(textViewList.get(i));
                textViewList.get(i).setVisibility(View.VISIBLE);
                textViewList.get(i).setText(String.valueOf(gallery.size()-5) + " >");
                textViewList.get(i).setBackgroundResource(R.color.black_shadow);
                imageViewList.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(getAlbumPath(gallery.get(0)[1])==null)
                            return;
                        Intent galleryImagesIntent = new Intent(activity, LocalImageActivity.class);
                        galleryImagesIntent.putExtra("galleryTitle", getAlbumPath(gallery.get(0)[1]));
                        activity.startActivity(galleryImagesIntent);
                    }
                });
            } else {
                textViewList.get(i).setText("");
                textViewList.get(i).setVisibility(View.INVISIBLE);
                imageViewList.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // go to photo detail
                        boolean isLocalImage = true;
                        int imagePosition = 0;
                        Intent showDetailIntent = new Intent(activity, DetailActivity.class);
                        itemPathList.clear();
                        for (String[] imageStr : gallery) {
                            itemPathList.add(imageStr[1]);
                        }

                        switch (view.getId()){
                            case R.id.album_pic_1:
                                imagePosition = 0;
                                break;
                            case R.id.album_pic_2:
                                imagePosition = 1;
                                break;
                            case R.id.album_pic_3:
                                imagePosition = 2;
                                break;
                            case R.id.album_pic_4:
                                imagePosition = 3;
                                break;
                            case R.id.album_pic_5:
                                imagePosition = 4;
                                break;
                            default:
                                break;
                        }

                        showDetailIntent.putStringArrayListExtra("itemPathList", itemPathList);
                        Log.e(LOG_TAG, itemPathList.size()+"");
                        showDetailIntent.putExtra("positionInList",imagePosition);
                        showDetailIntent.putExtra("isLocalImage", isLocalImage);
                        activity.startActivity(showDetailIntent);
                    }
                });

            }
        }

        for (int i = 0; i < sizeConstrain; i++) {
            String galleryPath = gallery.get(i)[1];

            File imageFile = new File(galleryPath);

            Uri uri = Uri.fromFile(imageFile);

            if (imageFile.exists()) {
                Picasso.with(activity)
                        .load(uri)
                        .resize(size.x / 3, size.x / 3)
                        .centerCrop()
                        .into(imageViewList.get(i));
            }
        }


        if (!gallery.isEmpty()) {
            holder.title.setText(gallery.get(0)[0]);
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAlbumPath(gallery.get(0)[1])==null)
                        return;
                    Intent galleryImagesIntent = new Intent(activity, LocalImageActivity.class);
                    galleryImagesIntent.putExtra("galleryTitle", getAlbumPath(gallery.get(0)[1]));
                    activity.startActivity(galleryImagesIntent);
                }
            });
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v, position);
                    return false;
                }
            });
        }

        // checkMark
        if (albumPositionSet.contains(position)) {
            holder.checkMark.setVisibility(View.VISIBLE);
        } else {
            holder.checkMark.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public static class AlbumRecyclerViewHolder extends RecyclerView.ViewHolder {

        protected ImageView imageView1;
        protected ImageView imageView2;
        protected ImageView imageView3;
        protected ImageView imageView4;
        protected ImageView imageView5;
        protected ImageView imageView6;

        protected TextView textView1;
        protected TextView textView2;
        protected TextView textView3;
        protected TextView textView4;
        protected TextView textView5;
        protected TextView textView6;

        protected RelativeLayout cell;
        protected LinearLayout layoutLayer1;
        protected LinearLayout layoutLayer2;

        protected ImageView checkMark;
        protected TextView title;


        public AlbumRecyclerViewHolder(View itemView) {
            super(itemView);
            cell = (RelativeLayout) itemView.findViewById(R.id.relOne);
            layoutLayer1 = (LinearLayout) itemView.findViewById(R.id.layout_first_layer_text);
            layoutLayer2 = (LinearLayout) itemView.findViewById(R.id.layout_second_layer_text);

            imageView1 = (ImageView) itemView.findViewById(R.id.album_pic_1);
            imageView2 = (ImageView) itemView.findViewById(R.id.album_pic_2);
            imageView3 = (ImageView) itemView.findViewById(R.id.album_pic_3);
            imageView4 = (ImageView) itemView.findViewById(R.id.album_pic_4);
            imageView5 = (ImageView) itemView.findViewById(R.id.album_pic_5);
            imageView6 = (ImageView) itemView.findViewById(R.id.album_pic_6);

            textView1 = (TextView) itemView.findViewById(R.id.album_tv_1);
            textView2 = (TextView) itemView.findViewById(R.id.album_tv_2);
            textView3 = (TextView) itemView.findViewById(R.id.album_tv_3);
            textView4 = (TextView) itemView.findViewById(R.id.album_tv_4);
            textView5 = (TextView) itemView.findViewById(R.id.album_tv_5);
            textView6 = (TextView) itemView.findViewById(R.id.album_tv_6);

            checkMark = (ImageView) itemView.findViewById(R.id.album_check_mark);
            title = (TextView) itemView.findViewById(R.id.tv_album_title);
        }
    }

    public void setPositionSet(Set<Integer> positionSet) {
        this.albumPositionSet = positionSet;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private String getAlbumPath(String str) {
        if(null!=str&&str.length()>0)
        {
            int endIndex = str.lastIndexOf('/');
            if (endIndex != -1) {
                return str.substring(0, endIndex);
            }else return null;
        }else return null;

    }
}
