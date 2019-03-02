/* Credits: Tutorial from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package SQLiteDatabaseModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import de.uni_freiburg.iems.beatit.R;

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

    public SmokingEventListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public SmokingEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new SmokingEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SmokingEventViewHolder holder, int position) {
        if (mSmokingEvents != null && getItemCount() > 0) {
            /* Write display data to view */
            if(position < 30) {
                holder.smokingEventItemView.setText(formatEvent(mSmokingEvents.get(getItemCount() - position - 1)));
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.smokingEventItemView.setText("No Smoke Event");
        }
    }

    private String formatEvent(SmokingEvent event) {
        try {
            String confirmed = (event.getEventConfirmed() ? "\u2713" : "\u2717");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd HHmmss");
            LocalDateTime start = LocalDateTime.parse(event.getStartDate() + " " + event.getStartTime(), formatter);
            LocalDateTime stop = LocalDateTime.parse(event.getStopDate() + " " + event.getStopTime(), formatter);
            Duration duration = Duration.between(start, stop);
            String durationMinutes = String.format("%02d", duration.getSeconds() / 60);
            String durationSeconds = String.format("%02d", duration.getSeconds() % 60);
            String dayName = start.getDayOfWeek().toString().substring(0, 3);
            String day = String.format("%02d", start.getDayOfMonth());
            String monthName = start.getMonth().toString().substring(0, 3);
            String hours = String.format("%02d", start.getHour());
            String minutes = String.format("%02d", start.getMinute());
            String seconds = String.format("%02d", start.getSecond());

            return dayName + ", " + day + ". " + monthName + " " + hours + ":" + minutes + ":" + seconds +
                   ", " + durationMinutes + ":" + durationSeconds + " " + confirmed;
        } catch(Exception e) {
            Log.i("ML", "failed to format event: " + e.getMessage());
        }
        return "";
    }

    public void setEvents(List<SmokingEvent> events){
        mSmokingEvents = events;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    @Override
    public int getItemCount() {
        if (mSmokingEvents != null)
            return mSmokingEvents.size();
        else return 0;
    }
}