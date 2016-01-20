package example.com.mpdlcamera.UploadFragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Settings.RemoteCollectionSettingsActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    static final int PICK_COLLECTION_REQUEST = 1997;
    private RadioGroup radioGroup;
    private View rootview;

    // TODO: Rename and change types of parameters

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadFragment newInstance(String param1, String param2) {
        UploadFragment fragment = new UploadFragment();
        return fragment;
    }

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_upload, container, false);
        //init radioButton group
        initRadioButtonGroup();

        //choose collection
        chooseCollection();
        return rootview;
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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PICK_COLLECTION_REQUEST) {
//            // Make sure the request was successful
//            if (resultCode == Activity.RESULT_OK)
//                  data.getData();
//                Log.i("RESULT_OK",data.getData().toString());
////                TextView collectionNameTextView = (TextView) rootview.findViewById(R.id.collection_name);
////                collectionNameTextView.setText(data.getDataString());}
//            }
//
//    }

    public void initRadioButtonGroup(){
        radioGroup = (RadioGroup) rootview.findViewById(R.id.radio_group);
        radioGroup.clearCheck();
    }

    public void chooseCollection(){
        TextView chooseCollectionTextView = (TextView) rootview.findViewById(R.id.tv_choose_collection);
        chooseCollectionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(getActivity(), RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
            }
        });
    }
}
