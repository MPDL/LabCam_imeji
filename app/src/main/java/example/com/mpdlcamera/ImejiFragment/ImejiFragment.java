package example.com.mpdlcamera.ImejiFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Folder.FolderListAdapter;
import example.com.mpdlcamera.Items.ItemsActivity;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImejiFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ImejiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImejiFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private View rootView;
    private final String LOG_TAG = "ImejiFragment";
    private String username;
    private String password;
    private SharedPreferences mPrefs;

    private ProgressDialog pDialog;

    //private FolderGridAdapter adapter;
    //private GridView gridview;

    private NavigationView navigation;

    private FolderListAdapter adapter;
    private ListView listView;

    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();
    private ImejiFolder currentCollectionLocal = new ImejiFolder();

    SharedPreferences preferencesFiles;

    //TESTING DB
    private boolean isAdd = false;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImejiFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImejiFragment newInstance(String param1, String param2) {
        ImejiFragment fragment = new ImejiFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ImejiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_imeji, container, false);


        loadImejiFolder();
        adapter = new FolderListAdapter(getActivity(), collectionListLocal);
        listView = (ListView) rootView.findViewById(R.id.folder_listView);
        listView.setAdapter(adapter);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ImejiFolder folder = (ImejiFolder) adapter.getItem(position);

                Intent showItemsIntent = new Intent(getActivity(), ItemsActivity.class);
                showItemsIntent.putExtra(Intent.EXTRA_TEXT, folder.id);
                showItemsIntent.putExtra("folderTitle", folder.getTitle());
                startActivity(showItemsIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFolder();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    //
    private void loadImejiFolder(){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


        if(sharedPreferences.getString("status","").isEmpty()) {
            SharedPreferences.Editor editorS = sharedPreferences.edit();
            editorS.putString("status","wifi");
            editorS.commit();

        }
        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putString("UploadStatus", "false");
        //editor.putString("status", "wifi");
        editor.commit();


        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.DATA};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferencesFiles = getActivity().getSharedPreferences("gallery", Context.MODE_PRIVATE);

        Cursor cur = getActivity().getContentResolver().query(images, albums, null, null, null);

        // final ArrayList<String> folders = new ArrayList<String>();

        /*
            set the folder path and folder names in shared preferences
         */
        if(cur != null) {
            if (cur.moveToFirst()) {
                String album;
                String folderPath;
                int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int path = cur.getColumnIndex(MediaStore.Images.Media.DATA);

                do {
                    // here store filename and filepath
                    album = cur.getString(albumLocation);
                    folderPath = cur.getString(path);
                    File file = new File(folderPath);
                    String dir = file.getParent();
                    SharedPreferences.Editor ed = preferencesFiles.edit();
                    ed.putString(album, dir);
                    ed.commit();
                    // folders.add(album);
                    Log.i("ListingImages", " album=" + album);
                } while (cur.moveToNext());
            }
        }

        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");


        collectionListLocal = new Select()
                .from(ImejiFolder.class)
                .execute();
        Log.v(LOG_TAG, "size: " + collectionListLocal.size() + "");
    }

    private void updateFolder(){
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Loading...");
        pDialog.show();
        RetrofitClient.getCollections(callback, username, password);
    }

    private void getFolderItems(String collectionId){
        RetrofitClient.getCollectionItems(collectionId, callbackItems, username, password);
    }

    /**
     * Callbacks
     */
    Callback<List<ImejiFolder>> callback = new Callback<List<ImejiFolder>>() {
        @Override
        public void success(List<ImejiFolder> folderList, Response response) {
            ActiveAndroid.beginTransaction();
            try {
                collectionListLocal.clear();
                for(ImejiFolder folder : folderList){
                    Log.v(LOG_TAG, "collection title: " + String.valueOf(folder.getTitle()));
                    Log.v(LOG_TAG, "collection id: " + String.valueOf(folder.id));

                    getFolderItems(folder.id);

                    //TODO Here is a bug, collectionLocal will be random one collection
                    //collectionLocal = folder;

                    collectionListLocal.add(folder);
                    //folder.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally{
                ActiveAndroid.endTransaction();
            }

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
            DeviceStatus.showSnackbar(rootView, "update data failed");
        }
    };

    Callback<List<DataItem>> callbackItems = new Callback<List<DataItem>>() {
        @Override
        public void success(List<DataItem> dataList , Response response) {

            if(dataList != null) {
                ActiveAndroid.beginTransaction();
                try {
                    for (ImejiFolder folder : collectionListLocal) {

                        if(dataList.size()>0) {
                            DataItem coverItem = dataList.get(0);
                            //check for each folder, if the current items belongs to the current folder
                            if(coverItem.getCollectionId().equals(folder.id)){
                                folder.setItems(dataList);

                                folder.setCoverItemUrl(coverItem.getWebResolutionUrlUrl());
                                folder.setImejiId(folder.id);
                                folder.save();

                            }
                        }
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();

                    adapter.notifyDataSetChanged();
                    adapter = new FolderListAdapter(getActivity(), collectionListLocal);
                    listView.setAdapter(adapter);


                    if(pDialog != null) {
                        pDialog.hide();
                    }
                }
            }else{
                DeviceStatus.showToast(getActivity(), "no items");
                Log.v(LOG_TAG, "no items");

            }

            Log.v(LOG_TAG, "get list OK");

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
            //  DeviceStatus.showToast(activity, "update data failed");
        }
    };
}
