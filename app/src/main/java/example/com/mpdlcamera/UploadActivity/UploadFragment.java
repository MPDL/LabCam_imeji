package example.com.mpdlcamera.UploadActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.util.List;

import example.com.mpdlcamera.Model.LocalModel.LocalUser;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Settings.RemoteCollectionSettingsActivity;
import example.com.mpdlcamera.Utils.DeviceStatus;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends Activity {

    private String TAG = UploadFragment.class.getSimpleName();
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    static final int PICK_COLLECTION_REQUEST = 1997;
    private RadioGroup radioGroup;

    private Context context = this;

    private String email;
    private SharedPreferences mPrefs;


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
        mPrefs = this.getSharedPreferences("myPref", 0);
        email = mPrefs.getString("email", "");

        setContentView(R.layout.fragment_upload);
        //init radioButton group
        initRadioButtonGroup();

        //choose collection
        chooseCollection();
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView collectionNameTextView = (TextView) findViewById(R.id.collection_name);

        if(!DeviceStatus.is_newUser(email)){
            LocalUser user = new Select().from(LocalUser.class).where("email = ?", email).executeSingle();
        collectionNameTextView.setText(user.getCollectionName());
            List<LocalUser> userList = new Select().from(LocalUser.class).where("email = ?", email).execute();
            Log.i(TAG,userList.size()+"");
        }else {
            collectionNameTextView.setText("unset");
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
//        radioGroup.clearCheck();
    }

    public void chooseCollection(){
        TextView chooseCollectionTextView = (TextView) findViewById(R.id.tv_choose_collection);
        chooseCollectionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(context, RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
            }
        });
    }
}
