package com.example.app2025.Api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class HttpProvider {
    public static JSONObject sendPost(String URL, RequestBody formBody) {
        // Create a result holder
        final JSONObject[] result = new JSONObject[1];

        try {
            // Create a thread to perform the network operation
            Thread networkThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                                .tlsVersions(TlsVersion.TLS_1_2)
                                .cipherSuites(
                                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                                .build();

                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectionSpecs(Collections.singletonList(spec))
                                .callTimeout(5000, TimeUnit.MILLISECONDS)
                                .build();

                        Request request = new Request.Builder()
                                .url(URL)
                                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                .post(formBody)
                                .build();

                        Response response = client.newCall(request).execute();

                        if (!response.isSuccessful()) {
                            Log.e("BAD_REQUEST", response.body().string());
                            result[0] = null;
                        } else {
                            result[0] = new JSONObject(response.body().string());
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        result[0] = null;
                    }
                }
            });

            // Start the thread and wait for it to finish
            networkThread.start();
            networkThread.join();

            return result[0];
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}