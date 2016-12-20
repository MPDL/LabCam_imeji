package de.mpg.mpdl.labcam.TaskManager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;

/**
 * Created by Yunqing on 19.12.16.
 */


public class RecentNoteAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    private Activity activity;
    private List<Note> noteList;


    public RecentNoteAdapter(Activity activity, List<Note> noteList) {
        this.activity = activity;
        this.noteList = noteList;
    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int i) {
        return noteList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.recent_text_list_cell, null);

        TextView taskName = (TextView) view.findViewById(R.id.text_list_cell_text);

        String taskInfo = noteList.get(i).getNoteContent();

        taskName.setText(taskInfo);

        TextView taskTime = (TextView) view.findViewById(R.id.text_list_cell_time);

        String endDate = noteList.get(i).getCreateTime();

        taskTime.setText(endDate);

        // DELETE NOTE
        ImageView deleteNoteButton = (ImageView) view.findViewById(R.id.btn_delete);
        deleteNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote(noteList.get(i));
            }
        });

        return view;

    }

    private void deleteNote(Note note){
        for (Image image : DBConnector.getImageByNoteId(note.getNoteId())) {
            image.setNoteId(null);
        }
        note.delete();
        noteList.remove(note);
        notifyDataSetChanged();
    }
}
