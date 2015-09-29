package example.com.mpdlcamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.FolderModel;
import example.com.mpdlcamera.Model.MetaData;
import example.com.mpdlcamera.Model.User;
import example.com.mpdlcamera.Otto.OttoSingleton;
import example.com.mpdlcamera.Otto.UploadEvent;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

/**
 * Created by kiran on 23.09.15.
 */
public class CustomAdapter extends ArrayAdapter<FolderModel> {

    Context mContext;

   /* public CustomAdapter(Context mContext,int resource,ArrayList<FolderModel> list ) {
        super(mContext,resource);
        this.mContext = mContext;
    } */

    ArrayList<String> permFolder = new ArrayList<String>();
    private final String LOG_TAG = CustomAdapter.class.getSimpleName();
    private String username;
    private String password;
    String prefOption;
    String networkStatus;
    private DataItem item = new DataItem();
    private MetaData meta = new MetaData();
    private String collectionID = DeviceStatus.collectionID;
    private User user;
    public TypedFile typedFile;
    String json;

    private ArrayList<FolderModel> folderList;

    public CustomAdapter(Context context, int textViewResourceId,
                         ArrayList<FolderModel> folderList) {
        super(context, textViewResourceId, folderList);
        this.mContext = context;
        this.folderList = new ArrayList<FolderModel>();
        this.folderList.addAll(folderList);
    }

    private class ViewHolder {
        TextView textView;
        Switch fSwitch;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.row, null);

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.folder);
            holder.fSwitch = (Switch) convertView.findViewById(R.id.fswitch);
            convertView.setTag(holder);

            holder.fSwitch.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Switch sw = (Switch) v;
                    FolderModel folder = (FolderModel) sw.getTag();

                    folder.setSelected(sw.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.fSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!isChecked) {

                    RelativeLayout rl = (RelativeLayout) buttonView.getParent();
                    TextView tv = (TextView) rl.findViewById(R.id.folder);
                    String folder = tv.getText().toString();



                    Iterator folderIterator = permFolder.iterator();

                    while (folderIterator.hasNext()) {
                        if (folder.equalsIgnoreCase(folderIterator.next().toString()))
                            permFolder.remove(folder);
                    }

                }

                else if(isChecked)
                {

                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                    prefOption = settings.getString("status", "");

                    if (prefOption.equalsIgnoreCase("both") || (prefOption.equalsIgnoreCase("Wifi") && (networkStatus.equalsIgnoreCase("wifi")))) {
                        RelativeLayout rl = (RelativeLayout) buttonView.getParent();
                        TextView tv = (TextView) rl.findViewById(R.id.folder);
                        String folder = tv.getText().toString();
                        permFolder.add(folder);
                        //Toast.makeText(getApplicationContext(), folder + "is now synced", Toast.LENGTH_LONG).show();

                        Uri uri;
                        Cursor cursor;
                        user = new User();
                        user.setCompleteName("Kiran");
                        user.save();

                        int column_index_data, column_index_folder_name, column_index_file_name;
                        ArrayList<String> listOfAllImages = new ArrayList<String>();
                        String absolutePathOfImage = null;
                        String absoluteFileName = null;
                        String absoluteFolderName = null;
                        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                        String[] projection = {MediaStore.MediaColumns.DATA,
                                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DISPLAY_NAME};

                        cursor = mContext.getContentResolver().query(uri, projection, null,
                                null, null);

                        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);


                        column_index_folder_name = cursor
                                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                        column_index_file_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

                        HashMap<String, String> namePathMap = new HashMap<String, String>();

                        while (cursor.moveToNext()) {
                            absolutePathOfImage = cursor.getString(column_index_data);

                            absoluteFolderName = cursor.getString(column_index_folder_name);

                            absoluteFileName = cursor.getString(column_index_file_name);

                            if (absoluteFolderName.equalsIgnoreCase(folder)) {


                                listOfAllImages.add(absolutePathOfImage);

                                namePathMap.put(absoluteFileName, absolutePathOfImage);
                            }
                        }


                        Iterator hashIterator = namePathMap.keySet().iterator();
                        while (hashIterator.hasNext()) {
                            String fileName = (String) hashIterator.next();
                            String filePath = (String) namePathMap.get(fileName);
                            item.setFilename(fileName);
                            meta.setTags(null);

                            meta.setAddress("blabla");

                            meta.setTitle(fileName);

                            meta.setCreator(user.getCompleteName());

                            item.setCollectionId(collectionID);

                            item.setLocalPath(filePath);

                            item.setMetadata(meta);

                            item.setCreatedBy(user);

                            meta.save();
                            item.save();

                            //  dataList.add(item);

                       /* if(prefOption.equalsIgnoreCase("both")) {
                            upload(item);
                        }
                        else if (prefOption.equalsIgnoreCase("wifi") && (networkStatus.equalsIgnoreCase("wifi"))) {
                            upload(item);
                        }
                        else */
                            upload(item);

                        }
                   /* for(String imageInfo : listOfAllImages) {

                        Bitmap bm = BitmapFactory.decodeFile(imageInfo);
                        upload();
                    } */


                        // upload(dataList);


                    } else {
                        Toast.makeText(mContext.getApplicationContext(), "Please Switch On Wifi or change your Network Preference", Toast.LENGTH_LONG).show();
                    }

                }

                // upload(dataList)

            }
        });


        FolderModel folder = folderList.get(position);
        holder.textView.setText(folder.getFolder());

        holder.fSwitch.setChecked(folder.isSelected());
        holder.fSwitch.setTag(folder);

        return convertView;

    }
    private void upload(DataItem item) {
        String jsonPart1 = "\"collectionId\" : \"" +
                collectionID +
                "\"";
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        item.getMetadata().setDeviceID("1");
        typedFile = new TypedFile("multipart/form-data", new File(item.getLocalPath()));


        // json = "{" + jsonPart1 + "}";
        json = "{" + jsonPart1 + "}";

        Log.v(LOG_TAG, json);
        RetrofitClient.uploadItem(typedFile, json, callback, username, password);


    }

    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            Toast.makeText(mContext.getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_LONG).show();
            Log.v(LOG_TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());




          /*  List<DataItem> tempList =  dataList;
            for(int i = 0; i<dataList.size(); i++){
                DataItem d = tempList.get(i);
                dataList.remove(d);
            } */


            if (new Select()
                    .from(DataItem.class)
                    .where("isLocal = ?", true)
                    .execute().size() < 1) {
                //upload a POI as Album on Imeji
                // RetrofitClient.createPOI(createNewPOI(), callbackPoi, username, password);

                //You cannot modify, add/remove, a List while iterating through it.
                //The foreach loop you are using creates an Iterator object in the background.
                // Use a regular for loop if you'd like to modify the list.

//            for (DataItem item: dataList){
//                //if(item.getFilename().equals(dataItem.getFilename())){
//                    dataList.remove(item);
//                //}
//            }

//            List<DataItem> tempList =  dataList;
//            for(int i = 0; i<dataList.size(); i++){
//                DataItem d = tempList.get(i);
//                dataList.remove(d);
//            }


            }
        }

        @Override
        public void failure(RetrofitError error) {

            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                Toast.makeText(mContext.getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));
                String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                if (jsonBody.contains("already exists")) {
                    Toast.makeText(mContext.getApplicationContext(), "File already synced ", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(mContext.getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();

            }

            //Log.v(LOG_TAG, jsonBody);

            Log.v(LOG_TAG, String.valueOf(error));

        }
    };
}