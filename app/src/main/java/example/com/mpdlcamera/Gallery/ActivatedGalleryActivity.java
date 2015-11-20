package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Otto.OttoSingleton;
import example.com.mpdlcamera.Otto.UploadEvent;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.SQLite.FileId;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.Utils.DeviceStatus;
import example.com.mpdlcamera.Utils.ImageFileFilter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

/**
 * Created by kiran on 29.10.15.
 */
public class ActivatedGalleryActivity extends AppCompatActivity implements UploadResultReceiver.Receiver {


    private final String LOG_TAG = ActivatedGalleryActivity.class.getSimpleName();
    private List<String> selectedDataPathList = new ArrayList<String>();
    private SharedPreferences mPrefs;
    private TypedFile typedFile;
    private CircularProgressButton circularButton;
    private String dataCollectionId;


    private View rootView;
    private Toolbar toolbar;
    private Activity activity = this;
    private String galleryName;
    private String galleryPath;
    private List<String> imagePathList = new ArrayList<String>();
    public LocalImageAdapter adapter;
    private GridView gridView;

    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            Toast.makeText(activity, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            Log.v(LOG_TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

            MySQLiteHelper db = new MySQLiteHelper(activity);
            String fileNamePlusId = dataItem.getFilename() + dataCollectionId;
            FileId fileId = new FileId(fileNamePlusId,"yes");
            db.insertFile(fileId);

            mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

            if(mPrefs.contains("RemovePhotosAfterUpload")) {
                if(mPrefs.getBoolean("RemovePhotosAfterUpload",true)) {

                    File file = typedFile.file();
                    Boolean deleted = file.delete();
                    Log.v(LOG_TAG, "deleted:" +deleted);
                }
            }
            adapter.notifyDataSetChanged();


            circularButton.setProgress(100);

        }

        @Override
        public void failure(RetrofitError error) {

            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                if(error.getKind().name().equalsIgnoreCase("NETWORK")) {
               //     Toast.makeText(activity, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));
                String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                if (jsonBody.contains("already exists")) {
                    // Toast.makeText(mContext.getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
              //  else
                    Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();

            }
            circularButton.setProgress(-1);

            Log.v(LOG_TAG, String.valueOf(error));

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.active_gallery_gridview);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = activity.getIntent();
        if (intent != null) {
            galleryName = intent.getStringExtra("galleryName");
            galleryPath = intent.getStringExtra("galleryPath");
            if(galleryName != null) {
                ///storage/emulated/0/DCIM/Screenshots
                titleView.setText(galleryName);
            }
        }

        File galleryFolder = new File(galleryPath);
        File[] galleryFiles = galleryFolder.listFiles();

        for (File imageFile : galleryFiles) {
            Log.v("file",imageFile.toURI().toString() );

            if(new ImageFileFilter(imageFile).accept(imageFile)) {
                imagePathList.add(imageFile.getAbsolutePath());
            }
        }

        adapter = new LocalImageAdapter(activity, imagePathList, true);

        gridView = (GridView) findViewById(R.id.image_gridView);

        circularButton = (CircularProgressButton) findViewById(R.id.circularButton);


        gridView.setAdapter(adapter);



        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int selectedCount = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                adapter.clearSelection();
                toolbar.setVisibility(View.VISIBLE);

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                selectedCount = 0;

                toolbar.setVisibility(View.GONE);

                //adapter.getCheckBox().setVisibility(View.VISIBLE);

                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.contextual_menu, menu);

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()) {

                    case R.id.item_delete:
                        selectedCount = 0;
                        Integer count = 0;

                        if(selectedDataPathList != null) {
                            delete(selectedDataPathList);
                            for(String str: selectedDataPathList){
                                imagePathList.remove(str);
                            }
                            adapter.notifyDataSetChanged();

                        }
                        selectedDataPathList.clear();
                        mode.finish();
                        break;


                    case R.id.item_upload:
                        selectedCount = 0;
                        //Integer count = 0;

                        Log.v(LOG_TAG,"##upload");

                        Log.v(LOG_TAG, " "+selectedDataPathList.size());

                        Log.v(LOG_TAG, selectedDataPathList.get(0));

                        circularButton.setVisibility(View.VISIBLE);
                        circularButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
                        circularButton.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress

                        if(selectedDataPathList != null) {
                            upload(selectedDataPathList);
                        }
                        selectedDataPathList.clear();
                        mode.finish();
                        break;
                }

                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position,
                                                  long id,
                                                  boolean checked) {
                // TODO Auto-generated method stub
                if (checked) {
                    selectedCount++;
                    adapter.setNewSelection(position, checked);
                    selectedDataPathList.add(imagePathList.get(position));

                    // toBeUploadDataPathList.add(dataPathList.get(position));
                    //adapter.getCheckBox().setChecked(true);

                } else {
                    selectedCount--;
                    adapter.removeSelection(position);
                    selectedDataPathList.remove(imagePathList.get(position));
                    //adapter.getCheckBox().setChecked(false);

                }
                mode.setTitle(selectedCount + " selected");

            }
        });

       /* UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intentNew = new Intent(this, UploadService.class);
        intentNew.putExtra("receiver", mReceiver);
        this.startService(intentNew);*/

        gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                // TODO Auto-generated method stub

                gridView.setItemChecked(position, !adapter.isPositionChecked(position));
                return false;
            }
        });




    }

    /*
        deletes the selected images
     */
    private void delete(List<String> toBeDeleteImagePathList) {

        for(String imagePath : toBeDeleteImagePathList) {

            File file = new File(imagePath);
            Boolean deleted = file.delete();
            Log.v(LOG_TAG, "##deleted:" + deleted);
          //  adapter.notifyDataSetChanged();

        }


    }

    /*
        uploads the selected files
     */
    private void upload(List<String> toBeDeleteImagePathList) {

        mPrefs = activity.getSharedPreferences("myPref", 0);
        String username = mPrefs.getString("username", "");
        String password = mPrefs.getString("password", "");
        dataCollectionId = mPrefs.getString("collectionID", DeviceStatus.collectionID);

        String jsonPart1 = "\"collectionId\" : \"" +
                dataCollectionId +
                "\"";

        for (String filePath : toBeDeleteImagePathList) {
            typedFile = new TypedFile("multipart/form-data", new File(filePath));

            String json ="{" + jsonPart1  +"}";

            Log.v(LOG_TAG, json);
            RetrofitClient.uploadItem(typedFile, json, callback, username, password);
            Log.v(LOG_TAG, "##upload:" + filePath);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_local, menu);
        return true;
    }



    /*
        method which receives the result from the activity
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 0:

                setProgressBarIndeterminateVisibility(true);
                break;
            case 1:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);

                //  String[] results = resultData.getStringArray("result");

                /* Update ListView with result */
                //ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_2, results);
                //listView.setAdapter(arrayAdapter);
              //  Toast.makeText(this, "Files are synced", Toast.LENGTH_LONG).show();




                mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

                SharedPreferences.Editor e = mPrefs.edit();
                e.putString("UploadStatus","true");
                e.commit();
                adapter.notifyDataSetChanged();
//                Intent showLocalImageIntent = new Intent(activity, LocalGalleryActivity.class);
//                startActivity(showLocalImageIntent);



/*                if(mPrefs.contains("L_A_U")) {

                    if(mPrefs.getBoolean("L_A_U", true)) {

                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        View popupView = inflater.inflate(R.layout.logout_confirm, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                550,
                                300);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        popupWindow.setAnimationStyle(R.style.AnimationPopup);

                        Button yes = (Button) popupView.findViewById(R.id.buttonYes);
                        Button no = (Button) popupView.findViewById(R.id.buttonNo);
                        popupWindow.showAtLocation(findViewById(R.id.navigation), Gravity.CENTER, 0, 0);

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                                Intent loginIntent = new Intent(activity, LoginActivity.class);
                                startActivity(loginIntent);
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });

                    }
                }*/

                break;
            case 2:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }

}
