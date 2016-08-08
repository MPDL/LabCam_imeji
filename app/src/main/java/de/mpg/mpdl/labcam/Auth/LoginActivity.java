package de.mpg.mpdl.labcam.Auth;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.mpg.mpdl.labcam.Folder.MainActivity;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.LocalAlbum;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.User;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import de.mpg.mpdl.labcam.Utils.ImageFileFilter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends AppCompatActivity {

    private EditText usernameView, passwordView, serverURLView;
    private TextView gluonsLabel, othersLabel;
    private TextView newHereView;
    private Button signIn;
    private Button scan;
    private Activity activity = this;
    private ImageView animation;

    private String username;
    private String password;
    private String serverURL;
    private SharedPreferences mPrefs;
    private View rootView;
    private static final int INTENT_QR = 1001;
    private String LOG_TAG = LoginActivity.class.getSimpleName();

    private String collectionId = null;
    private String collectionName = "for auto upload, please set a collection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        mPrefs = getSharedPreferences("myPref", 0);
        String Key = mPrefs.getString("apiKey", "");

        if(Key.equalsIgnoreCase("")){
        setContentView(R.layout.layout_login);
        }else {
            //login
            serverURL = mPrefs.getString("server", "");
            RetrofitClient.setRestServer(serverURL);
            Intent intent = new Intent(activity, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        //don't store local images
//        getLocalFolders();

        gluonsLabel = (TextView) findViewById(R.id.label_gluons);
        othersLabel = (TextView) findViewById(R.id.label_other);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        serverURLView = (EditText) findViewById(R.id.serverURL);
        usernameView = (EditText) findViewById(R.id.userName);
        passwordView = (EditText) findViewById(R.id.password);
        newHereView = (TextView) findViewById(R.id.tv_new_here);

        signIn = (Button) findViewById(R.id.btnSignIn);
        scan = (Button) findViewById(R.id.qr_scanner);
        //error = (TextView) findViewById(R.id.tv_error);

        gluonsLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gluonsLabel
                gluonsLabel.setTextColor(Color.parseColor("#ffffff"));
                gluonsLabel.setBackground(getResources().getDrawable(R.drawable.round_button));
                //othersLabel
                othersLabel.setTextColor(Color.parseColor("#cccccc"));
                othersLabel.setBackground(null);
                serverURLView.setVisibility(View.GONE);
                serverURLView.setText(R.string.url_gluons);
            }
        });

        othersLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //gluonsLabel
                othersLabel.setTextColor(Color.parseColor("#ffffff"));
                othersLabel.setBackground(getResources().getDrawable(R.drawable.round_button));
                //othersLabel
                gluonsLabel.setTextColor(Color.parseColor("#cccccc"));
                gluonsLabel.setBackground(null);
                serverURLView.setVisibility(View.VISIBLE);

                if(serverURL.contains("gluons")){
                    serverURLView.setText("https://");
                }else {
                    serverURLView.setText(serverURL);
                }
            }
        });


