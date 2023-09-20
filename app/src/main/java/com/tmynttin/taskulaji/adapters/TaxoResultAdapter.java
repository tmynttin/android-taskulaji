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

public class TaxoResultAdapter extends RecyclerView.Adapter<TaxoResultAdapter.ViewHolder>{
    private final String TAG = "TaxoResultAdapter";

    private JSONArray localDataSet;
    private RecyclerViewClickListener rListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final String TAG = "TaxoResultAdapter ViewH";
        private final TextView textView;
        private final TextView idView;
        RecyclerViewClickListener vrListener;

        public ViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(this);
            textView = view.findViewById(R.id.descriptionTitleView);
            idView = view.findViewById(R.id.taxoIdView);
            vrListener = listener;
            Log.d(TAG, "Created a view holder");
        }

        public TextView getTextView() {
            return textView;
        }

        public TextView getIdView() {return idView;}

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: so far so good");
            vrListener.onClick(view, getAdapterPosition());
        }
    }

    public TaxoResultAdapter(JSONArray dataSet, RecyclerViewClickListener listener) {
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
    public TaxoResultAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.taxo_result_item, viewGroup, false);

        return new TaxoResultAdapter.ViewHolder(view, rListener);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TaxoResultAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        String text = "";
        String id = "";
            try {
                text = localDataSet.getJSONObject(position).getString("value");
                id = localDataSet.getJSONObject(position).getString("key");
            } catch (JSONException e) {
                Log.d(TAG, "onBindViewHolder: ");
                e.printStackTrace();
            }

        viewHolder.getTextView().setText(text);
        viewHolder.getIdView().setText(id);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length();
    }
}
