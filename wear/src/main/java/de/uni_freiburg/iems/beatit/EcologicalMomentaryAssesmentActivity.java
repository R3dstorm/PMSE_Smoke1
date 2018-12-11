package de.uni_freiburg.iems.beatit;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.MessageClient;


public class EcologicalMomentaryAssesmentActivity extends WearableActivity {

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private MessageClient mMessageClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecological_momentary_assesment);

        mTextView = (TextView) findViewById(R.id.text);

        final Button button = findViewById(R.id.pressButton);

        Intent intent = new Intent(this, EcologicalMomentaryAssesmentActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.preference_wrapped_icon, getString(R.string.hello_world), pendingIntent).build();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.hello_world))
                .setContentTitle(getString(R.string.hello_world))
                .extend(new NotificationCompat.WearableExtender().addAction(action))
                .build();

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(001, notification);

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
//                if(mGoogleApiClient == null)
//                    return;


            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }
}
