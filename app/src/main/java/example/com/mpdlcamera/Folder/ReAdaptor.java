package example.com.mpdlcamera.Folder;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by allen on 06/10/15.
 */
public class ReAdaptor extends RecyclerView.Adapter<ReAdaptor.ViewHolder> {
    private String[] mDataset;
    private final String LOG_TAG = ReAdaptor.class.getSimpleName();


    // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
        public ReAdaptor(String[] myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ReAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
//            View v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.my_text_view, parent, false);
//            // set the view's size, margins, paddings and layout parameters
//
//            ViewHolder vh = new ViewHolder(v);
//            return vh;
            return null;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(mDataset[position]);

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }