package de.senseable.cloudsync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by phil on 29.06.18.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();
    private static final String BASEURL = "xxx";
    private final ContentResolver mContentResolver;
    private RequestQueue mQ = null;
    private String mUid = null;

    public SyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle bundle,
                              String authority,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {
        /*
         * Local-First synchronization
         *
         * Step 1: get all entries on the server
         * Step 2: case-by-case:
         *  if server and!local: delete on server
         *  if!server and local: upload to server
         */
        Log.d(TAG, "running synchronization");

        try {
            Context c = getContext();
            AccountManager mgr = (AccountManager) c.getSystemService(c.ACCOUNT_SERVICE);
            mUid = mgr.blockingGetAuthToken(account, "IID", true);

            mQ = Volley.newRequestQueue(getContext());
            mQ.add(query());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Comparator<? super CigaretteEvent> mCigEventIdAscending = new Comparator<CigaretteEvent>() {
        @Override
        public int compare(CigaretteEvent a, CigaretteEvent b) {
            return Long.compare(a.mID, b.mID);
        }
    };

    private Response.Listener<String> mOnEventsLists = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            /* parse the list of events from the response */
            String[] lines = response.split(System.getProperty("line.separator"));
            HashMap<Long, CigaretteEvent> server = new HashMap<>();

            // skip first empty line
            for (int pos=0; pos < lines.length; pos++) {
                if (lines[pos].trim().length() == 0)
                    continue;
                CigaretteEvent ev = CigaretteEvent.fromCSV(lines[pos]);
                server.put(ev.mID, ev);
            }

            /* get the current local data */
            Cursor c = getContext().getContentResolver().query(
                    Contract.EVENTSURI, null, null, null, null);
            HashMap<Long, CigaretteEvent> local = new HashMap<>();

            for (int pos=0; pos < c.getCount(); pos++) {
                c.moveToPosition(pos);
                CigaretteEvent ev = CigaretteEvent.fromCursor(c);
                local.put(ev.mID, ev);
            }

            /* delete all events from server, which are in the server set but not in the local set */
            HashSet<Long> toremove = new HashSet<>(server.keySet()),
                          toupload = new HashSet<>(local.keySet());

            toremove.removeAll(local.keySet());
            toupload.removeAll(server.keySet());

            for (Long id : toremove)
                mQ.add(delete(server.get(id)));

            for (Long id : toupload)
                mQ.add(upload(local.get(id)));

        }
    };

    private Response.ErrorListener mOnError= new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
        }
    };

    @Override
    public void onSyncCanceled() {
        mQ.cancelAll(TAG);
        super.onSyncCanceled();
    }

    private StringRequest upload(CigaretteEvent ev) {
        StringBuilder url = basequery("insert");
        url.append("&id=");
        url.append(ev.mID);
        url.append("&timestamp=");
        try {
            url.append(URLEncoder.encode(ev.toIsoString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return request(url, null);
    }

    private StringRequest delete(CigaretteEvent ev) {
        StringBuilder url = basequery("delete");
        url.append("&id=");
        url.append(ev.mID);
        return request(url, null);
    }

    private StringRequest query() {
        StringBuilder url = basequery("query");
        return request(url, mOnEventsLists);
    }

    private StringRequest request(StringBuilder url, Response.Listener<String> onSuccess) {
        StringRequest sq = new StringRequest(Request.Method.GET,
                url.toString(), onSuccess, mOnError);
        sq.setTag(TAG);
        return sq;
    }

    /** create an http request url for the specified query command */
    private StringBuilder basequery(String query) {
        StringBuilder sb = new StringBuilder();

        sb.append(BASEURL); sb.append("/");
        sb.append(Contract.EVENTS);

        if (query != null) {
            sb.append("/");
            sb.append(query);
        }

        sb.append("?uid="); sb.append(mUid);

        return sb;
    }

}
