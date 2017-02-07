package de.mpg.mpdl.labcam.TaskManager;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.ToastUtil;

/**
 * Created by Yunqing on 20.12.16.
 */

public class RecentVoiceAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    private Activity activity;
    private List<Voice> voiceList;


    public RecentVoiceAdapter(Activity activity, List<Voice> voiceList) {
        this.activity = activity;
        this.voiceList = voiceList;
    }

    @Override
    public int getCount() {
        return voiceList.size();
    }

    @Override
    public Object getItem(int i) {
        return voiceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.recent_voice_list_cell, null);


        initVoicePanel(view, voiceList.get(i));

        return view;

    }


    /** voice panel **/
    private void initVoicePanel(View itemView, final Voice voice){

        final MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(voice.getVoicePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        TextView createTimeTextView = (TextView) itemView.findViewById(R.id.voice_list_cell_text);
        createTimeTextView.setText(voice.getCreateTime());
        final ImageButton pauseButton = (ImageButton) itemView.findViewById(R.id.btn_pause_voice_cell);
        final ImageButton rewindButton = (ImageButton) itemView.findViewById(R.id.btn_rewind_voice_cell);
        final ImageButton deleteButton = (ImageButton) itemView.findViewById(R.id.btn_delete_voice_cell);
        final ImageButton resetButton = (ImageButton) itemView.findViewById(R.id.btn_reset_voice_cell);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showLongToast(activity, "Pausing sound");
                mediaPlayer.pause();

                pauseButton.setEnabled(false);
                rewindButton.setEnabled(true);
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showLongToast(activity, "Playing sound");
                mediaPlayer.start();

                pauseButton.setEnabled(true);
                rewindButton.setEnabled(false);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showLongToast(activity, "Reseting sound");
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(voice.getVoicePath());
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
                ToastUtil.showLongToast(activity, "Deleting sound");
                deleteVoice(voice);
            }
        });
    }
    private void deleteVoice(Voice voice){
        for (Image image : DBConnector.getImageByVoice(voice.getVoiceId())) {
            image.setVoiceId(null);
            image.save();
        }
        voice.delete();
        voiceList.remove(voice);
        notifyDataSetChanged();
    }
}
