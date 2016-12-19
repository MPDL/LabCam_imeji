package de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by yingli on 11/24/16.
 */

public class NoteDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_note, null);
        Activity activity = this.getActivity();

        Bundle bundle = getArguments();
        final String[] imagePathArray = bundle.getStringArray("imagePathArray");
        for (String s : imagePathArray) {
            Log.d("sss", s);
        }

        // display dialog window size
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int squareWidth = 0;

        if(width>height)
            squareWidth = height/2;
        else
            squareWidth = width/2;


        // edit notes
        final EditText editText= (EditText)view.findViewById(R.id.note_edit_text);
        editText.setHeight(3*squareWidth/4);    // push pos,neg button to the bottom


        // builder
        AlertDialog.Builder builder=  new  AlertDialog.Builder(getActivity())
                .setTitle("Create a new text note：")
                .setPositiveButton("SAVE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // save note
                                String noteContentStr = editText.getText().toString();

                                Note newNote = new Note();
                                newNote.setNoteId(UUID.randomUUID().toString());
                                newNote.setNoteContent(noteContentStr);
                                newNote.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
                                newNote.save();

                                // update image, set note
                                boolean deleteNote = true;
                                for (String imagePath : imagePathArray) {
                                    Image image = DBConnector.getImageByPath(imagePath);
                                    if(image!=null){
                                        if(image.getNoteId()!=null){
                                            Note oldNote = DBConnector.getNoteById(image.getNoteId());
                                            oldNote.setNoteContent(noteContentStr);
                                            oldNote.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
                                            oldNote.save();
                                        }else {
                                            image.setNoteId(newNote.getNoteId()); // set noteId as newNote
                                            image.save();
                                            deleteNote = false;
                                        }
                                    }
                                    // delete new note
                                    if(deleteNote){
                                        newNote.delete();
                                    }
                                }
                                // log notes size
                               Log.e("NOTES_SIZE", "size" + new Select().from(Note.class).execute().size());
                            }
                        }
                )
                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // do nothing
                            }
                        }
                );

        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setLayout(squareWidth, squareWidth);
        return alertDialog;
    }
}
