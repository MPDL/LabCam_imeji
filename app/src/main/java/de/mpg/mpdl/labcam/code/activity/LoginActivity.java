package de.mpg.mpdl.labcam.code.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
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
import com.tbruyelle.rxpermissions.RxPermissions;

import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.code.base.BaseMvpActivity;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.data.model.UserModel;
import de.mpg.mpdl.labcam.code.injection.component.DaggerUserComponent;
import de.mpg.mpdl.labcam.code.injection.module.ImejiFolderModule;
import de.mpg.mpdl.labcam.code.injection.module.UserModule;
import de.mpg.mpdl.labcam.code.mvp.presenter.LoginPresenter;
import de.mpg.mpdl.labcam.code.mvp.view.LoginView;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;
import de.mpg.mpdl.labcam.code.utils.QRUtils;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import retrofit2.adapter.rxjava.HttpException;


public class LoginActivity extends BaseMvpActivity<LoginPresenter> implements LoginView {

    @BindView(R.id.userName) EditText usernameView;
    @BindView(R.id.password) EditText passwordView;
    @BindView(R.id.serverURL) EditText serverURLView;

    @BindView(R.id.label_gluons) TextView gluonsLabel;
    @BindView(R.id.label_other) TextView othersLabel;

    @BindView(R.id.tv_new_here)  TextView newHereView;
    @BindView(R.id.tv_register) TextView newRegister;
    @BindView(R.id.btnSignIn)  Button signIn;
    @BindView(R.id.qr_scanner)  Button scan;
    private BaseMvpActivity activity = this;

    private String username;
    private String password;
    private String serverURL;
    private View rootView;
    private static final int INTENT_QR = 1001;
    private String LOG_TAG = LoginActivity.class.getSimpleName();

    private String collectionId = null;
    private String collectionName = "for auto upload, please set a collection";

    @Override
    protected int getLayoutId() {
        return R.layout.layout_login;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        String Key = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
        serverURL =  PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");
        String otherServerUrl = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.OTHER_SERVER, "");

