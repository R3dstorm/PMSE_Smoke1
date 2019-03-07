/* Credits: Tutorial from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package eu.senseable.SQLiteDatabaseModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.senseable.cigarettetracker.R;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SmokingEventListAdapter extends RecyclerView.Adapter<SmokingEventListAdapter.SmokingEventViewHolder> {

    public class SmokingEventViewHolder extends RecyclerView.ViewHolder {
        public SmokingEvent mEvent;
        private final TextView smokingEventItemView;
        public View foregroundView, backgroundView;
        private ImageView mClockView;

        private SmokingEventViewHolder(View itemView) {
            super(itemView);
            smokingEventItemView = itemView.findViewById(R.id.text_view);
            foregroundView = itemView.findViewById(R.id.view_foreground);
            backgroundView = itemView.findViewById(R.id.view_background);
        }
        public SmokingEvent getItem() {
            return mEvent;
        }
        public void setItem(SmokingEvent event){
            mEvent = event;
            mClockView = itemView.findViewById(R.id.clock_view);
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
            holder.setItem(current);
            Date startDate = new Date();
            startDate = ConvertToDate(current.getStartDate());
            String startDateFormatted = ConvertDateToString(startDate);
            Date startTime = new Date();
            startTime = ConvertToTime(current.getStartTime());
            String startTimeFormatted = ConvertTimeToString(startTime);
            DateTime tmpTimeStamp = new DateTime(startTime.getTime());

            AnimationSet animSet = new AnimationSet(true);
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setFillAfter(true);
            animSet.setFillEnabled(true);

            float secs = tmpTimeStamp.getMillisOfDay()  * (360f / (24*60*60*1000));

            final RotateAnimation animRotate = new RotateAnimation(0.0f, secs,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            animRotate.setDuration(1500);
            animRotate.setFillAfter(true);
            animSet.addAnimation(animRotate);

            holder.mClockView.startAnimation(animSet);
            TimeUnit timeUnit = TimeUnit.MILLISECONDS;
            Date stopTime = new Date();
            stopTime = ConvertToTime(current.getStopTime());
            long tmpMilliDiff = stopTime.getTime() - startTime.getTime();
            long milliDiff = timeUnit.convert(tmpMilliDiff,MILLISECONDS);

            DateFormat durationFormatter = new SimpleDateFormat("mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliDiff);
            /* Write display data to view */
            holder.smokingEventItemView.setText(startDateFormatted + " " + startTimeFormatted + " " + durationFormatter.format(calendar.getTime()));
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
    @Override
    public int getItemCount() {
        if (mSmokingEvents != null)
            return mSmokingEvents.size();
        else return 0;
    }

    private Date ConvertToDate(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");

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