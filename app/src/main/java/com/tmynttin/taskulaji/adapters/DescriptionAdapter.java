package com.tmynttin.taskulaji.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tmynttin.taskulaji.R;
import com.tmynttin.taskulaji.document.Unit;
import com.tmynttin.taskulaji.listeners.RecyclerViewClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT;

public class DescriptionAdapter extends RecyclerView.Adapter<DescriptionAdapter.ViewHolder>{
    private final String TAG = "DescriptionAdapter";

    private JSONArray localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final String TAG = "DescrAdapter ViewHolder";
        private final TextView titleView;
        private final TextView bodyView;

        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.descriptionTitleView);
            bodyView = view.findViewById(R.id.descriptionBodyView);

            Log.d(TAG, "Created a view holder");
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getBodyView() {return bodyView;}

    }

    public DescriptionAdapter(JSONArray dataSet) {
        localDataSet = dataSet;
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
    public DescriptionAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.description_item, viewGroup, false);

        return new DescriptionAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DescriptionAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        Log.d(TAG, "onBindViewHolder: " + localDataSet.toString() + "position: " + position);

        String title = "";
        String body = "";

        try {
            JSONObject description = localDataSet.getJSONObject(position);

            title = description.getString("title");
            body = description.getString("content");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        viewHolder.getTitleView().setText(title);
        viewHolder.getBodyView().setText(HtmlCompat.fromHtml(body, 0));
        viewHolder.getBodyView().setMovementMethod(LinkMovementMethod.getInstance());

        if (body.equals("")) {
            viewHolder.getTitleView().setVisibility(View.GONE);
            viewHolder.getBodyView().setVisibility(View.GONE);
        }
        else {
            viewHolder.getTitleView().setVisibility(View.VISIBLE);
            viewHolder.getBodyView().setVisibility(View.VISIBLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length();
    }
}
