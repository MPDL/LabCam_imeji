package de.mpg.mpdl.labcam.code.common.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

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
        holder.authorTextView.setText(mCollection.getCreatedBy().getFullname() + mCollection.getCreatedDate());

        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(new CollectionItemAdapter(mCollection.getImageUrls()));
    }

    @Override
    public int getItemCount() {
        return collectionList.size();
    }

    class CollectionViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView authorTextView;
        RecyclerView recyclerView;
        ToggleButton toggleButton;
        TextView.OnClickListener titleClickListener;
        ToggleButton.OnCheckedChangeListener toggleChangeListerner;


        public CollectionViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.big_text_view);
            authorTextView = (TextView) itemView.findViewById(R.id.small_text_view);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.item_rv);
            toggleButton = (ToggleButton) itemView.findViewById(R.id.collection_expand_button);

            titleClickListener = v -> {
                Intent showItemsIntent = new Intent(context, ItemsActivity.class);
                showItemsIntent.putExtra(Intent.EXTRA_TEXT, collectionList.get(getAdapterPosition()).getId());
                context.startActivity(showItemsIntent);
            };

            toggleChangeListerner = (buttonView, isChecked) -> {
                if (isChecked) {
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            };

            titleTextView.setOnClickListener(titleClickListener);
            toggleButton.setOnCheckedChangeListener(toggleChangeListerner);
        }
    }
}
