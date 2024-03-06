package com.blk.sdk.com;

import android.os.Looper;
import android.util.Log;

import com.blk.sdk.activity.BaseActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;


public class HttpClient {

    public SyncHttpClient client = new SyncHttpClient();

    public int StatusCode;
    public String ResponseBody;
    Throwable error;
    private static final String TAG = HttpClient.class.getSimpleName();


    public void post(String url, String JsonRequest) throws Exception {
        Log.i(TAG, "post(" + url + ") request : " + JsonRequest);

        error = null;

        ByteArrayEntity entity = new ByteArrayEntity(JsonRequest.getBytes());
        client.setTimeout(30 * 1000);
        client.setMaxRetriesAndTimeout(0, 30 * 1000);
        client.post(BaseActivity.GetMainActivity(), url, entity, "application/json", new AsyncHttpResponseHandler(Looper.getMainLooper()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "response : " + new String(responseBody));
                StatusCode = statusCode;
                ResponseBody = new String(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable _error) {
                StatusCode = statusCode;
                error = _error;
                Log.i(TAG, "failure statusCode : " + statusCode);
            }
        });

        if (error != null)
            throw new Exception(error);
    }

    public void get(String url) throws Exception {
        Log.i(TAG, "get(" + url + ")");

        error = null;
        AsyncHttpResponseHandler rh = new AsyncHttpResponseHandler(Looper.getMainLooper()) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "response : " + new String(responseBody));
                StatusCode = statusCode;
                ResponseBody = new String(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable _error) {
                Log.i(TAG, "failure statusCode : " + statusCode);
                StatusCode = statusCode;
                error = _error;
            }
        };
        //rh.setUsePoolThread(true);

        client.setTimeout(30 * 1000);
        client.setMaxRetriesAndTimeout(0, 30 * 1000);
        client.get(url, rh);

        if (error != null)
            throw new Exception(error);
    }
}
