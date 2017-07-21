//package de.mpg.mpdl.labcam.code.common.fragment;
//
//import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//
//import com.activeandroid.ActiveAndroid;
//
//import de.mpg.mpdl.labcam.Model.CreatedBy;
//import de.mpg.mpdl.labcam.Model.DataItem;
//import de.mpg.mpdl.labcam.Model.ImejiFolder;
//import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
//import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
//import de.mpg.mpdl.labcam.R;
//import de.mpg.mpdl.labcam.code.base.BaseActivity;
//import de.mpg.mpdl.labcam.code.base.BaseMvpFragment;
//import de.mpg.mpdl.labcam.code.common.adapter.FolderListAdapter;
//import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
//import de.mpg.mpdl.labcam.code.injection.component.DaggerCollectionComponent;
//import de.mpg.mpdl.labcam.code.injection.module.CollectionMessageModule;
//import de.mpg.mpdl.labcam.code.injection.module.ItemMessageModule;
//import de.mpg.mpdl.labcam.code.mvp.presenter.ImejiPresenter;
//import de.mpg.mpdl.labcam.code.mvp.view.ImejiView;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.List;
//
//import butterknife.BindView;
//
//public class ImejiFragment extends BaseMvpFragment<ImejiPresenter> implements ImejiView {
//
//    @BindView(R.id.folder_cardview)
//    RecyclerView cardView;
//
//    private FolderListAdapter adapter;
//    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();
//    BaseActivity activity;
//    public ImejiFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    protected void injectComponent() {
//        DaggerCollectionComponent.builder()
//                .applicationComponent(getApplicationComponent())
//                .collectionMessageModule(new CollectionMessageModule())
//                .itemMessageModule(new ItemMessageModule())
//                .build()
//                .inject(this);
//        mPresenter.setView(this);
//    }
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.fragment_imeji;
//    }
//
//    @Override
//    protected void initContentView(Bundle savedInstanceState) {
//        activity = (BaseActivity) getActivity();
//        adapter = new FolderListAdapter(getActivity(), collectionListLocal);
//        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
//        llm.setOrientation(LinearLayoutManager.VERTICAL);
//        cardView.setLayoutManager(llm);
//
//        cardView.setAdapter(adapter);
//    }
//
//    @Override
//    public void getItemsSuc(ItemMessage itemMessage) {
//        List<DataItem> dataList = new ArrayList<>();
//        dataList = itemMessage.getResults();
//
//        /** store image **/
//        // date example 2015-02-16T13:02:27 +0100
//        for(DataItem dataItem:dataList ) {
//            String time = dataItem.getCreatedDate();
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
//            Date dt = null;
//            Long dtLong = null;
//            try {
//                dt = df.parse(time);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            if(dt!=null){
//                dtLong= dt.getTime();
//            }else {
//                dtLong = null;
//            }
//
//            dataItem.getFileUrl();
////                imageList.put(dtLong, dataItem.getFileUrl());
//        }
//
//
//        if(dataList != null) {
//            ActiveAndroid.beginTransaction();
//            try {
//                for (ImejiFolder folder : collectionListLocal) {
//                    // compare all the collectionID with the first dataItem's collectionId
//                    if(dataList.size()>0) {
//                        DataItem coverItem = dataList.get(0);
//                        //check for each folder, if the current items belongs to the current folder
//                        if(coverItem.getCollectionId().equals(folder.getImejiId())) {
////                                folder.setItems(dataList);
//                            folder.setCoverItemUrl(coverItem.getWebResolutionUrlUrl());
//                            folder.setImejiId(folder.getImejiId());
//                            folder.save();
//                        }
//                    }
//                }
//                ActiveAndroid.setTransactionSuccessful();
//            } finally {
//                ActiveAndroid.endTransaction();
//
//                adapter.notifyDataSetChanged();
//                adapter = new FolderListAdapter(getActivity(), collectionListLocal);
//                cardView.setAdapter(adapter);
//            }
//        }
//    }
//
//    @Override
//    public void getItemsFail(Throwable e) {
//
//    }
//
//    @Override
//    public void getCollectionsSuc(CollectionMessage collectionMessage) {
//        List<ImejiFolder> folderList = new ArrayList<>();
//        for (ImejiFolderModel imejiFolderModel : collectionMessage.getResults()) {
//            ImejiFolder imejiFolder = new ImejiFolder();
//            imejiFolder.setImejiId(imejiFolderModel.getId());  // parsed Id is ImejiId
//            imejiFolder.setContributors(imejiFolderModel.getContributors());
//            imejiFolder.setTitle(imejiFolderModel.getTitle());
//            imejiFolder.setDescription(imejiFolderModel.getDescription());
//            imejiFolder.setModifiedDate(imejiFolderModel.getModifiedDate());
//            imejiFolder.setCreatedDate(imejiFolderModel.getCreatedDate());
//
//            CreatedBy createdBy = new CreatedBy(imejiFolderModel.getCreatedBy().getFullname(),
//                    imejiFolderModel.getCreatedBy().getUserId());
//            imejiFolder.setCreatedBy(createdBy);
//            folderList.add(imejiFolder);
//        }
//
//        Collections.sort(folderList,new CustomComparator());
//
//        // clear imeji folder list
//        collectionListLocal.clear();
//        for (ImejiFolder folder : folderList) {
//            getFolderItems(folder.getImejiId());
//            collectionListLocal.add(folder);
//        }
//    }
//
//    @Override
//    public void getCollectionsFail(Throwable e) {
//
//    }
//
//    private void updateFolder(){
//        mPresenter.getCollectionMessage(activity);
//    }
//
//    private void getFolderItems(String collectionId){
//        mPresenter.getCollectionItems(collectionId, 10, 0, activity);
//    }
//
//
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            updateFolder();
//        }
//    }
//
//    public class CustomComparator implements Comparator<ImejiFolder> {
//        @Override
//        public int compare(ImejiFolder o1, ImejiFolder o2) {
//            return o2.getModifiedDate().compareTo(o1.getModifiedDate());
//        }
//    }
//
//}
