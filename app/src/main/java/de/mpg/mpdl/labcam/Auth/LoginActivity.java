package de.mpg.mpdl.labcam.Auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mpg.mpdl.labcam.Folder.MainActivity;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.User;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import de.mpg.mpdl.labcam.Utils.QRUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.userName) EditText usernameView;
    @BindView(R.id.password) EditText passwordView;
    @BindView(R.id.serverURL) EditText serverURLView;

    @BindView(R.id.label_gluons) TextView gluonsLabel;
    @BindView(R.id.label_other) TextView othersLabel;

    @BindView(R.id.tv_new_here)  TextView newHereView;
    @BindView(R.id.btnSignIn)  Button signIn;
    @BindView(R.id.qr_scanner)  Button scan;
    private Activity activity = this;

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

        mPrefs = getSharedPreferences("myPref", 0);
        String Key = mPrefs.getString("apiKey", "");

        // check login states
        if(Key.equalsIgnoreCase("")){
            setContentView(R.layout.layout_login);
            ButterKnife.bind(this);
        }else {
            //login
            serverURL = mPrefs.getString("server", "");
            RetrofitClient.setRestServer(serverURL);
            Intent intent = new Intent(activity, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        // use soft keyboard enter to login
        passwordView.setImeOptions(EditorInfo.IME_ACTION_SEND);
        passwordView.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
            KeyEvent event){
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode()
                        && KeyEvent.ACTION_DOWN == event.getAction())) {
                  login();
                }
                return false;
            }
        });

        // gluons server is choosen
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

        // other server is choosen
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

        // last user
        usernameView.setText(mPrefs.getString("email", ""));

        // store server url in sharedPreference
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

        } else { // BASE_URL as serverURL
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

            mPrefs = getSharedPreferences("myPref", 0);
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putString("username", username).apply();
            mEditor.putString("password", password).apply();
            mEditor.putString("server", serverURL).apply();
            SharedPreferences preferences = getSharedPreferences("folder", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = preferences.edit();
            ed.putString("Camera", "On");
            ed.commit();
            RetrofitClient.login(username,password,callback_login);
        }

    }

    //    apiKey  LOG_TAG activity userId serverUrl collectionId

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_QR) {

            if (resultCode == Activity.RESULT_OK) {

                    String APIkey = QRUtils.processQRCode(data, activity, LOG_TAG, null).getAPIkey();
                    collectionId = QRUtils.processQRCode(data, activity, LOG_TAG, null).getQrCollectionId();

                    serverURL = serverURLView.getText().toString();

                    /** parse server url **/

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


            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the photo picking
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    public void accountLogin(String userId, boolean isQRLogin) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("isQRLogin", isQRLogin);
        startActivity(intent);
        // layout_login out of stack
        finish();
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
        accountLogin(userId,true);
        Toast.makeText(activity,"Successfully login with QR code",Toast.LENGTH_LONG).show();
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
            String userCompleteName = "";
            userCompleteName = user.getPerson().getCompleteName();
            if(userCompleteName!="" && userCompleteName!=null){
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
                if(collectionId!=null&&collectionId!=""){   // login with qr code
                    RetrofitClient.getCollectionById(collectionId, callback_collection, user.getApiKey());

                    //create a new task for new selected collection
                }else {
                    Toast.makeText(activity,"Welcome "+userCompleteName,Toast.LENGTH_SHORT).show();
                    accountLogin(user.getPerson().getId(),false);
                }

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
            collectionId = null;
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG,"failed");
            Toast.makeText(activity,"The collectionId in QR code is not valid",Toast.LENGTH_LONG).show();
        }
    };
}