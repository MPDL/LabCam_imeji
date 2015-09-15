package example.com.mpdlcamera;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

import example.com.mpdlcamera.Folder.MainActivity;

public class LoginActivity extends AppCompatActivity {

    ImageView animation;
    EditText username, password ;
    Button signIn;
    String errorMsg,resp ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        username = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        signIn = (Button) findViewById(R.id.btnSignIn);
        //error = (TextView) findViewById(R.id.tv_error);

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




    public void accountLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class );
        startActivity(intent);
    }



}
