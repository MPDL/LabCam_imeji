package example.com.mpdlcamera.Auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.LocalAlbum;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Model.User;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Utils.DeviceStatus;
import example.com.mpdlcamera.Utils.ImageFileFilter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends AppCompatActivity {

    private EditText usernameView, passwordView, serverURLView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //don't store local images
//        getLocalFolders();

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        serverURLView = (EditText) findViewById(R.id.serverURL);
        usernameView = (EditText) findViewById(R.id.userName);
        passwordView = (EditText) findViewById(R.id.password);
        signIn = (Button) findViewById(R.id.btnSignIn);
        scan = (Button) findViewById(R.id.qr_scanner);
        //error = (TextView) findViewById(R.id.tv_error);


        mPrefs = this.getSharedPreferences("myPref", 0);
        usernameView.setText(mPrefs.getString("username", ""));
        passwordView.setText(mPrefs.getString("password", ""));
        if (!mPrefs.getString("server", "").equals("")) {
            serverURL = mPrefs.getString("server", "");
        } else {
            serverURL = DeviceStatus.BASE_URL;
        }

        //it is called at login, what about without login?
        RetrofitClient.setRestServer(serverURL);

        serverURLView.setText(serverURL);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean cancel = false;
                View focusView = null;

                username = usernameView.getText().toString();
                password = passwordView.getText().toString();
                serverURL = serverURLView.getText().toString();

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
        });


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, QRScannerActivity.class);
                startActivityForResult(intent, INTENT_QR);
            }
        });

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
                }


                try {

                    URL u = new URL(url);

                    String path = u.getPath();

                    if (path != null) {
                        collectionId = path.substring(path.lastIndexOf("/") + 1);
                        Log.v("collectionId", collectionId);
                    }


                    mPrefs = getSharedPreferences("myPref", 0);
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.clear();
                    mEditor.commit();
                    mEditor.putString("APIkey",APIkey);
//                    mEditor.putString("username", values.getString("username")).apply();
//                    mEditor.putString("password", values.getString("password")).apply();
                    mEditor.putString("collectionID", collectionId).apply();
                    mEditor.commit();
//                    Log.v(LOG_TAG, values.getString("username"));
//                    Log.v(LOG_TAG, values.getString("password"));
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

    public void accountLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

    private void createTask(){
        mPrefs = getSharedPreferences("myPref", 0);
        String userName = mPrefs.getString("username", "");

        Task latestTask = getTask();

        if(latestTask==null){
            Log.v("create Task","no task in database");
            Task task = new Task();
            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionId);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(userName);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            Log.v("now", now+"");
            task.setStartDate(String.valueOf(now));
            task.setTaskName("AU to" + collectionId + currentDateTimeString);

            task.save();

        }else if(latestTask.getCollectionId()!=collectionId) {
            Task task = new Task();
            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionId);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(userName);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            Log.v("now", now+"");
            task.setStartDate(String.valueOf(now));
            task.setTaskName("AU to" + collectionId + currentDateTimeString);

            task.save();
        }
    }

    //get latest task
     public static Task getTask() {
     return new Select()
     .from(Task.class)
     .orderBy("startDate DESC")
     .executeSingle();
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
            Log.v("<<<", userCompleteName);
            if(userCompleteName!="" && userCompleteName!=null){
                Toast.makeText(activity,"Welcome "+userCompleteName,Toast.LENGTH_SHORT).show();
                mPrefs = getSharedPreferences("myPref", 0);
                SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("username",user.getPerson().getCompleteName()).apply();
                mEditor.commit();

                if(collectionId!=null&&collectionId!=""){
                    //create a new task for new selected collection
                    createTask();
                }
            }
            accountLogin();
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                public void run() {
//
//                }
//            }, 1000);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v("~~~","login failed" );
            Log.v("~~~",error.getMessage());
        }
    };
}