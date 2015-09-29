package example.com.mpdlcamera;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import java.util.concurrent.ExecutorService;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.DataItem;

/**
 * Created by kiran on 29.09.15.
 */
public class NewFileObserver extends ContentObserver {




    private Context context;

    FileUploader fileUploader = new FileUploader();

    //private FileUploader application;

    private ExecutorService queue;

    public NewFileObserver(Handler handler,MainActivity application) {
        super(handler);
        this.context = application.getBaseContext();
    }


    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public NewFileObserver(Handler handler,  ExecutorService queue) {
        super(handler);

      //  this.application = application;
        this.queue = queue;
    }

    @Override
    public void onChange(boolean selfChange) {
        //super.onChange(selfChange);

        LatestImage imageLatest = new LatestImage(context);
        int imageId = imageLatest.getId();

        if (imageId == -1) {
            return;
        }

        DataItem item = imageLatest.getLatestItem();

        if(item == null) {
            return;
        }

        else {

            FileUploader fileUploader = new FileUploader(context);
            fileUploader.upload(item);



        }


    }






}
