package com.platform;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 9/29/16.
 * Copyright (c) 2016 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class APIClient {

    private static final String FEE_PER_KB_URL = "https://go.digibyte.co/bws/api/v2/feelevels";
    private final OkHttpClient client = new OkHttpClient.Builder().followRedirects(
            false).writeTimeout(60, TimeUnit.SECONDS).readTimeout(60,
            TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS).retryOnConnectionFailure(
            true).build();
    private static APIClient ourInstance;

    public static synchronized APIClient getInstance(Context context) {
        if (ourInstance == null) ourInstance = new APIClient();
        return ourInstance;
    }

    public long feePerKb() {
        Response response = null;
        long fee = 80000;
        try {
            Request request = new Request.Builder().url(FEE_PER_KB_URL).get().build();
            response = sendRequest(request);
            String body = response.body().string();
            JSONArray jsonArray = new JSONArray(body);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                if (jObject.getString("normal") != null) {
                    fee = jObject.getInt("feePerKb");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
            return fee;
        }
    }

    public Response sendRequest(Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return new Response.Builder().code(599).request(request).body(ResponseBody.create(null, new byte[0])).protocol(Protocol.HTTP_1_1).message("").build();
        }
    }
}