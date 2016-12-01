package de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Point;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.mpg.mpdl.labcam.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by yingli on 11/24/16.
 */

public class MicrophoneDialogFragment extends DialogFragment{

    private final String LOG_TAG = MicrophoneDialogFragment.class.getSimpleName().toString();
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_microphone, null);
        final Activity activity = this.getActivity();

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



        // builder
        AlertDialog.Builder builder=  new  AlertDialog.Builder(getActivity())
                .setTitle("Create a new voice note：")
                .setPositiveButton("SAVE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(activity, getFilename(),Toast.LENGTH_LONG).show();

                            }
                        }
                )
                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                );

//        // hint text
//        TextView textView = (TextView) view.findViewById(R.id.text_view_voice_hint);
//        textView.setHeight(squareWidth/5);

        // record voice
        Button voiceImageView = (Button)view.findViewById(R.id.im_microphone_record);  // record icon
        voiceImageView.getLayoutParams().height = squareWidth/3;
        voiceImageView.getLayoutParams().width = squareWidth/3;

        voiceImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.d(LOG_TAG, "Start Recording");
                        startRecording();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(LOG_TAG, "stop Recording");
                        stopRecording();

                        break;
                }
                return false;
            }
        });


        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setLayout(squareWidth, squareWidth);
        return alertDialog;
    }



    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.i(LOG_TAG,"Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.i(LOG_TAG, "Warning: " + what + ", " + extra);
        }
    };

    private void stopRecording(){
        if(null != recorder){
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }
}
