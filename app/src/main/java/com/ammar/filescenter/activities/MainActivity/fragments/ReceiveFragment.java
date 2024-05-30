package com.ammar.filescenter.activities.MainActivity.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.adapters.ReceiveAdapter;
import com.ammar.filescenter.activities.MainActivity.models.Download;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReceiveFragment extends Fragment {
    View v;
    Button requestDownloadsB;
    EditText addressET;
    RecyclerView downloadsRV;
    ReceiveAdapter downloadsAdapter;
    OkHttpClient client = new OkHttpClient().newBuilder().
            followRedirects(false)
            .build();

    LinkedList<Download> downloadsList = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_receive, container, false);
        initItems();
        setItemsListeners();
        return v;
    }


    private void initItems() {
        requestDownloadsB = v.findViewById(R.id.B_RequestAvailableDownloads);
        addressET = v.findViewById(R.id.ET_IPInput);
        downloadsRV = v.findViewById(R.id.RV_DownloadsList);


        downloadsAdapter = new ReceiveAdapter(downloadsList);
        downloadsRV.setAdapter(downloadsAdapter);
        downloadsRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setItemsListeners() {
        requestDownloadsB.setOnClickListener((button) -> {
            String ip = addressET.getText().toString();
            if (!validateIp(ip)) return;
            Request request = new Request.Builder()
                    .url("http://" + ip + ":2999" + "/available-downloads")
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        try {
                            JSONArray downloadsJson = new JSONArray(jsonResponse);
                            int i = 0;
                            for (; i < downloadsJson.length(); i++) {
                                JSONObject downloadJson = downloadsJson.getJSONObject(i);
                                Download download = new Download(
                                        downloadJson.getString("uuid"),
                                        downloadJson.getString("name"),
                                        downloadJson.getInt("size"));
                                downloadsList.add(download);
                            }
                            int itemsAdded = i + 1;

                            getActivity().runOnUiThread(() -> {
                                downloadsAdapter.notifyItemRangeInserted(
                                        downloadsList.size() - itemsAdded,
                                        itemsAdded
                                );
                            });

                        } catch (JSONException e) {
                            Log.d("MYLOG", "JSON Failed: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("MYLOG", "Request failed: " + e.getMessage());
                }

            });

        });
    }

    private boolean validateIp(String ip) {
        return true; // TODO: Validate input
    }

}
