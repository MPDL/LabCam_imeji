package de.mpg.mpdl.labcam.ItemDetails;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.ToastUtil;
import de.mpg.mpdl.labcam.Utils.camPicassoLoader;

/**
 * Created by yingli on 3/16/16.
 */
public class ViewPagerAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;
    Point size;
    List<String> imagePathList;
    boolean isLocalImage;
    private OnItemClickListener onItemClickListener;

    public Set<Integer> positionSet = new HashSet<>();

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setPositionSet(Set<Integer> positionSet){
        this.positionSet = positionSet;
        Log.e("setPositionSet",positionSet.toString());
        notifyDataSetChanged();
    }

    public ViewPagerAdapter(Context context, Point size,boolean isLocalImage, List<String> imagePathList) {
        this.context = context;
        this.size = size;
        this.isLocalImage = isLocalImage;
        this.imagePathList = imagePathList;
    }

    @Override
    public int getCount() {
        return imagePathList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ImageView imageView;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);

        imageView = (ImageView) itemView.findViewById(R.id.detail_image);

        // voice panel

        //check mark
        ImageView checkMark = (ImageView) itemView.findViewById(R.id.viewpager_check_mark);
        if(positionSet.contains(position)){
            checkMark.setVisibility(View.VISIBLE);
        }else {
            checkMark.setVisibility(View.GONE);
        }

        if(isLocalImage){
            Uri uri = Uri.fromFile(new File(imagePathList.get(position)));
            Picasso.with(context)
                    .load(uri)
                    .resize(size.x, size.y)
                    .centerInside()
                            //.centerCrop()
                            //.placeholder(R.drawable.progress_animation)
                    .into(imageView);
        }else {
            Picasso myPicasso = new Picasso.Builder(context).downloader(new camPicassoLoader(context)).build();
            myPicasso.load(imagePathList.get(position))
                    .resize(size.x, size.y)
                    .centerInside()
                    .error(R.drawable.error_alert).into(imageView);
        }

        if (onItemClickListener!=null){

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v, position);
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, position);
                }
            });
        }

        initImageInfoLayout(itemView, position);  // init notes and voice

        ((ViewPager) container).addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

    // view pager need to remove all views and reload them all
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }


    // ui VoicePanel

    private void initImageInfoLayout(View itemView, int position){
        // get Image object
        Image image = DBConnector.getImageByPath(imagePathList.get(position));
        if(image != null){
            RelativeLayout notePanelLayout = (RelativeLayout) itemView.findViewById(R.id.layout_note_panel);
            RelativeLayout voicePanelLayout = (RelativeLayout) itemView.findViewById(R.id.layout_voice_panel);

            if(image.getNoteId()!=null){      // show notes
                initNotePanel(itemView, image, notePanelLayout);
                notePanelLayout.setVisibility(View.VISIBLE);
            }else notePanelLayout.setVisibility(View.GONE);

            if(image.getVoiceId()!=null){     // show voice
                initVoicePanel(itemView, image, voicePanelLayout);  // init player
                voicePanelLayout.setVisibility(View.VISIBLE);
            }else voicePanelLayout.setVisibility(View.GONE);
        }
    }

    private void initNotePanel(View itemView, final Image image, final View notePanelLayout){
        final RelativeLayout editNoteButtonLayout = (RelativeLayout) itemView.findViewById(R.id.layout_edit_note_button);
        final TextView noteTextView = (TextView) itemView.findViewById(R.id.tv_notes_detail);
        final EditText noteEditText = (EditText) itemView.findViewById(R.id.et_notes_detail);
        final TextView cancelTextView = (TextView) itemView.findViewById(R.id.tv_cancel_edit_note);
        final TextView saveTextView = (TextView) itemView.findViewById(R.id.tv_save_edit_note);

        noteTextView.setVisibility(View.VISIBLE);
        if(image.getNoteId()==null){
            noteTextView.setVisibility(View.GONE);
        }else noteTextView.setText(DBConnector.getNoteById(image.getNoteId()).getNoteContent());

        cancelTextView.setOnClickListener(new View.OnClickListener() {  //
            @Override
            public void onClick(View v) {
                // do nothing
            }
        });

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> selectedImageList = new ArrayList<Image>(); // selected ImageList
                selectedImageList.add(image);

                DBConnector.batchEditNote(selectedImageList, String.valueOf(noteEditText.getText()));

                editNoteButtonLayout.setVisibility(View.GONE);
                noteEditText.setVisibility(View.GONE);

                noteTextView.setText(noteEditText.getText());
                noteTextView.setVisibility(View.VISIBLE);
            }
        });

        noteTextView.setOnClickListener(new View.OnClickListener() {  // click to edit note
            @Override
            public void onClick(View v) {
                noteTextView.setVisibility(View.GONE);

                noteEditText.setText(DBConnector.getNoteById(image.getNoteId()).getNoteContent());
                noteEditText.setVisibility(View.VISIBLE);

                editNoteButtonLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    /** voice panel **/
    private void initVoicePanel(View itemView, final Image image, final View voicePanelLayout){

        final MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(DBConnector.getVoiceById(image.getVoiceId()).getVoicePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final ImageButton pauseButton = (ImageButton) itemView.findViewById(R.id.btn_pause_voice);
        final ImageButton rewindButton = (ImageButton) itemView.findViewById(R.id.btn_rewind_voice);
        final ImageButton deleteButton = (ImageButton) itemView.findViewById(R.id.btn_delete_voice);
        final ImageButton resetButton = (ImageButton) itemView.findViewById(R.id.btn_reset_voice);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showLongToast(context, "Pausing sound");
                mediaPlayer.pause();

                pauseButton.setEnabled(false);
                rewindButton.setEnabled(true);
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showLongToast(context, "Playing sound");
                mediaPlayer.start();

                pauseButton.setEnabled(true);
                rewindButton.setEnabled(false);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showLongToast(context, "Reseting sound");
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(DBConnector.getVoiceById(image.getVoiceId()).getVoicePath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                pauseButton.setEnabled(true);
                rewindButton.setEnabled(true);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showLongToast(context, "Deleting sound");
                voicePanelLayout.setVisibility(View.GONE);
                image.setVoiceId(null);  // set image voice to null, not really delete voice here
                image.save();          // todo: think about when and where to delete Voice and voice file (not here)
            }
        });
    }


}
