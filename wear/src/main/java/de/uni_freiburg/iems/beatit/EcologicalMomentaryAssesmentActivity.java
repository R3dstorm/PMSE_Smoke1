package de.uni_freiburg.iems.beatit;

import android.os.Bundle;
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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                if(mGoogleApiClient == null)
                    return;


            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }
}
