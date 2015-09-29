package example.com.mpdlcamera.Utils;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;

/**
 * Created by kiran on 22.09.15.
 */
public class BackupFileHelper extends BackupAgentHelper {


    static final String fileName = "myFiles";
    static final String PFILES_BACKUP_KEY = "backup";


    @Override
    public void onCreate() {
        FileBackupHelper helper = new FileBackupHelper(this, fileName);
        addHelper(PFILES_BACKUP_KEY, helper);
    }
}