//        mPrefs = this.getSharedPreferences("myPref", 0);
        usernameView.setText(mPrefs.getString("email", ""));

        // store server url
        if (!mPrefs.getString("server", "").equals("") && !mPrefs.getString("server", "").equals(DeviceStatus.BASE_URL)){
            serverURL = mPrefs.getString("server", "");
            //gluonsLabel
            othersLabel.setTextColor(Color.parseColor("#ffffff"));
            othersLabel.setBackground(getResources().getDrawable(R.drawable.round_button));
            //othersLabel
            gluonsLabel.setTextColor(Color.parseColor("#cccccc"));
            gluonsLabel.setBackground(null);
            serverURLView.setVisibility(View.VISIBLE);
            serverURLView.setText(serverURL);

        } else {
            serverURL = DeviceStatus.BASE_URL;

            //gluonsLabel
            gluonsLabel.setTextColor(Color.parseColor("#ffffff"));
            gluonsLabel.setBackground(getResources().getDrawable(R.drawable.round_button));
            //othersLabel
            othersLabel.setTextColor(Color.parseColor("#cccccc"));
            othersLabel.setBackground(null);
            serverURLView.setVisibility(View.GONE);
            serverURLView.setText(R.string.url_gluons);
        }

        RetrofitClient.setRestServer(serverURL);

        serverURLView.setText(serverURL);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, QRScannerActivity.class);
                startActivityForResult(intent, INTENT_QR);
            }
        });

        newHereView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://labcam.mpdl.mpg.de/"));
                startActivity(browserIntent);
            }
        });

    }

    private void login(){
        boolean cancel = false;
        View focusView = null;

        username = usernameView.getText().toString();
        password = passwordView.getText().toString();

        String url;
        serverURL = serverURLView.getText().toString();

        /** parse server url **/

//                RetrofitClient.setRestServer(parseServerUrl(serverURL));
        RetrofitClient.setRestServer(serverURL);

        Log.v(LOG_TAG,serverURL);
        usernameView.setError(null);
        passwordView.setError(null);

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error, focus the first form field with an error.
            focusView.requestFocus();
        } else {
//                    usernameView.setEnabled(false);
//                    passwordView.setEnabled(false);

            mPrefs = getSharedPreferences("myPref", 0);
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putString("username", username).apply();
            mEditor.putString("password", password).apply();
            mEditor.putString("server", serverURL).apply();
            SharedPreferences preferences = getSharedPreferences("folder", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = preferences.edit();
            ed.putString("Camera", "On");
            ed.commit();
//                    DeviceStatus.showSnackbar(rootView, "Login Successfully");
            RetrofitClient.login(username,password,callback_login);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_QR) {

            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                String QRText = bundle.getString("QRText");
                Log.v(LOG_TAG, QRText);
                String APIkey = "";
                String url = "";
                try {
                    JSONObject jsonObject = new JSONObject(QRText);
                    APIkey = jsonObject.getString("key");
                    Log.v("APIkey",APIkey);
                    url = jsonObject.getString("col");
                    Log.v("col",url);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity,"parse qrCode failed, please scan again",Toast.LENGTH_LONG).show();
                    return;
                }


                try {

                    URL u = new URL(url);

                    String path = u.getPath();

                    if (path != null) {
                        try {
                            collectionId = path.substring(path.lastIndexOf("/") + 1);
                            Log.i(LOG_TAG,collectionId);
                        }catch (Exception e){
                            Toast.makeText(activity,"qrCode not legal",Toast.LENGTH_LONG).show();
                            return;
                        }

                    }

                    serverURL = serverURLView.getText().toString();

                    /** parse server url **/

//                RetrofitClient.setRestServer(parseServerUrl(serverURL));
                    RetrofitClient.setRestServer(serverURL);

                    Log.v(LOG_TAG,serverURL);

                    //get collection
                    mPrefs = getSharedPreferences("myPref", 0);
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("APIkey",APIkey).apply();
                    mEditor.putString("server", serverURL).apply();
                    mEditor.putString("collectionID", collectionId).apply();
                    mEditor.commit();
                    RetrofitClient.apiLogin(APIkey,callback_login);

//                    accountLogin();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the photo picking
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void getLocalFolders(){
        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.DATA};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        final ArrayList<String> localImageFolders = new ArrayList<String>();

        final HashMap<String,String> albumFolders = new HashMap<String, String>();

        Cursor cur = getContentResolver().query(images, albums, null, null, null);

        if (cur.moveToFirst()) {
            String album;
            String filePath;
            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int path = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                //name of the local folder
                album = cur.getString(albumLocation);
//                Log.i("folderName",album);

                //image file path
                filePath = cur.getString(path);
//                Log.i("filePath",filePath);

                //folder path for files
                File file = new File(filePath);
                String directory = file.getParent();
//                Log.i("directory", directory);
//                SharedPreferences.Editor editor = preferencesFiles.edit();
//                editor.putString(album, directory);
//                editor.commit();

                if(!albumFolders.containsKey(album))
                albumFolders.put(album,directory);

            } while (cur.moveToNext());
            for(int i=0;i<localImageFolders.size();i++){
            Log.i("localImageFolders", "Folder" + i+": "+localImageFolders.get(i));}
            
            Iterator it = albumFolders.entrySet().iterator();


            while ((it.hasNext())) {
                String folderPath;
                String folderName;
                Map.Entry pair = (Map.Entry)it.next();
                //folderPath
                folderPath = pair.getValue().toString();
                folderName = pair.getKey().toString();

                Log.i("folderPath",folderPath);
                Log.i("folderName",folderName);

                File folder = new File(folderPath);
                //listing all the files
                File[] folderFiles = folder.listFiles();

                ActiveAndroid.beginTransaction();
                try {
                    for (File imageFile : folderFiles) {
//                        Log.i("file", imageFile.toURI().toString());

                        //only "jpg", "png", "gif","jpeg" accepted
                        if (new ImageFileFilter(imageFile).accept(imageFile)) {
                            Image image = new Image();
                            //set ImageId as path
                            // FIXME: 1/13/16 change model if needed
                            image.setImageId(imageFile.getAbsolutePath());
                            image.save();
                        }
                    }
                    ActiveAndroid.setTransactionSuccessful();
                }finally {
                    ActiveAndroid.endTransaction();
                }
                it.remove();

                //print out table
//                List<Image> imageList = getImages(folderName);
//                for (int i =0;i<imageList.size();i++){
//
//                    Image image = imageList.get(i);
//                    Log.i("Images",": "+image.getImageId());
//
//                }
            }

            //save album to LocalAlbum table
            ActiveAndroid.beginTransaction();
            try {
                while (it.hasNext()){
                    Map.Entry pair = (Map.Entry)it.next();
                    System.out.println(pair.getKey() + " = " + pair.getValue());
                    LocalAlbum localAlbum = new LocalAlbum();
                    localAlbum.setAlbumName(pair.getKey().toString());
                    localAlbum.setAlbumDirectory(pair.getValue().toString());
                    localAlbum.save();
                    it.remove();
                }
                ActiveAndroid.setTransactionSuccessful();
            }
            finally {
                ActiveAndroid.endTransaction();
            }
//            Log.i("~~~", getRandom().getAlbumName());

        }
    }

    /**

    public static List<Image> getImages(String q) {
        return new Select()
                .from(Image.class)
                .where(" = ?",q)
                .orderBy("imageName ASC")
                .execute();
    }
     */
    boolean isTaskFragment = false;

    public void accountLogin(String userId) {


        List<Task> taskList = DeviceStatus.getUserTasks(userId);
        boolean isFinished = true;
        for(Task task:taskList){

            if(task.getFinishedItems()<task.getTotalItems()){
                isFinished = false;
            }
        }
            if(!isFinished) {

                new AlertDialog.Builder(this)
                        .setTitle("Welcome")
                        .setMessage("There are some uploading tasks not be compeleted last time, you can restart them in your Task Manager")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                isTaskFragment = true;

                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.putExtra("isTaskFragment",isTaskFragment);
                                startActivity(intent);
                                // layout_login out of stack
                                finish();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.putExtra("isTaskFragment",isTaskFragment);
                                startActivity(intent);
                                // layout_login out of stack
                                finish();
                            }
                        })
                        .setIcon(R.drawable.error_alert)
                        .show();
            }else if(isFinished){
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("isTaskFragment",isTaskFragment);
                startActivity(intent);
                // layout_login out of stack
                finish();
            }
    }


    private static void jettyUtil(String url) throws Exception {
        URL u = new URL(url);

        String path = u.getPath();
        String collectionId = null;
        if (path != null) {
            collectionId = path.substring(path.lastIndexOf("/") + 1);
        }

        String query = u.getQuery();
        MultiMap<String> values = new MultiMap<String>();
        UrlEncoded.decodeTo(query, values, "UTF-8", 1000);

        System.out.println(collectionId);
        System.out.println(values.getString("username"));
        System.out.println(values.getString("password"));
    }


    // createTask only when with QRcode
    private void createTask(){
        mPrefs = getSharedPreferences("myPref", 0);
        String userName = mPrefs.getString("username", "");
        String userId = mPrefs.getString("userId", "");

        Task latestTask = new Select().from(Task.class).where("userId = ?",userId).where("uploadMode = ?","AU").executeSingle();

        if(latestTask==null){
            Log.v("create Task","no task in database");
            Task task = new Task();
            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionId);
            task.setCollectionName(collectionName);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(userName);
            task.setUserId(userId);
            task.setTotalItems(0);
            task.setFinishedItems(0);
            task.setSeverName(serverURL);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            Log.v("now", now+"");
            task.setStartDate(String.valueOf(now));
            task.setCollectionName(collectionName);

            task.save();

        }else if(!latestTask.getCollectionId().equals(collectionId)) {
            // last AuTask exist delete and create new
            Log.v("collectionID",collectionId);
            new Delete().from(Task.class).where("uploadMode = ?", "AU").execute();

            Task task = new Task();
            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionId);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(userName);
            task.setUserId(userId);
            task.setTotalItems(0);
            task.setFinishedItems(0);
            task.setSeverName(serverURL);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            Log.v("now", now+"");
            task.setStartDate(String.valueOf(now));
            task.setCollectionName(collectionName);

            task.save();
        }

        // QR layout_login
        accountLogin(userId);
    }

    /**
     * method for parse server url, examples as below
     *
     * https://spot.mpdl.mpg.de/rest
     * https://spot.mpdl.mpg.de/rest/
     * spot.mpdl.mpg.de
     * spot.mpdl.mpg.de/rest
     * spot.mpdl.mpg.de/rest/
     */
    private String url1 = "https://spot.mpdl.mpg.de/rest";
    private String url2 = "https://spot.mpdl.mpg.de/rest/";
    private String url3 = "spot.mpdl.mpg.de";
    private String url4 = "spot.mpdl.mpg.de/rest";
    private String url5 = "spot.mpdl.mpg.de/rest/";

    private String parseServerUrl(String Url){
        // divide string
        String[] parts = Url.split("/");
        String coreUrl = null;
        String serverUrl = null;
        for (int i = 0;i < parts.length;i++){
            if(parts[i].equalsIgnoreCase("https:")){
                // ignore https:
            } else if(parts[i].equalsIgnoreCase("")){
                // ignore empty
            } else if (parts[i].equalsIgnoreCase("rest")) {
                // also ignore rest
            } else {
                 coreUrl = parts[i];
            }
        }
        serverUrl = "http://"+coreUrl+"/rest/";
        return serverUrl;
    }

    /**
     * callbacks
     */

    Callback<User> callback_login = new Callback<User>() {
        @Override
        public void success(User user, Response response) {
            Log.v(LOG_TAG, "Login success");
            Log.v(LOG_TAG,user.getQuota()+"");

            String userCompleteName = "";
            userCompleteName = user.getPerson().getCompleteName();
            if(userCompleteName!="" && userCompleteName!=null){
                Toast.makeText(activity,"Welcome "+userCompleteName,Toast.LENGTH_SHORT).show();
                mPrefs = getSharedPreferences("myPref", 0);
                SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("username",user.getPerson().getCompleteName()).apply();
                    mEditor.putString("familyName",user.getPerson().getFamilyName()).apply();
                    mEditor.putString("givenName",user.getPerson().getGivenName()).apply();
                    mEditor.putString("userId",user.getPerson().getId()).apply();
                    mEditor.putString("email",user.getEmail()).apply();
                    mEditor.putString("apiKey",user.getApiKey()).apply();
                    mEditor.putString("server",serverURL).apply();
                mEditor.commit();
                if(collectionId!=null&&collectionId!=""){
                     RetrofitClient.getCollectionById(collectionId, callback_collection, user.getApiKey());
                    //create a new task for new selected collection
                }else { accountLogin(user.getPerson().getId());}
            }

        }

        @Override
        public void failure(RetrofitError error) {

            if(error.getResponse()==null){
                Toast.makeText(activity, serverURL+ " please check your wifi connection", Toast.LENGTH_LONG).show();
                return;
            }

            if(error.getResponse().getStatus()==401) {
                Toast.makeText(activity, "username or password wrong", Toast.LENGTH_SHORT).show();
            }else if(error.getResponse().getStatus()==404){
                Toast.makeText(activity, "server not response", Toast.LENGTH_SHORT).show();
            }
        }
    };

    Callback<ImejiFolder> callback_collection = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            Log.v(LOG_TAG,"success");
            collectionName = imejiFolder.getTitle();
            createTask();
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG,"failed");
        }
    };
}