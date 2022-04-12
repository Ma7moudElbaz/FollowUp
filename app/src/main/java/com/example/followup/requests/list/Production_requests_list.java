package com.example.followup.requests.list;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.followup.R;
import com.example.followup.home.Attach_item;
import com.example.followup.requests.RequestsActivity;
import com.example.followup.requests.list.adapters.Production_adapter;
import com.example.followup.requests.list.models.Production_item;
import com.example.followup.utils.UserUtils;
import com.example.followup.webservice.WebserviceContext;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Production_requests_list extends Fragment {

    RecyclerView recyclerView;
    ProgressBar loading;

    ArrayList<Production_item> production_list;
    Production_adapter production_adapter;

    int currentPageNum = 1;
    int lastPageNum;
    boolean mHasReachedBottomOnce = false;

    int projectId;
    RequestsActivity activity;

    WebserviceContext ws;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_production_requests_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(view);

    }

    public void getRequests(int selectedTab, int pageNum, Map<String, String> filterMap) {
        loading.setVisibility(View.VISIBLE);

        ws.getApi().getRequests(UserUtils.getAccessToken(getContext()), projectId, (selectedTab + 1), pageNum, filterMap).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    JSONObject responseObject = new JSONObject(response.body().string());
                    JSONArray requestsArray = responseObject.getJSONArray("data");
                    setPrintList(requestsArray);
                    JSONObject metaObject = responseObject.getJSONObject("meta");
                    lastPageNum = metaObject.getInt("last_page");

                    loading.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d("Error Throw", t.toString());
                Log.d("commit Test Throw", t.toString());
                Log.d("Call", t.toString());
                Toast.makeText(getContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
            }
        });
    }


    public void setPrintList(JSONArray list) {
        try {
            for (int i = 0; i < list.length(); i++) {


                JSONObject currentObject = list.getJSONObject(i);
                final int id = currentObject.getInt("id");
                final int type_id = currentObject.getInt("type_id");
                final int created_by_id = currentObject.getInt("created_by_id");
                final int status_code = currentObject.getInt("status");
                final String quantity = currentObject.getString("quantity");
                final String status_message = currentObject.getString("status_message");
                final String item_name = currentObject.getString("item_name");
                final String description = currentObject.getString("description");
//                final String delivery_address = currentObject.getString("delivery_address");
                final String delivery_date = currentObject.getString("delivery_address");
                final String note = currentObject.getString("note");
                final String country = currentObject.getString("country");
                final String venue = currentObject.getString("venue");
                final String days = currentObject.getString("days");
                final String dimensions = currentObject.getString("dimension");
                final String screen = currentObject.getString("screen");
                final String designer_name = currentObject.getString("designer_name");
                final String created_by_name = currentObject.getJSONObject("created_by_name").getString("name");

                ArrayList<Attach_item> attach_files = new ArrayList<>();

                production_list.add(new Production_item(id, type_id, created_by_id, status_code, quantity, status_message,
                        item_name, description, delivery_date, note, country, venue, days, dimensions, screen,
                        designer_name, created_by_name, attach_files));

            }

            production_adapter.notifyDataSetChanged();
            mHasReachedBottomOnce = false;
            currentPageNum++;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initFields(View view) {
        ws = new WebserviceContext(getActivity());
        activity = (RequestsActivity) getActivity();
        projectId = activity.getProjectId();

        loading = view.findViewById(R.id.loading);
        recyclerView = view.findViewById(R.id.recycler_view);
        production_list = new ArrayList<>();
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        production_adapter = new Production_adapter(getContext(), production_list);
        recyclerView.setAdapter(production_adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !mHasReachedBottomOnce) {
                    mHasReachedBottomOnce = true;

                    if (currentPageNum <= lastPageNum)
                        getRequests(0, currentPageNum,activity.getFilterMap());

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        production_list.clear();
        currentPageNum = 1;
        getRequests(2, currentPageNum,activity.getFilterMap());
    }
}