package com.tmynttin.taskulaji.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tmynttin.taskulaji.R;
import com.tmynttin.taskulaji.document.Unit;
import com.tmynttin.taskulaji.listeners.RecyclerViewClickListener;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.ViewHolder>{
    private final String TAG = "UnitAdapter";

    private Unit[] localDataSet;
    private RecyclerViewClickListener rListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final String TAG = "UnitAdapter ViewHolder";
        private final TextView taxoView;
        private final TextView countView;

        RecyclerViewClickListener vrListener;

        public ViewHolder(View view, RecyclerViewClickListener listener) {
            super(view);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(this);
            taxoView = view.findViewById(R.id.descriptionTitleView);
            countView = view.findViewById(R.id.descriptionBodyView);

            vrListener = listener;
            Log.d(TAG, "Created a view holder");
        }

        public TextView getTaxoView() {
            return taxoView;
        }

        public TextView getCountView() {return countView;}

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: so far so good");
            vrListener.onClick(view, getAdapterPosition());
        }
    }

    public UnitAdapter(Unit[] dataSet, RecyclerViewClickListener listener) {
        localDataSet = dataSet;
        rListener = listener;
    }

    public void UpdateDataSet(Unit[] dataSet) {
        localDataSet = dataSet;
        this.notifyDataSetChanged();
    }

    public Unit[] getLocalDataSet() {
        return localDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UnitAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.unit_item, viewGroup, false);

        return new UnitAdapter.ViewHolder(view, rListener);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(UnitAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        Log.d(TAG, "onBindViewHolder: " + localDataSet.toString() + "position: " + position);
        Unit unit = localDataSet[position];

        String taxon = "";
        String count = "";

        if (unit.identifications != null) {
            taxon = unit.identifications[0].taxon;
            count = unit.count;
        }

        viewHolder.getTaxoView().setText(taxon);
        viewHolder.getCountView().setText(count);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }
}
