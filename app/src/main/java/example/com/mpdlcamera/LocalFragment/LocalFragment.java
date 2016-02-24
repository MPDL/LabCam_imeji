package example.com.mpdlcamera.LocalFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import example.com.mpdlcamera.Gallery.ActivatedGalleryActivity;
import example.com.mpdlcamera.Gallery.GalleryListAdapter;
import example.com.mpdlcamera.Gallery.LocalImageActivity;
import example.com.mpdlcamera.Gallery.SectionedGridView.SectionedGridRecyclerViewAdapter;
import example.com.mpdlcamera.Gallery.SectionedGridView.SimpleAdapter;
import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.Model.LocalModel.Image;
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
    RecyclerView recyclerView;
    SharedPreferences preferences;

    GalleryListAdapter adapter;

    //adapter data
    final ArrayList<Gallery> folders = new ArrayList<Gallery>();
    final ArrayList<String> folders1 = new ArrayList<String>();

    TreeMap<Long, String> imageList =
            new TreeMap<Long, String>();

    TreeMap<String,String> imageTree =
            new TreeMap<String,String>();

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
        // folder gridView
        gridView = (GridView) rootView.findViewById(R.id.gallery_gridView);

        // timeline recycleView
        recyclerView = (RecyclerView) rootView.findViewById(R.id.gallery_recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        //prepare local gallery image data
        prepareData();

        //set grid adapter
        loadLocalGallery();

        //set header recycleView adapter
        loadTimeLinePicture();





        //switch
        Switch modeSwitch = (Switch) rootView.findViewById(R.id.switch_mode);
        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!compoundButton.isChecked()){
                    gridView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }else {
                    gridView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

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


    private void prepareData(){
        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                ,MediaStore.Images.Media.DATA
                , MediaStore.Images.Media.DATE_TAKEN

        };
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferences = getActivity().getSharedPreferences("folder", Context.MODE_PRIVATE);

        Cursor cur = getActivity().getContentResolver().query(images, albums,null,null,null);


        /*
            Listing out all the image folders(Galleries) in the file system.
         */
        if (cur.moveToFirst()) {

            int albumLocation = cur.getColumnIndex(MediaStore.Images.
                    Media.BUCKET_DISPLAY_NAME);
            int nameColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            int dateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);

            do {
                //get all image and date


                Long imageDate =  cur.getLong(dateColumn);
//                Date d = new Date(imageDate);
                String imagePath = cur.getString(nameColumn);
                imageList.put(imageDate,imagePath);

                //gallery view
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
    }

    //load timeLinePicture
    private void loadTimeLinePicture(){
        // Your RecyclerView.Adapter
        // sortedImageNameList is imagePath list
        List<String> sortedImageNameList = new ArrayList<>();
        // sectionMap key is the date, value is the picture number
        List<String> dateAseList = new ArrayList<String>();
        List<String> dateList = new ArrayList<String>();
        HashMap<String,Integer> sectionMap = new HashMap<String,Integer>();

        for(Map.Entry<Long,String> entry: imageList.entrySet()){
            Date d = new Date(entry.getKey());
            SimpleDateFormat dt = new SimpleDateFormat(" dd.MM.yyyy");
            String dateStr = dt.format(d);
            dateAseList.add(dateStr);
            sortedImageNameList.add(entry.getValue());
        }


        for(int i=dateAseList.size()-1;i>=0;i--){
                dateList.add(dateAseList.get(i));
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(),sortedImageNameList);

        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();


        // section init
        String preStr = "";
        int count = 0;
//        int imgPosition = 0;
        for(String dateStr:dateList){
            // count is img position
            if(preStr.equalsIgnoreCase(dateStr)){
                count++;
            }else {
             //init
                sections.add(new SectionedGridRecyclerViewAdapter.Section(count,dateStr));
                count++;
            }
            preStr = dateStr;
        }


        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        SectionedGridRecyclerViewAdapter mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(getActivity(),R.layout.header_grid_section,R.id.section_text,recyclerView,simpleAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);
    }
}
