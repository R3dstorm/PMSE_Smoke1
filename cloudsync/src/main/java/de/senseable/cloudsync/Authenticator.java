package de.senseable.cloudsync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.UUID;

/** This is just a stub.
 *
 * Created by phil on 29.06.18.
 */

public class Authenticator extends AbstractAccountAuthenticator {
    private String mInstallationID;

    public Authenticator(Context context) {
        super(context);

        SharedPreferences sharedPref = context.getSharedPreferences(
        context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        mInstallationID = sharedPref.getString("IID", null);

        if (mInstallationID == null) {
            mInstallationID = UUID.randomUUID().toString();

            sharedPref
                    .edit()
                    .putString("IID", mInstallationID)
                    .commit();
        }
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        // XXX this is just a hack
        Bundle b = new Bundle();
        b.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        b.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        b.putString(AccountManager.KEY_AUTHTOKEN, mInstallationID);
        return b;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
