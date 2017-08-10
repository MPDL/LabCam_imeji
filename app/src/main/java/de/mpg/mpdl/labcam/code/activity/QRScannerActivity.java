package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QRScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private Activity activity = this;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.e("Prints scan results", rawResult.getText()); // Prints scan results
        Log.e("Prints the scan format", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)


        Intent intent = activity.getIntent();
        intent.putExtra("QRText", rawResult.getText());
        activity.setResult(RESULT_OK, intent);
        finish();
    }

}
