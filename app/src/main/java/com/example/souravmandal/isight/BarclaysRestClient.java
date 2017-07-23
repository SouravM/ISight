package com.example.souravmandal.isight;
import com.loopj.android.http.*;


/**
 * Created by Sourav Mandal on 7/17/2017.
 */

public class BarclaysRestClient {
    private static final String BASE_URL = "https://atlas.api.barclays/open-banking/v1.3/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
