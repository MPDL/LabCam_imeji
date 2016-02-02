package example.com.mpdlcamera.LocalFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import example.com.mpdlcamera.Gallery.ActivatedGalleryActivity;
import example.com.mpdlcamera.Gallery.GalleryListAdapter;
import example.com.mpdlcamera.Gallery.LocalImageActivity;
import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalFragment extends Fragment {


    //TODO: from kiran's galleryListActivity, remove lava later
    private View rootView;

    GridView gridView;
    SharedPreferences preferences;

    GalleryListAdapter adapter;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocalFragment newInstance(String param1, String param2) {
        LocalFragment fragment = new LocalFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LocalFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_local, container, false);
        gridView = (GridView) rootView.findViewById(R.id.gallery_gridView);
        // fill grid with LocalGallery
        loadLocalGallery();

        return rootView;

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

    /**
     * Local Gallery Grid
     * get folder name folder path
     * set adapter for grid
     * handle onclick
     */
    private void loadLocalGallery(){

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferences = getActivity().getSharedPreferences("folder", Context.MODE_PRIVATE);


        final ArrayList<Gallery> folders = new ArrayList<Gallery>();
        final ArrayList<String> folders1 = new ArrayList<String>();

        Cursor cur = getActivity().getContentResolver().query(images, albums, null, null, null);

        /*
            Listing out all the image folders(Galleries) in the file system.
         */
        if (cur.moveToFirst()) {

            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            do {
                Gallery album = new Gallery();
                album.setGalleryName(cur.getString(albumLocation));
                String currentAlbum = cur.getString(albumLocation);
                if(!folders1.contains(currentAlbum)) {
                    folders.add(album);
                    folders1.add(currentAlbum);
                }

                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }

        //try close cursor here
        cur.close();

        ArrayList<Gallery> imageFolders = new ArrayList<Gallery>();
        imageFolders = new ArrayList<Gallery>(new LinkedHashSet<Gallery>(folders));


        adapter = new GalleryListAdapter(getActivity(), imageFolders );

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Gallery gallery = (Gallery) adapter.getItem(position);

                String status = preferences.getString(gallery.getGalleryName(),"");

                //// FIXME: 2/2/16 why need this if else?
//                if(status.equalsIgnoreCase("On")) {
//
//                    /*
//                        Sending the galleryname and the gallerypath to the next activity(for the activated gallery)
//                     */
//                    Intent activatedGalleryIntent = new Intent(getActivity(), ActivatedGalleryActivity.class);
//                    activatedGalleryIntent.putExtra("galleryName", gallery.getGalleryName());
//                    activatedGalleryIntent.putExtra("galleryPath", gallery.getGalleryPath());
//
//                    startActivity(activatedGalleryIntent);
//
//                }
//                else {
                    /*
                        Sending the gallerytitle for the next activity(not activated gallery)
                     */
                
                
                
                    Intent galleryImagesIntent = new Intent(getActivity(), LocalImageActivity.class);
                    galleryImagesIntent.putExtra("galleryTitle", gallery.getGalleryPath());

                    startActivity(galleryImagesIntent);
                }
//            }
        });


    }

}
