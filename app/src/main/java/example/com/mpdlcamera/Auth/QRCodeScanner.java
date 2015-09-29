package example.com.mpdlcamera.Auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import example.com.mpdlcamera.R;

/**
 * Created by allen on 29/09/15.
 */
public class QRCodeScanner extends Activity implements QRCodeReaderView.OnQRCodeReadListener {

    private TextView myTextView;
    private QRCodeReaderView decoderview;
    private ImageView line_image;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        decoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        decoderview.setOnQRCodeReadListener(this);

        myTextView = (TextView) findViewById(R.id.exampleTextView);

        line_image = (ImageView) findViewById(R.id.red_line_image);

        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.5f);
        mAnimation.setDuration(1000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        line_image.setAnimation(mAnimation);

    }


    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        myTextView.setText(text);

        Intent intent = activity.getIntent();
        intent.putExtra("QRText", text);
        activity.setResult(RESULT_OK, intent);
        finish();
    }


    // Called when your device have no camera
    @Override
    public void cameraNotFound() {

    }

    // Called when there's no QR codes in the camera preview image
    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        decoderview.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        decoderview.getCameraManager().stopPreview();
    }
}