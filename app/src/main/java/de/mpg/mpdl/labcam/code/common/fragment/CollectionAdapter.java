package de.mpg.mpdl.labcam.code.common.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    public CollectionAdapter(List<ImejiFolderModel> collectionList) {
        this.collectionList = collectionList;
    }

    @Override
    public CollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.collection_container_template, parent, false);
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
        List<String> pItemList = mCollection.getImageUrls()!=null && mCollection.getImageUrls().size()>3?
                mCollection.getImageUrls().subList(0,3) : mCollection.getImageUrls();
        CollectionItemAdapter adapter = new CollectionItemAdapter(pItemList, holder.recyclerView);
        List<String> pAllItemList = mCollection.getImageUrls();
        if(pItemList!=null) {
            adapter.setOnLoadMoreListener(new CollectionItemAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (pItemList == null) return;
                    adapter.notifyDataSetChanged();
                    adapter.setLoaded();
                }
            });
        }

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
        ToggleButton.OnCheckedChangeListener toggleChangeListerner;


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

            toggleChangeListerner = (buttonView, isChecked) -> {
                if (isChecked) {
                    descriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    descriptionTextView.setVisibility(View.GONE);
                }
            };

            titleTextView.setOnClickListener(titleClickListener);
            toggleButton.setOnCheckedChangeListener(toggleChangeListerner);
        }
    }
}
