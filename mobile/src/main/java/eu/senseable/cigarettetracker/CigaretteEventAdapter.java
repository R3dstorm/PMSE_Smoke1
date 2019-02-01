package eu.senseable.cigarettetracker;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import de.senseable.cloudsync.CigaretteEvent;
import de.senseable.cloudsync.Contract;

/**
 * Created by phil on 28.05.18.
 */

class CigaretteEventAdapter extends RecyclerView.Adapter<CigaretteEventAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private final ContentResolver mEvents;

    public CigaretteEventAdapter(Activity c) {
        mEvents = c.getContentResolver();
        mInflater = c.getLayoutInflater();

        Handler h = new Handler(c.getMainLooper());

        mEvents.registerContentObserver(Contract.EVENTSURI, true,
        new ContentObserver(h) {
            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.my_item_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cursor cur = mEvents.query(Contract.EVENTSURI,
                                  null, null, null, null);
        cur.moveToPosition(position);
        holder.setItem(CigaretteEvent.fromCursor(cur));
    }

    @Override
    public int getItemCount() {
        Cursor cur = mEvents.query(Contract.EVENTSURI,
                null, null, null, null);
        return cur.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CigaretteEvent mEvent;
        public ImageView mClockView;
        public TextView mTextView;
        public View mForegroundView,
                    mBackgroundView;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = itemView.findViewById(R.id.text_view);
            mClockView = itemView.findViewById(R.id.clock_view);
            mForegroundView = itemView.findViewById(R.id.view_foreground);
            mBackgroundView = itemView.findViewById(R.id.view_background);
        }

        public void setItem(CigaretteEvent ev) {
            mEvent = ev;

            mTextView.setText(ev.toString());

            AnimationSet animSet = new AnimationSet(true);
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setFillAfter(true);
            animSet.setFillEnabled(true);

            float secs = ev.getTimestamp().getMillisOfDay() * (360f / (24*60*60*1000));

            final RotateAnimation animRotate = new RotateAnimation(0.0f, secs,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            animRotate.setDuration(1500);
            animRotate.setFillAfter(true);
            animSet.addAnimation(animRotate);

            mClockView.startAnimation(animSet);
        }

        public CigaretteEvent getItem() {
            return mEvent;
        }
    }
}
