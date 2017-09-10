package de.mpg.mpdl.labcam.code.common.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnTouch;
import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.Person;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseMvpFragment;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.CustomImageDownaloder;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.injection.component.DaggerCollectionComponent;
import de.mpg.mpdl.labcam.code.injection.module.CollectionMessageModule;
import de.mpg.mpdl.labcam.code.injection.module.ItemMessageModule;
import de.mpg.mpdl.labcam.code.mvp.presenter.ImejiPresenter;
import de.mpg.mpdl.labcam.code.mvp.view.ImejiView;
import de.mpg.mpdl.labcam.code.utils.FastImageLoader;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

public class CollectionViewFragment extends BaseMvpFragment<ImejiPresenter> implements ImejiView {

    private class PassUrls{
        private List<URL> urlList;
        private String imejiId;

        public void setUrlList(List<URL> urls){
            this.urlList = urls;
        }

        public List<URL> getUrlList(){
            return this.urlList;
        }

        public void setImejiId(String id){
            this.imejiId = id;
        }

        public String getImejiIdUrls(){
            return this.imejiId;
        }
    }
    BaseActivity activity;
    ScrollView globalView;
    private boolean syncComplete = false;
    private Map<String, String> headers = new HashMap<>();
    DisplayImageOptions options;

    @BindView(R.id.base_scroll_container)
    ScrollView rootView;

    @BindView(R.id.base_view)
    LinearLayout baseView;

    @OnTouch(R.id.base_scroll_container)
    public boolean incCollectionContainers(View view, MotionEvent event){
//        int scrollY = view.getScrollY();
//        int maxScroll = rootView.getHeight();
//        View childOne = rootView.findViewById(0+0);
//        int dpHeight = childOne.getHeight();
//        int children = baseView.getChildCount();
//        int maxItems = collectionListLocal.size();
//        if (scrollY > dpHeight){
//            scrollY = scrollY % dpHeight;
//        }
//        if (children < maxItems)
//            Log.v(LOG_TAG, "DATA: height = " + Integer.toString(maxScroll)+
//                    " scrolled = " + Integer.toString(scrollY)+
//                    " dpHeight = " + Integer.toString(dpHeight)+
//                    " children = " + Integer.toString(children));
//
//        if (scrollY >= dpHeight * 0.7) {
//            int itemsToAdd;
//            if (children < maxItems ){
//                itemsToAdd = maxItems - children;
//                if (itemsToAdd > 2) {
//                    itemsToAdd = 2;
//                }
//                for(int i = children +1 ; i <= itemsToAdd + children ; i++) {
//                    ImejiFolderModel folder = collectionListLocal.get(i-1);
//                    String createdDate = folder.getCreatedDate();
//                    String author = folder.getContributors().get(0).getFamilyName();
//                    String collectionInfo = "Created by: " + author + " on: " + createdDate;
//                    ArrayList<String> detailCollectionInfo = new ArrayList<>();
//                    ArrayList<String> contributors = new ArrayList<>();
//                    for (Person contributor : folder.getContributors()){
//                        String completeName = contributor.getCompleteName();
//                        contributors.add(completeName);
//                        detailCollectionInfo.add("contributor: " + completeName);
//                    }
//                    String description = folder.getDescription();
//                    detailCollectionInfo.add(description);
//                    AddCollectionContainer(i-1, folder.getTitle(), collectionInfo, detailCollectionInfo, folder.getId());
//                    List<URL> urlList = collectionListLocal.get(i-1).getImageUrls();
//                    if(urlList!=null) {
//                        for (int l = 0; l < urlList.size() && l < 5; l++) {
//                            Log.v(LOG_TAG, "Adding Picture at" + Integer.toString(l));
//                            AddPicture(i - 1, l, urlList.get(l));
//                        }
//                    }
//                }
//            }
//        }
        return false;
    }

    private final String LOG_TAG = this.getClass().getSimpleName();
    int smallTextViewSize = 5;
    public Float dens;

    private List<ImejiFolderModel> collectionListLocal = new ArrayList<>();


