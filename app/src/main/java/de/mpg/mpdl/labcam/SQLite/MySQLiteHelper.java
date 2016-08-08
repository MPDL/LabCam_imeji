package de.mpg.mpdl.labcam.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kiran on 30.10.15.
 * SQLite Helper class for the database
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

        Context context;

        private static final int DATABASE_VERSION = 1;

        //new database with the name "FileDB" with the columns (filename+collectionID) and status
        private static final String DATABASE_NAME = "FileDB";

        // table name "file"
        private static final String TABLE_FILE = "file";

        //First Column name "filename"
        private static final String KEY_FILENAME = "filename";

        //Second Column name "status" (whether its uploaded or not)
        private static final String KEY_STATUS = "status";


    private static final String[] COLUMNS = {KEY_FILENAME, KEY_STATUS};

         public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
           this.context = context;
            // this.onCreate();
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create file table
        String CREATE_FILE_TABLE = "CREATE TABLE file ( " +
                "filename TEXT PRIMARY KEY, " +
                "status TEXT" + ")";

        // create file table
        db.execSQL(CREATE_FILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older file table if existed
        db.execSQL("DROP TABLE IF EXISTS books");

        // create fresh file table
        this.onCreate(db);
    }



    /*
        inserts the file row into the table
     */
    public void insertFile(FileId fileId) {
        //for logging
        Log.d("addFile", fileId.toString());

        // 1. get reference to writable DB
      // MySQLiteHelper dbHelper = new MySQLiteHelper(this.get);

       // MySQLiteHelper dbHelper = new MySQLiteHelper(context);
        SQLiteDatabase db = this.getWritableDatabase();
        //this.onCreate(db);

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_FILENAME, fileId.getFileName()); // get title
        values.put(KEY_STATUS, fileId.getStatus()); // get author

        // 3. insert
        db.insert(TABLE_FILE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    /*
        returns the row having the file in the database
     */
    public String getFileStatus(String fileName) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;

        //  try {
//           cursor =  db.query(TABLE_FILE, // a. table
//                           COLUMNS, // b. column names
//                           " filename = ?",// c. selections
//                                   new String[] {fileName}, // d. selections args
//                           null, // e. group by
//                           null, // f. having
//                           null, // g. order by
//                           null); // h. limit

        // cursor = db.rawQuery("select * from file where filename = ('" + fileName + "')",null);

        String Query = "Select * from " + TABLE_FILE + " where " + KEY_FILENAME + " = " + "'" + fileName + "'";
        String status = null;
                cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return "not present";
        }
        else {
            if(cursor != null && cursor.moveToFirst()) {
                // num = cursor.getString(cursor.getColumnIndex("ContactNumber"));

                status = cursor.getString(cursor.getColumnIndex(KEY_STATUS));
            }
            cursor.close();
            return status;
        }

    }


    public void updateFileStatus(String fileCollectionName, String status) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;

        String Query = "UPDATE " + TABLE_FILE + " SET " + KEY_STATUS + " = " + "'" + status + "'" + " where " + KEY_FILENAME + " = " + "'" + fileCollectionName + "'";

        db.execSQL(Query);

    }
    //   }

//       catch (SQLiteException e) {
//
//           if(e.getMessage().toString().contains("no such column")) {
//               return false;
//           }
//           return false;




       /* if (cursor != null)
            cursor.moveToFirst();
        else if(cursor == null) {
            return null;
        }*/

/*        FileId file = new FileId();
        file.setFileName((cursor.getString(0)));
        file.setStatus(cursor.getString(1));
        //book.setAuthor(cursor.getString(2));

        Log.d("getFile(" + fileName + ")", file.toString());

        return file;*/
       // return true;

  //  }

    /*
        list all the records of the database
     */
    public List<FileId> getAllFiles() {
        List<FileId> fileIds = new LinkedList<FileId>();

        String query = "SELECT  * FROM " + TABLE_FILE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        FileId file = null;

        if (cursor.moveToFirst()) {
            do {
                file = new FileId();
                file.setFileName((cursor.getString(0)));
                file.setStatus(cursor.getString(1));


                // Add file to files
                fileIds.add(file);
            } while (cursor.moveToNext());
        }

        //Log all the files which are present in the file table
        Log.d("getAllFiles()", fileIds.toString());

        return fileIds;
    }

}