        /********************   if already have apiKey, jump over login steps ***********/
        if(!Key.equalsIgnoreCase("")){
            //login
            RetrofitClient.setRestServer(serverURL);
            Intent intent = new Intent(activity, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        /********************************************************************************/

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        usernameView.setText(PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.EMAIL, "")); // set last user email
        serverURLView.setText(otherServerUrl);

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

                if(serverURL.equalsIgnoreCase(DeviceStatus.BASE_URL)){
//                    serverURLView.setText("https://");
                }else {
                    serverURLView.setText(otherServerUrl);
                }
            }
        });

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

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        newHereView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://labcam.mpdl.mpg.de/"));
                startActivity(browserIntent);
            }
        });

        newRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gluons.mpdl.mpg.de/static/gluons-preregister/preregister.html"));
                startActivity(browserIntent);
            }

        });
    }

    private void login(){
        boolean cancel = false;
        View focusView = null;

        username = usernameView.getText().toString();
        password = passwordView.getText().toString();

        if(serverURLView.getVisibility()==View.VISIBLE) {
            serverURL = serverURLView.getText().toString();
        }else serverURL = DeviceStatus.BASE_URL;

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
            PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.USER_NAME, username);
            PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.PASSWORD, password);
            PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.SERVER_NAME, serverURL);

            if(serverURLView.getVisibility()==View.VISIBLE) {
                PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.OTHER_SERVER, serverURL);
            }
            String credentials = username + ":" + password;
            String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.API_KEY, string);
            mPresenter.basicLogin(this);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_QR) {

            if (resultCode == Activity.RESULT_OK) {

                String APIkey = QRUtils.processQRCode(data, activity, LOG_TAG, null).getAPIkey();
                collectionId = QRUtils.processQRCode(data, activity, LOG_TAG, null).getQrCollectionId();

                if(serverURLView.getVisibility()==View.VISIBLE) {
                    serverURL = serverURLView.getText().toString();
                }else serverURL = DeviceStatus.BASE_URL;

                RetrofitClient.setRestServer(serverURL);

                Log.v(LOG_TAG,serverURL);

                    //get collection
                PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.API_KEY, APIkey);
                PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.SERVER_NAME, serverURL);

                if(serverURLView.getVisibility()==View.VISIBLE) {
                    PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.OTHER_SERVER, serverURL);
                }
                PreferenceUtil.setString(this,Constants.SHARED_PREFERENCES,Constants.COLLECTION_ID, APIkey);
                mPresenter.basicLogin(this);


            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the photo picking
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void accountLogin(String userId, boolean isQRLogin) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("isQRLogin", isQRLogin);
        startActivity(intent);
        // layout_login out of stack
        finish();
    }

    // createTask only when with QRcode
    private void createTask(){
        String username = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_NAME, "");
        String userId = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");


        Task latestTask = new Select().from(Task.class).where("userId = ?",userId).where("uploadMode = ?","AU").executeSingle();

        if(latestTask==null){
            Log.v("create Task","no task in database");
            Task task = new Task();
            task.setUploadMode("AU");
            task.setCollectionId(collectionId);
            task.setCollectionName(collectionName);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(username);
            task.setUserId(userId);
            task.setTotalItems(0);
            task.setFinishedItems(0);
            task.setServerName(serverURL);

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
            task.setUploadMode("AU");
            task.setCollectionId(collectionId);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(username);
            task.setUserId(userId);
            task.setTotalItems(0);
            task.setFinishedItems(0);
            task.setServerName(serverURL);

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

    @Override
    protected void injectComponent() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .userModule(new UserModule())
                .imejiFolderModule(new ImejiFolderModule())
                .build()
                .inject(this);
        mPresenter.setView(this);
    }

    private void scanQR(){
        Intent intent = new Intent(activity, QRScannerActivity.class);
        startActivityForResult(intent, INTENT_QR);
    }

    private void checkPermission() {
        RxPermissions rxp = new RxPermissions(activity);
        rxp.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        scanQR();
                    }
                    else {
                        showToast(R.string.exception_no_photo_permission);
                    }
                });
    }

    @Override
    public void loginSuc(UserModel user) {
        String userCompleteName = "";
        userCompleteName = user.getPerson().getCompleteName();
        if(userCompleteName!="" && userCompleteName!=null){
            PreferenceUtil.setString(getApplicationContext(),Constants.SHARED_PREFERENCES,Constants.USER_NAME, user.getPerson().getCompleteName());
            PreferenceUtil.setString(getApplicationContext(),Constants.SHARED_PREFERENCES,Constants.FAMILY_NAME, user.getPerson().getFamilyName());
            PreferenceUtil.setString(getApplicationContext(),Constants.SHARED_PREFERENCES,Constants.GIVEN_NAME, user.getPerson().getGivenName());
            PreferenceUtil.setString(getApplicationContext(),Constants.SHARED_PREFERENCES,Constants.USER_ID, user.getPerson().getId());
            PreferenceUtil.setString(getApplicationContext(),Constants.SHARED_PREFERENCES,Constants.EMAIL, user.getEmail());
            PreferenceUtil.setString(getApplicationContext(),Constants.SHARED_PREFERENCES,Constants.API_KEY, user.getApiKey());
            PreferenceUtil.setString(getApplicationContext(),Constants.SHARED_PREFERENCES,Constants.SERVER_NAME, serverURL);
            if(collectionId!=null&&collectionId!=""){   // login with qr code
                mPresenter.getCollectionById(collectionId, activity);

                //create a new task for new selected collection
            }else {
                Toast.makeText(activity,"Welcome "+userCompleteName,Toast.LENGTH_SHORT).show();
                accountLogin(user.getPerson().getId(),false);
            }

        }
    }

    @Override
    public void loginFail(Throwable e) {

        PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.API_KEY);

        HttpException httpException = (HttpException)e;

            switch (httpException.code()) {
                case 401:
                    Toast.makeText(activity, "username or password wrong", Toast.LENGTH_SHORT).show();
                    if(passwordView.getText().length()>0)
                        passwordView.selectAll();
                    return;
                case 404:
                    Toast.makeText(activity, "server not response", Toast.LENGTH_SHORT).show();
                    return;
                case 0:
                    Toast.makeText(activity, serverURL+ " please check your wifi connection", Toast.LENGTH_LONG).show();
                    return;
            }
    }

    @Override
    public void getCollectionByIdSuc(ImejiFolderModel imejiFolder) {
        Log.v(LOG_TAG,"success");
        collectionName = imejiFolder.getTitle();
        createTask();
        collectionId = null;
    }

    @Override
    public void getCollectionByIdFail(Throwable e) {
        Log.v(LOG_TAG,"failed");
        Toast.makeText(activity,"The collectionId in QR code is not valid",Toast.LENGTH_LONG).show();
    }
}