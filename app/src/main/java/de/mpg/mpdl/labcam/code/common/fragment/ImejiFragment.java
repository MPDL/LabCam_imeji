package de.mpg.mpdl.labcam.code.common.fragment;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activeandroid.ActiveAndroid;

import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.code.common.adapter.FolderListAdapter;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ImejiFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private View rootView;
    private final String LOG_TAG = "ImejiFragment";
    private String APIKey;
    private FolderListAdapter adapter;
    private RecyclerView cardView;
    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();
    private OnFragmentInteractionListener mListener;
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
        APIKey = PreferenceUtil.getString(getActivity(), Constants.SHARED_PREFERENCES, Constants.API_KEY, "");

        adapter = new FolderListAdapter(getActivity(), collectionListLocal);
        cardView = (RecyclerView) rootView.findViewById(R.id.folder_cardview);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        cardView.setLayoutManager(llm);

        cardView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /** screen orientation **/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
         void onFragmentInteraction(Uri uri);
    }

    private void updateFolder(){
        RetrofitClient.getCollections(callback_collection, APIKey);
    }

    private void getFolderItems(String collectionId){
        RetrofitClient.getCollectionItems(collectionId,0, callback_Items, APIKey);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateFolder();
        }
    }

    /**
     * Callbacks
     */

    Callback<ItemMessage> callback_Items = new Callback<ItemMessage>() {
        @Override
        public void success(ItemMessage itemMessage, Response response) {
            List<DataItem> dataList = new ArrayList<>();
            dataList = itemMessage.getResults();

            /** store image **/
            // date example 2015-02-16T13:02:27 +0100
            for(DataItem dataItem:dataList ) {
                String time = dataItem.getCreatedDate();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
                Date dt = null;
                Long dtLong = null;
                try {
                    dt = df.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(dt!=null){
                    dtLong= dt.getTime();
                }else {
                    dtLong = null;
                }

                dataItem.getFileUrl();
//                imageList.put(dtLong, dataItem.getFileUrl());
            }


            if(dataList != null) {
                ActiveAndroid.beginTransaction();
                try {
                    for (ImejiFolder folder : collectionListLocal) {
                        // compare all the collectionID with the first dataItem's collectionId
                        if(dataList.size()>0) {
                            DataItem coverItem = dataList.get(0);
                            //check for each folder, if the current items belongs to the current folder
                            if(coverItem.getCollectionId().equals(folder.id)) {
//                                folder.setItems(dataList);
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
                    cardView.setAdapter(adapter);
                }
            }else{
                Log.e(LOG_TAG, "no items");

            }

            Log.v(LOG_TAG, "get DataItem success");

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get DataItem failed");
            Log.v(LOG_TAG, error.toString());
        }
    };

    Callback<CollectionMessage> callback_collection = new Callback<CollectionMessage>() {
        @Override
        public void success(CollectionMessage collectionMessage, Response response) {
            Log.i("callback_collection", "callback_collection success");

            List<ImejiFolder> folderList = new ArrayList<>();
            for (ImejiFolderModel imejiFolderModel : collectionMessage.getResults()) {
                ImejiFolder imejiFolder = new ImejiFolder();
                imejiFolder.setImejiId(imejiFolderModel.getId());
                imejiFolder.setContributors(imejiFolderModel.getContributors());
                imejiFolder.setTitle(imejiFolder.getTitle());
                imejiFolder.setDescription(imejiFolderModel.getDescription());
                imejiFolder.setCoverItemUrl(imejiFolder.getCoverItemUrl());
                imejiFolder.setModifiedDate(imejiFolder.getModifiedDate());
                imejiFolder.setCreatedDate(imejiFolder.getCreatedDate());
                folderList.add(imejiFolder);
            }

            Collections.sort(folderList,new CustomComparator());

            // clear imeji folder list
            collectionListLocal.clear();
            for (ImejiFolder folder : folderList) {
                Log.v(LOG_TAG, "collection title: " + String.valueOf(folder.getTitle()));
                getFolderItems(folder.id);
                collectionListLocal.add(folder);
                //folder.save();
            }

//            if(pDialog != null) {
//                pDialog.hide();
//            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.i("callback_collection","callback_collection fails");

        }
    };

    public class CustomComparator implements Comparator<ImejiFolder> {
        @Override
        public int compare(ImejiFolder o1, ImejiFolder o2) {
            return o2.getModifiedDate().compareTo(o1.getModifiedDate());
        }
    }

}
