package de.senseable.cloudsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/** This is used to bind the Android Framework to the Authenticator.
 *
 * Created by phil on 29.06.18.
 */

public class AuthenticatorService extends Service {
    protected Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
