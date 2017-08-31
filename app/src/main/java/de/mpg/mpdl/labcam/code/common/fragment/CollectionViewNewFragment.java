package de.mpg.mpdl.labcam.code.common.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import de.mpg.mpdl.labcam.Model.CreatedBy;
import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseMvpFragment;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.CustomImageDownaloder;
import de.mpg.mpdl.labcam.code.data.model.UserModel;
import de.mpg.mpdl.labcam.code.injection.component.DaggerCollectionComponent;
import de.mpg.mpdl.labcam.code.injection.module.CollectionMessageModule;
import de.mpg.mpdl.labcam.code.injection.module.ItemMessageModule;
import de.mpg.mpdl.labcam.code.mvp.presenter.ImejiPresenter;
import de.mpg.mpdl.labcam.code.mvp.view.ImejiView;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

/**
 * author : yingli
 * time   : 8/31/17
 * desc   : CollectionViewNewFragment display collections
 */

public class CollectionViewNewFragment extends BaseMvpFragment<ImejiPresenter> implements ImejiView {

    @BindView(R.id.collection_rv)
    RecyclerView mRecyclerView;

    BaseActivity activity;
    private Map<String, String> headers = new HashMap<>();
    DisplayImageOptions options;

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
        String apiKey = PreferenceUtil.getString(activity, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
        this.headers.put("Authorization","Bearer "+apiKey);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(activity)
                .imageDownloader(new CustomImageDownaloder(activity))
                .writeDebugLogs()
                .build();

        ImageLoader.getInstance().init(configuration);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnFail(R.drawable.error_alert)
                .build();

        //todo: mock a ItemMessage
        ItemMessage itemMessage = mockItemMessage();
        //todo: collection recycleview



        //todo: detail horizontal recycleview
    }

    @Override
    public void getItemsSuc(ItemMessage model) {

    }

    @Override
    public void getItemsFail(Throwable e) {

    }

    @Override
    public void getCollectionsSuc(CollectionMessage model) {

    }

    @Override
    public void getCollectionsFail(Throwable e) {

    }

    private ItemMessage mockItemMessage(){
        ItemMessage itemMessage = new ItemMessage();
        List<DataItem> dataItemList = new ArrayList<>();
        DataItem dataItem = new DataItem();
        dataItem.setCollectionId("nphXUBnqYj3T7rWG");
        dataItem.setFileUrl("http://qa-imeji.mpdl.mpg.de/imeji/file?itemId=THwGADFaFw2me9WG&resolution=original");
        dataItem.setFilename("WechatIMG72.jpeg");
        dataItem.setMimetype("image/jpeg");
        dataItem.setCreatedDate("2017-08-10T14:31:00 +0200");
        dataItem.setCreatedBy(new CreatedBy("Unknown User", "kr9ELkkh2irTXwh9"));

        dataItemList.add(dataItem);
        dataItemList.add(dataItem);
        itemMessage.setResults(dataItemList);
        return itemMessage;
    }
}
