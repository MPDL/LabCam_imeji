package example.com.mpdlcamera.Auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

import java.net.URL;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.User;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Utils.DeviceStatus;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

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

//                Log.v(LOG_TAG, QRText);

                try {
                    URL u = new URL(QRText);

                    String path = u.getPath();
                    String collectionId = null;
                    if (path != null) {
                        collectionId = path.substring(path.lastIndexOf("/") + 1);
                    }

                    String query = u.getQuery();
                    MultiMap<String> values = new MultiMap<String>();
                    UrlEncoded.decodeTo(query, values, "UTF-8", 1000);


                    usernameView.setText(values.getString("username"));
                    passwordView.setText(values.getString("password"));

                    mPrefs = getSharedPreferences("myPref", 0);
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("username", values.getString("username")).apply();
                    mEditor.putString("password", values.getString("password")).apply();
                    mEditor.putString("collectionID", collectionId).apply();
//                    Log.v(LOG_TAG, values.getString("username"));
//                    Log.v(LOG_TAG, values.getString("password"));
//                    Log.v(LOG_TAG, collectionId);

                    accountLogin();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the photo picking
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


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

    /**
     * callbacks
     */

    Callback<User> callback_login = new Callback<User>() {
        @Override
        public void success(User user, Response response) {
            Log.v(LOG_TAG, "Login success");
            if(username!="" && username!=null){
                Toast.makeText(activity,"Welcome "+username,Toast.LENGTH_SHORT).show();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    accountLogin();
                }
            }, 1000);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v("~~~","login failed" );
        }
    };
}