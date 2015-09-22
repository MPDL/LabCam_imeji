package example.com.mpdlcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Utils.DeviceStatus;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameView, passwordView;
    private Button signIn;
    private String errorMsg, resp ;
    private ImageView animation;

    private String username;
    private String password;
    private SharedPreferences mPrefs;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        usernameView = (EditText) findViewById(R.id.userName);
        passwordView = (EditText) findViewById(R.id.password);
        signIn = (Button) findViewById(R.id.btnSignIn);
        //error = (TextView) findViewById(R.id.tv_error);


        mPrefs = this.getSharedPreferences("myPref", 0);
        usernameView.setText(mPrefs.getString("username", ""));
        passwordView.setText(mPrefs.getString("password", ""));

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean cancel = false;
                View focusView = null;

                username = usernameView.getText().toString();
                password = passwordView.getText().toString();

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
                    usernameView.setEnabled(false);
                    passwordView.setEnabled(false);

                    mPrefs = getSharedPreferences("myPref", 0);
                    SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.putString("username", username).apply();
                    mEditor.putString("password", password).apply();
                    DeviceStatus.showSnackbar(rootView, "Login Successfully");

                    accountLogin();
                }


            }
        });





     /*   signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ContentValues values = new ContentValues();
                        values.put("username",username.getText().toString());
                        values.put("password",password.getText().toString());

                        String response = null;

                        try {
                            response = SimpleHttpClient.executeHttpPost("LoginServer/login.do", postParameters);
                            String res = response.toString();
                            resp = res.replaceAll("\\s+", "");

                        } catch (Exception e) {
                            e.printStackTrace();
                            errorMsg = e.getMessage();
                        }
                    }

                }).start();

                try {
                    Thread.sleep(1000);

                   // error.setText(resp);
                    if (null != errorMsg && !errorMsg.isEmpty()) {
                        //error.setText(errorMsg);
                    }
                } catch (Exception e) {
                   // error.setText(e.getMessage());
                }
            }
        });

*/
    }




    public void accountLogin() {
        Intent intent = new Intent(this, MainActivity.class );
        startActivity(intent);
    }



}
