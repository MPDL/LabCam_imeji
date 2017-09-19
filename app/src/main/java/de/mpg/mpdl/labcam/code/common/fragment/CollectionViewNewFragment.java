package de.mpg.mpdl.labcam.code.common.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseMvpFragment;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.injection.component.DaggerCollectionComponent;
import de.mpg.mpdl.labcam.code.injection.module.CollectionMessageModule;
import de.mpg.mpdl.labcam.code.injection.module.ItemMessageModule;
import de.mpg.mpdl.labcam.code.mvp.presenter.ImejiPresenter;
import de.mpg.mpdl.labcam.code.mvp.view.ImejiView;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author : yingli
 * time   : 8/31/17
 * desc   : CollectionViewNewFragment display collections
 */

public class CollectionViewNewFragment extends BaseMvpFragment<ImejiPresenter> implements ImejiView {

    @BindView(R.id.collection_rv)
    RecyclerView mRecyclerView;

    BaseActivity activity;
    CollectionAdapter mCollectionAdapter;
    private boolean syncComplete = false;
    public Float dens;

    @Override
    protected void injectComponent() {
        DaggerCollectionComponent.builder()
                .applicationComponent(getApplicationComponent())
                .collectionMessageModule(new CollectionMessageModule())
                .itemMessageModule(new ItemMessageModule())
                .build()
                .inject(this);
        mPresenter.setView(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_collection;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        activity = (BaseActivity) getActivity();

        CollectionItemAdapter.OnLoadMoreListener listener = new CollectionItemAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int listNum, CollectionItemAdapter adapter) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int index = adapter.getDataset().size();
                        int end = index+3<=collectionCompleteList.get(listNum).getImageUrls().size()?
                                index+3 : collectionCompleteList.get(listNum).getImageUrls().size();

                        adapter.setDataSet(collectionCompleteList.get(listNum).getImageUrls().subList(0,end));
                        adapter.setLoaded();
                    }
                }, 2000);


            }
        };

        mCollectionAdapter = new CollectionAdapter(collectionList, mRecyclerView, listener);
        mCollectionAdapter.setOnLoadMoreListener(new CollectionAdapter.OnLoadMoreCollectionListener() {
            @Override
            public void onLoadMore() {
                int index = collectionList.size();
                mPresenter.getCollectionMessage("", 10, index, activity);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mRecyclerView.setAdapter(mCollectionAdapter);

    }

    private void downSynchCollections(){
        if (syncComplete == false) {
            collectionList.clear();
            mPresenter.getCollectionMessage("", 10, 0, activity);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            dens = getContext().getResources().getDisplayMetrics().density;
            downSynchCollections();
        }
    }

    @Override
    public void getItemsSuc(ItemMessage itemMessage) {
        List<DataItem> imageItems = itemMessage.getResults();
        List<String> urlList = new ArrayList<>();
        String id = null;
        for (DataItem imageItem : imageItems) {
            String orgUrl = imageItem.getFileUrl();
            String preUrl = new StringBuilder()
                    .append(orgUrl.substring(0, orgUrl.lastIndexOf("&")))
                    .append("&resolution=preview").toString();
            urlList.add(preUrl);
        }
        CollectionViewNewFragment.PassUrls urlsObj = new CollectionViewNewFragment.PassUrls();
        urlsObj.setImejiId(imageItems.get(0).getCollectionId());
        urlsObj.setUrlList(urlList);

        Observable<CollectionViewNewFragment.PassUrls> urlObservable = Observable.just(urlsObj)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Subscription mySubscription = urlObservable.subscribe(urlListObserver);

    }

    private class PassUrls{
        private List<String> urlList;
        private String imejiId;

        public List<String> getUrlList() {
            return urlList;
        }

        public void setUrlList(List<String> urlList) {
            this.urlList = urlList;
        }

        public String getImejiId() {
            return imejiId;
        }

        public void setImejiId(String imejiId) {
            this.imejiId = imejiId;
        }
    }

    Observer<CollectionViewNewFragment.PassUrls> urlListObserver = new Observer<CollectionViewNewFragment.PassUrls>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(CollectionViewNewFragment.PassUrls urlsObj) {
            //NOTE how to keep the order?
            List<String> urlList = urlsObj.getUrlList();
            String id = urlsObj.getImejiId();

            for (ImejiFolderModel collectionModel : collectionList){
                if (collectionModel.getId().equals(id)){
                    if(urlList.size()>=3) {
                        collectionModel.setImageUrls(urlList.subList(0, 3));
                    }else {
                        collectionModel.setImageUrls(urlList);
                    }
                }
            }

            for (ImejiFolderModel collectionModel : collectionCompleteList){
                if (collectionModel.getId().equals(id)){
                    collectionModel.setImageUrls(urlList);
                }
            }

            mCollectionAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void getItemsFail(Throwable e) {

    }

    private List<ImejiFolderModel> collectionList = new ArrayList<>();
    private List<ImejiFolderModel> collectionCompleteList = new ArrayList<>();
    @Override
    public void getCollectionsSuc(CollectionMessage collectionMessage) {
        for (ImejiFolderModel collectionModel : collectionMessage.getResults()) {
            collectionList.add(collectionModel);
            ImejiFolderModel model = new ImejiFolderModel();
            model.setId(collectionModel.getId());
            collectionCompleteList.add(model);
            mPresenter.getCollectionItems(collectionModel.getId(), 100, 0, activity);
        }
        mCollectionAdapter.notifyDataSetChanged();
        mCollectionAdapter.setLoaded();
    }

    @Override
    public void getCollectionsFail(Throwable e) {

    }

}
