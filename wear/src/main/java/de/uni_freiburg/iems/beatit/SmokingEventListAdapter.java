/* Credits: Major parts of sources from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package de.uni_freiburg.iems.beatit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SmokingEventListAdapter extends RecyclerView.Adapter<SmokingEventListAdapter.SmokingEventViewHolder> {

    class SmokingEventViewHolder extends RecyclerView.ViewHolder {
        private final TextView smokingEventItemView;

        private SmokingEventViewHolder(View itemView) {
            super(itemView);
            smokingEventItemView = itemView.findViewById(R.id.textView6);
        }
    }

    private final LayoutInflater mInflater;
    private List<SmokingEvent> mSmokingEvents; // Cached copy of smoking events

    SmokingEventListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public SmokingEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new SmokingEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SmokingEventViewHolder holder, int position) {
        if (mSmokingEvents != null) {
            SmokingEvent current = mSmokingEvents.get(position);
            /* Write display data to view */
            holder.smokingEventItemView.setText(current.getStartTime());
        } else {
            // Covers the case of data not being ready yet.
            holder.smokingEventItemView.setText("No Smoke Event");
        }
    }

    void setEvents(List<SmokingEvent> events){
        mSmokingEvents = events;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mSmokingEvents != null)
            return mSmokingEvents.size();
        else return 0;
    }
}