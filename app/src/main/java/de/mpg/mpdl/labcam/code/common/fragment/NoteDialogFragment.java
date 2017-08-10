package de.mpg.mpdl.labcam.code.common.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.rxbus.RxBus;
import de.mpg.mpdl.labcam.code.rxbus.event.NoteRefreshEvent;
import de.mpg.mpdl.labcam.code.utils.BatchOperationUtils;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

/**
 * Created by yingli on 11/24/16.
 */

public class NoteDialogFragment extends DialogFragment {

    private String userId;
    private String serverName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_note, null);
        Activity activity = this.getActivity();
        userId =  PreferenceUtil.getString(activity, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverName = PreferenceUtil.getString(activity, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

        Bundle bundle = getArguments();
        final String[] imagePathArray = bundle.getStringArray("imagePathArray");

        // display dialog window size
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int squareWidth;

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

                                List<Image> selectedImageList = BatchOperationUtils.addImages(imagePathArray, null, userId, serverName);  // init images in db

                                DBConnector.batchEditNote(selectedImageList, noteContentStr, userId, serverName);

                                NoteRefreshEvent noteRefreshEvent = new NoteRefreshEvent(imagePathArray[imagePathArray.length-1]);
                                RxBus.getDefault().post(noteRefreshEvent);
                            }
                        }
                )
                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setLayout(squareWidth, squareWidth);
        return alertDialog;
    }
}
