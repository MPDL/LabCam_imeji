package de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private String fileFullName = null;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_fragment_microphone, null);
        final Activity activity = this.getActivity();

        Bundle bundle = getArguments();
        final String[] imagePathArray = bundle.getStringArray("imagePathArray");
        for (String s : imagePathArray) {
            Log.d("sss", s);
        }

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
                                // save note
                                Log.d("LY", "save clicked");
                                if(fileFullName==null)
                                {
                                    return;
                                }

                                List<Image> selectedImageList = new ArrayList<Image>(); // selected ImageList
                                for (String imagePath : imagePathArray) {
                                    Image image = DBConnector.getImageByPath(imagePath);
                                    if(image!=null){
                                        selectedImageList.add(image);   // add image to imageList
                                    }

                                    DBConnector.batchEditVoice(selectedImageList, fileFullName);
                                }
                            }
                        }
                )
                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                if(fileFullName == null){
                                    Toast.makeText(activity, "Please long press the button to take record.",Toast.LENGTH_LONG).show();
                                    return;
                                }

                                File file = new File(fileFullName);
                                if(file.exists()){
                                    file.delete();   // delete record
                                }
                            }
                        }
                );

//        // hint text
//        TextView textView = (TextView) view.findViewById(R.id.text_view_voice_hint);
//        textView.setHeight(squareWidth/5);

        checkPermission();

        // record voice
        Button voiceImageView = (Button)view.findViewById(R.id.im_microphone_record);  // record icon
        voiceImageView.getLayoutParams().height = squareWidth/3;
        voiceImageView.getLayoutParams().width = squareWidth/3;

        // record name
        final TextView voiceTextView = (TextView) view.findViewById(R.id.text_view_voice_file_name); // record name

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
                        v.setVisibility(View.INVISIBLE);
                        v.setClickable(false);
                        v.setEnabled(false);
                        voiceTextView.setVisibility(View.VISIBLE);
                        if(fileFullName.contains("/")){
                            String[] filename = fileFullName.split("/");
                            voiceTextView.setText(filename[filename.length-1]);
                        }else voiceTextView.setText(fileFullName);
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

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return (file.getAbsolutePath() + "/" + timeStamp + file_exts[currentFormat]);
    }

    private void startRecording(){
        fileFullName = getFilename();  // pick a file name
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileFullName);
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

    /***********************************   permissions   ****************************************/

    private static final int CHECK_PERMISSION = 1;

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHECK_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CHECK_PERMISSION && grantResults.length >= 2) {
            int firstGrantResult = grantResults[0];
            int secondGrantResult = grantResults[1];
            boolean granted = (firstGrantResult == PackageManager.PERMISSION_GRANTED) && (secondGrantResult == PackageManager.PERMISSION_GRANTED);
            Log.i("permission", "onRequestPermissionsResult granted=" + granted);

            if(granted) {
//                startRecording();
            }else{
                getDialog().dismiss();
                ToastUtil.showShortToast(getActivity(), "please grant RECORD_AUDIO and WRITE_EXTERNAL_STORAGE permissions");
            }
        }
    }

    /**
     * Open image intent
     */
    private void checkPermission() {
        // check permission for android > 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED ) {
                requestPermission();

                return;
            }
        }
        // have permission, then pass
    }
}
