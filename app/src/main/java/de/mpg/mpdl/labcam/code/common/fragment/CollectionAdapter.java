package de.mpg.mpdl.labcam.code.common.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

import de.mpg.mpdl.labcam.Model.MessageModel.Person;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.activity.ItemsActivity;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;

/**
 * author : yingli
 * time   : 8/31/17
 * desc   :
 */

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>{

    private List<ImejiFolderModel> collectionList;
    private Context context;

    private int visibleThreshold = 3;
    private boolean isLoading;
    private RecyclerView mRecyclerView;
    private OnLoadMoreCollectionListener onLoadMoreCollectionListener;
    private CollectionItemAdapter.OnLoadMoreListener onLoadMoreItemListener;

    public CollectionAdapter(List<ImejiFolderModel> collectionList, RecyclerView recyclerView, CollectionItemAdapter.OnLoadMoreListener listener) {
        this.collectionList = collectionList;
        this.mRecyclerView = recyclerView;
        this.onLoadMoreItemListener = listener;
    }

    @Override
    public CollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.collection_container_template, parent, false);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!isLoading && dy>0){
                    int lastVisibleItem, totalItemCount;
                    LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (onLoadMoreCollectionListener != null) {
                            onLoadMoreCollectionListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            }
        });

        return new CollectionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CollectionViewHolder holder, int position) {
        ImejiFolderModel mCollection = collectionList.get(position);
        holder.titleTextView.setText(mCollection.getTitle());
        StringBuilder nameSb = new StringBuilder();
        nameSb.append("by: ");
        Person person = mCollection.getContributors().get(0);
        nameSb.append(person.getGivenName()!=null? person.getGivenName():"");
        nameSb.append(person.getFamilyName()!=null? person.getFamilyName():"");
        holder.authorTextView.setText(nameSb.toString());
        StringBuilder descriptionSb = new StringBuilder();
        descriptionSb.append("Creation date: ");
        descriptionSb.append(mCollection.getCreatedDate()+"\n");
        descriptionSb.append("Last modification date: ");
        descriptionSb.append(mCollection.getModifiedDate()+"\n");
        descriptionSb.append(mCollection.getDescription()!=null? mCollection.getDescription():"");
        holder.descriptionTextView.setText(descriptionSb.toString());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        holder.recyclerView.setLayoutManager(linearLayoutManager);
        CollectionItemAdapter adapter = new CollectionItemAdapter(mCollection.getImageUrls(), holder.recyclerView, position);
        adapter.setOnLoadMoreListener(onLoadMoreItemListener);
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return collectionList.size();
    }

    class CollectionViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView authorTextView;
        TextView descriptionTextView;
        RecyclerView recyclerView;
        ToggleButton toggleButton;
        TextView.OnClickListener titleClickListener;
        ToggleButton.OnCheckedChangeListener toggleChangeListener;


        public CollectionViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.big_text_view);
            authorTextView = (TextView) itemView.findViewById(R.id.small_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description_tv);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.item_rv);
            toggleButton = (ToggleButton) itemView.findViewById(R.id.collection_expand_button);

            titleClickListener = v -> {
                Intent showItemsIntent = new Intent(context, ItemsActivity.class);
                showItemsIntent.putExtra(Intent.EXTRA_TEXT, collectionList.get(getAdapterPosition()).getId());
                context.startActivity(showItemsIntent);
            };

            toggleChangeListener = (buttonView, isChecked) -> {
                if (isChecked) {
                    descriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    descriptionTextView.setVisibility(View.GONE);
                }
            };

            titleTextView.setOnClickListener(titleClickListener);
            toggleButton.setOnCheckedChangeListener(toggleChangeListener);
        }
    }

    public interface OnLoadMoreCollectionListener{
        void onLoadMore();
    }

    /**
     * Method set CollectionAdapter's OnLoadMoreListener
     * @param listener
     */
    public void setOnLoadMoreListener(OnLoadMoreCollectionListener listener){
        this.onLoadMoreCollectionListener = listener;
    }

    public void setLoaded(){
        isLoading = false;
    }
}
