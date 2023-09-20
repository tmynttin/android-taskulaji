package com.tmynttin.taskulaji.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tmynttin.taskulaji.R;
import com.tmynttin.taskulaji.listeners.RecyclerViewClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private final String TAG = "NewsAdapter";

    private JSONArray localDataSet;
    private RecyclerViewClickListener rListener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final String TAG = "ViewHodler";
        private final TextView textView;
        RecyclerViewClickListener vrListener;

        public ViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(this);
            textView = view.findViewById(R.id.descriptionBodyView);
            vrListener = listener;
            Log.d(TAG, "Created a view holder");
        }

        public TextView getTextView() {
            return textView;
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: so far so good");
            vrListener.onClick(view, getAdapterPosition());
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public NewsAdapter(JSONArray dataSet, RecyclerViewClickListener listener) {
        localDataSet = dataSet;
        rListener = listener;
    }

    public void UpdateDataSet(JSONArray dataSet) {
        localDataSet = dataSet;
        this.notifyDataSetChanged();
    }

    public JSONArray getLocalDataSet() {
        return localDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.news_item, viewGroup, false);

        return new ViewHolder(view, rListener);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        String text = "";
        try {
            text = ((JSONObject) localDataSet.get(position)).getString("title");
        }
        catch (JSONException e){
            Log.d(TAG, "onBindViewHolder: " + e.toString());
        }
        viewHolder.getTextView().setText(text);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length();
    }
}