    Observer<PassUrls> urlListObserver = new Observer<PassUrls>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(PassUrls urlsObj) {
//            // NOTE how to keep the order?
//            List<URL> urls = urlsObj.getUrlList();
//            String id = urlsObj.getImejiIdUrls();
//
//            int idx = 0;
//            for (ImejiFolderModel collectionModel : collectionListLocal){
//                if (collectionModel.getId().equals(id)){
//                    collectionModel.setImageUrls(urls);
//                    for (int imageIndex = 0; imageIndex <= 3 && imageIndex < urls.size(); imageIndex++) {
//                        AddPicture(idx, imageIndex, urls.get(imageIndex));
//                        //NOTE add picture as it is incoming, then store the urls locally
//                    }
//                }
//                idx ++;
//            }
        }
    };

    public CollectionViewFragment(){};

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return super.onCreateView(inflater, container, savedInstanceState);
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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_layout_test;
    }


    @Override
    public void getCollectionsFail(Throwable e) {}

    @Override
    public void getCollectionsSuc(CollectionMessage collectionMessage) {
        for (ImejiFolderModel collectionModel : collectionMessage.getResults()) {
            collectionListLocal.add(collectionModel);
            mPresenter.getCollectionItems(collectionModel.getId(), 10, 0, activity);
        }
        addCollectionContainers();
        //Note: adding pictures after collections are done
    }

    @Override
    public void getItemsSuc(ItemMessage itemMessage) {
        //NOTE Items = Images - > ItemMessage are the collection images
        List<DataItem> imageItems = itemMessage.getResults();
        List<URL> urlBuffer = new ArrayList<>();
        String id = null;
        for (DataItem image : imageItems){
            id = image.getCollectionId();
            if(!image.getMimetype().contains("image")){ // only show image files
                continue;
            }
            String urlString = image.getFileUrl();
            if(null!=urlString) {
                Log.e(LOG_TAG, urlString);
            }else {
                Log.e(LOG_TAG, "null fileUrl");
            }
            try {
                URL currUrl = new URL(urlString);
                urlBuffer.add(currUrl);
            } catch (Exception e){
                Log.e(LOG_TAG, "URL malformed: " + urlString);
            }
        }
        PassUrls urlsObj = new PassUrls();
        urlsObj.setImejiId(id);
        urlsObj.setUrlList(urlBuffer);
        Observable<PassUrls> urlObservable = Observable.just(urlsObj);
        Subscription mySubscription = urlObservable.subscribe(urlListObserver);
    }

    @Override
    public void getItemsFail(Throwable e) {
        Log.e(LOG_TAG, e.toString());
    }

    private void downSynchCollections(){
        if (syncComplete == false)
            mPresenter.getCollectionMessage("", 10, 0, activity);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            dens = getContext().getResources().getDisplayMetrics().density;
            downSynchCollections();
        }
    }

    public int dpToPix(Integer pixels, Float dens) {return (int) (pixels * dens + 0.5f);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState){
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "saved Instancestsate");
        globalView = rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (globalView != null)
            rootView = globalView;
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v(LOG_TAG, "saved FragmentView");
        globalView = rootView;
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.v(LOG_TAG, "fragment stopped");
    }


    public void addCollectionContainers(){
        Collections.sort(collectionListLocal, new CustomComparator());
        for (int idx = 0; idx <= 2 && idx < collectionListLocal.size();  idx++) {
            ImejiFolderModel folder = collectionListLocal.get(idx);
            String createdDate = (folder.getCreatedDate());
            String author = folder.getContributors().get(0).getFamilyName();
            String collectionInfo = "Created by: " + author + " on: " + createdDate;
            ArrayList<String> detailCollectionInfo = new ArrayList<>();
            ArrayList<String> contributors = new ArrayList<>();
            for (Person contributor : folder.getContributors()) {
                String completeName = contributor.getCompleteName();
                contributors.add(completeName);
                detailCollectionInfo.add("contributor: " + completeName);
            }
            String description = folder.getDescription();
            detailCollectionInfo.add(description);
            AddCollectionContainer(idx, folder.getTitle(),
                    collectionInfo, detailCollectionInfo, folder.getId());

        }
    }

    /**
     * Appends a new CardView to the current view
     * @param position the position/index of the Collection
     * @param collectionName the name of the Collection
     * @param collectionInfo the Date when the collection was created
     */
    private void AddCollectionContainer(final Integer position, String collectionName,
                                        String collectionInfo,
                                        ArrayList<String> detailCollectionInfo,
                                        String collectionId) {

//        LinearLayout baseView = (LinearLayout) rootView.findViewById(R.id.base_view);
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//
//        CardView cardView = (CardView)
//                inflater.inflate(R.layout.collection_container_template, baseView, false);
//        final HorizontalScrollView horizontalScrollView = (HorizontalScrollView)
//                cardView.findViewById(R.id.horz_container);
//        TextView bigTextView = (TextView) cardView.findViewById(R.id.big_text_view);
//        bigTextView.setText(collectionName);
//
//        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getActivity(),
//                R.layout.collection_detail_list_layout,
//                detailCollectionInfo);
//        final ListView collectionDetailsView = (ListView) cardView.findViewById(R.id.mainListView);
//        collectionDetailsView.setAdapter(listAdapter);
//        final ToggleButton button = (ToggleButton) cardView.findViewById(R.id.collection_expand_button);
//        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    collectionDetailsView.setVisibility(View.VISIBLE);
//                } else {
//                    collectionDetailsView.setVisibility(View.GONE);
//                }
//            }
//        });
//
//        TextView smallTextView = (TextView) cardView.findViewById(R.id.small_text_view);
//        smallTextView.setText(collectionInfo);
//        cardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent showItemsIntent = new Intent(getActivity(), ItemsActivity.class);
//                showItemsIntent.putExtra(Intent.EXTRA_TEXT, collectionId);
//                getActivity().startActivity(showItemsIntent);
//            }
//        });
//
//        //NOTE this adds new pictures on horizontal scroll
//        horizontalScrollView.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                LinearLayout outerLinear = (LinearLayout) horizontalScrollView.getChildAt(0);
//                LinearLayout innerLinear = (LinearLayout) outerLinear.getChildAt(0);
//
//                int scrollX = view.getScrollX();
//                int maxScroll = horizontalScrollView.getChildAt(0).getWidth();
//                int dpWidth = horizontalScrollView.getWidth();
//                int children = innerLinear.getChildCount();
//
//                int maxItems = collectionListLocal.get(position).getImageUrls().size();
//                if (scrollX >= maxScroll - 2 * dpWidth ) {
//                    int itemsToAdd;
//                    if (children < maxItems ){
//                        itemsToAdd = maxItems - children;
//                        if (itemsToAdd > 2) {
//                            itemsToAdd = 2;
//                        }
//                        for(int i = children + 1; i <= children+itemsToAdd; i++) {
//                            AddPicture(position, i, collectionListLocal.get(position).getImageUrls().get(i-1));
//                        }
//                    }
//                }
//                return false;
//            }
//
//        });
//        cardView.setId(position);
//        baseView.addView(cardView);
    }

    private void AddPicture(final Integer collectionPosition, final Integer imagePosition,
                            final URL bitmapURL) {
//        final CardView cardView = (CardView) rootView.findViewById(collectionPosition);
//        final LinearLayout layout = (LinearLayout) cardView.findViewById(R.id.nested_linear);
//        final LinearLayout metaTextLayout = (LinearLayout) cardView.findViewById(R.id.meta_text_container);
//        cardView.post(new Runnable() {
//            @Override
//            public void run() {
//                Integer viewWidth = cardView.getWidth();
//                float imageWidthFloat = viewWidth * (0.9f);
//                int imageWidth = Math.round(imageWidthFloat);
//                Integer imageHeight;
//                if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE){
//                    imageHeight = Math.round(imageWidthFloat/4f );
//                }
//                else {
//                    imageHeight = Math.round(imageWidthFloat/2.39f );
//                }
//                int halfWidth = viewWidth / 2;
//                int initialOffset = halfWidth - (imageWidth / 2) - 50;
//                int followMargin = dpToPix(8, dens);
//
//                ImageView imageView = new ImageView(getContext());
//                imageView.setId(2000 + imagePosition);
//                String apiKey = PreferenceUtil.getString(getContext(), Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
//                getImageAsync(imageWidth, imageHeight, collectionPosition, imagePosition,
//                        bitmapURL, apiKey);
//
//
//                //Static - Do not change (Adjust in formatting section if needed)
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth + 10,
//                        imageHeight + 5);
//                if (imagePosition == 0) {
//                    params.setMargins(initialOffset, 0, 0, 0);
//                } else {
//                    params.setMargins(followMargin, 0, 0, 0);
//                }
//
//                imageView.setLayoutParams(params);
//
//                layout.addView(imageView);
//
//                TextView metaTextView = new TextView(getContext());
//                LinearLayout.LayoutParams metaTextViewParams = new LinearLayout.LayoutParams(
//                        imageWidth + 10 , ViewGroup.LayoutParams.WRAP_CONTENT);
//                if (imagePosition == 0) {
//                    metaTextViewParams.setMargins(initialOffset, 0, 0, 0);
//                } else {
//                    metaTextViewParams.setMargins(followMargin + 5, 0, 0, 0);
//                }
//                metaTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGrey));
//                metaTextView.setTextSize(dpToPix(smallTextViewSize, dens));
//                metaTextView.setLayoutParams(metaTextViewParams);
//                metaTextView.setText(collectionListLocal.get(collectionPosition).getDescription());
//                metaTextLayout.addView(metaTextView);
//            }
//        });
    }

    /**
     * Sets the image of a container of the collection at collectionPosition to the specified drawable
     * @param collectionPosition the index of the collection
     * @param imagePosition the index of the image container
     * @param roundedDrawable the drawable to set the image to
     */
    private void SetImage(final Integer collectionPosition, final Integer imagePosition,
                          RoundedBitmapDrawable roundedDrawable){

//        roundedDrawable.setCornerRadius(dpToPix(3, dens));
//        LinearLayout baseView = (LinearLayout) rootView.findViewById(R.id.base_view);
//        CardView cardView = (CardView) baseView.findViewById(collectionPosition);
//        ImageView imageView = (ImageView) cardView.findViewById(2000 + imagePosition);
//        imageView.setImageDrawable(roundedDrawable);
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //NOTE move DetailActivity form string bundle to url bundle
//                Intent showDetailIntent = new Intent(getActivity(), DetailActivity.class);
//                List<URL> urls = collectionListLocal.get(collectionPosition).getImageUrls();
//                ArrayList<String> urlStrings = new ArrayList<>();
//                for (URL url : urls)
//                    urlStrings.add(url.toString());
//                showDetailIntent.putStringArrayListExtra("itemPathList", urlStrings);
//                showDetailIntent.putExtra("positionInList",imagePosition);
//                getActivity().startActivity(showDetailIntent);
//            }
//        });
    }

    /**
     * Asynchronously load the image
     * onTaskDone calls SetImage
     * @see #SetImage(Integer, Integer, RoundedBitmapDrawable)
     * @param imageWidth the target width of the image
     * @param imageHeight the target height of the image
     * @param collectionPosition the index of the collection
     * @param imagePosition the index of the image
     * @param bitmapURL the url to the target bitmap
     * @param apiKey the apiKey for the interaction with the specified server
     *
     */

    private void getImageAsync(Integer imageWidth, Integer imageHeight,
                               final Integer collectionPosition, final Integer imagePosition,
                               URL bitmapURL, String apiKey) {
        Bundle bundle = new Bundle();
        bundle.putString("apiKey", apiKey);
        bundle.putString("url", bitmapURL.toString());
        bundle.putInt("width", imageWidth);
        bundle.putInt("height", imageHeight);
        FastImageLoader.AsyncImageLoader testAsyncTask = new FastImageLoader().
                new AsyncImageLoader(getContext(),(new FragmentCallback() {

            @Override
            public void onTaskDone(RoundedBitmapDrawable drawable) {
                SetImage(collectionPosition, imagePosition, drawable);
            }
        }));

        testAsyncTask.execute(bundle);
    }

    public interface FragmentCallback {
        void  onTaskDone(RoundedBitmapDrawable drawable);
    }

    public class CustomComparator implements Comparator<ImejiFolderModel> {
        @Override
        public int compare(ImejiFolderModel o1, ImejiFolderModel o2) {
            return o2.getModifiedDate().compareTo(o1.getModifiedDate());
        }
    }
}
