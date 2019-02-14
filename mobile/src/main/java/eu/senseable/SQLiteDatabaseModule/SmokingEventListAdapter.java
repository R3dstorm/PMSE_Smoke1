/* Credits: Major parts of sources from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package eu.senseable.SQLiteDatabaseModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import eu.senseable.cigarettetracker.R;

public class SmokingEventListAdapter extends RecyclerView.Adapter<SmokingEventListAdapter.SmokingEventViewHolder> {


    public class SmokingEventViewHolder extends RecyclerView.ViewHolder {
        private final TextView smokingEventItemView;

        private SmokingEventViewHolder(View itemView) {
            super(itemView);
            smokingEventItemView = itemView.findViewById(R.id.text_view);
        }
    }

    private final LayoutInflater mInflater;
    private List<SmokingEvent> mSmokingEvents; // Cached copy of smoking events

    public SmokingEventListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public SmokingEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.my_item_view, parent, false);
        return new SmokingEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SmokingEventViewHolder holder, int position) {
        if (mSmokingEvents != null) {
            SmokingEvent current = mSmokingEvents.get(position);
            Date startDate = new Date();
            startDate = ConvertToDate(current.getStartDate());
            String startDateFormatted = ConvertDateToString(startDate);
            Date startTime = new Date();
            startTime = ConvertToTime(current.getStartTime());
            String startTimeFormatted = ConvertTimeToString(startTime);
            /* Write display data to view */
            holder.smokingEventItemView.setText(startDateFormatted + " " + startTimeFormatted);
        } else {
            // Covers the case of data not being ready yet.
            holder.smokingEventItemView.setText("No Smoke Event");
        }
    }

    public void setEvents(List<SmokingEvent> events){
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

    private Date ConvertToDate(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");

        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return convertedDate;
    }

    private Date ConvertToTime(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");

        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return convertedDate;
    }

    private String ConvertDateToString(Date dateBefore) {
        SimpleDateFormat formattedDateFormat = new SimpleDateFormat("E dd MMM");
        String formattedDate = "";

        formattedDate = formattedDateFormat.format(dateBefore);
        return formattedDate;
    }

    private String ConvertTimeToString(Date dateBefore) {
        SimpleDateFormat formattedDateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = "";

        formattedDate = formattedDateFormat.format(dateBefore);
        return formattedDate;
    }
}