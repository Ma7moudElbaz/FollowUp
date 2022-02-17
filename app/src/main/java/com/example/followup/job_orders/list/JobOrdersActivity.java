package com.example.followup.job_orders.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.example.followup.R;
import com.example.followup.bottomsheets.BottomSheet_choose_filter_job_orders;
import com.example.followup.job_orders.AddJobOrderActivity;
import com.example.followup.utils.UserType;
import com.example.followup.utils.UserUtils;
import com.example.followup.webservice.Webservice;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobOrdersActivity extends LocalizationActivity implements BottomSheet_choose_filter_job_orders.FilterListener{



    public void showFilterSheet() {
        BottomSheet_choose_filter_job_orders langBottomSheet =
                new BottomSheet_choose_filter_job_orders(selectedStatusIndex);
        langBottomSheet.show(getSupportFragmentManager(), "jo_filter");
    }

    public static void hideKeyboardFragment(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboardActivity(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    ImageView back;
    ProgressBar loading;
    RecyclerView recyclerView;
    TextView search;
    ImageView filterBtn;
    FloatingActionButton fab_add_job_order;

    ArrayList<Job_order_item> job_order_list;
    Job_orders_adapter job_order_adapter;

    int currentPageNum = 1;
    int lastPageNum;
    boolean mHasReachedBottomOnce = false;

    int projectId;



    int selectedStatusIndex = -1;
    String selectedStatus = "";
    String[] chipsStatus = new String[]{"1", "3", "5", "8","7"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_orders);
        initFields();


        back.setOnClickListener(v -> onBackPressed());
        filterBtn.setOnClickListener(v -> showFilterSheet());
        search.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                job_order_list.clear();
                currentPageNum = 1;
                getJobOrders(currentPageNum, getFilterMap());
                hideKeyboardActivity(JobOrdersActivity.this);
                return true;
            }
            return false;
        });
        fab_add_job_order.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), AddJobOrderActivity.class);
            i.putExtra("project_id", projectId);
            startActivity(i);
        });
    }

    private void setUserPermissions(){
        String loggedInUser = UserType.getUserType(UserUtils.getParentId(getBaseContext()), UserUtils.getChildId(getBaseContext()));
        if (loggedInUser.equals("nagat")) {
            fab_add_job_order.setVisibility(View.VISIBLE);
        } else {
            fab_add_job_order.setVisibility(View.GONE);
        }
    }

    public void getJobOrders(int pageNum, Map<String, String> filterMap) {
        loading.setVisibility(View.VISIBLE);

        Webservice.getInstance().getApi().getJobOrders(UserUtils.getAccessToken(getBaseContext()), projectId, pageNum, filterMap).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    JSONObject responseObject = new JSONObject(response.body().string());
                    JSONArray jobOrdersArray = responseObject.getJSONArray("data");
                    setJobOrdersList(jobOrdersArray);
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
                Toast.makeText(getBaseContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
            }
        });
    }

    public void setJobOrdersList(JSONArray list) {
        try {
            for (int i = 0; i < list.length(); i++) {

                JSONObject currentObject = list.getJSONObject(i);
                final int id = currentObject.getInt("id");
                final int project_id = currentObject.getInt("project_id");
                final int status_code = currentObject.getInt("status");
                final String status_message = currentObject.getString("status_message");
                final String name = currentObject.getString("name");
                final String po_number = currentObject.getString("po_number");

                job_order_list.add(new Job_order_item(id, project_id, status_code, status_message,
                        name, po_number));

            }

            job_order_adapter.notifyDataSetChanged();
            mHasReachedBottomOnce = false;
            currentPageNum++;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initFields() {
        projectId = getIntent().getIntExtra("project_id", 0);

        back = findViewById(R.id.back);
        fab_add_job_order = findViewById(R.id.fab_add_job_order);
        loading = findViewById(R.id.loading);
        search = findViewById(R.id.search);
        filterBtn = findViewById(R.id.filter_btn);
        recyclerView = findViewById(R.id.recycler_view);
        job_order_list = new ArrayList<>();
        initRecyclerView();
        setUserPermissions();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        job_order_adapter = new Job_orders_adapter(getBaseContext(), job_order_list);
        recyclerView.setAdapter(job_order_adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !mHasReachedBottomOnce) {
                    mHasReachedBottomOnce = true;

                    if (currentPageNum <= lastPageNum)
                        getJobOrders(currentPageNum, getFilterMap());

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        job_order_list.clear();
        currentPageNum = 1;
        getJobOrders(currentPageNum, getFilterMap());
    }

    public Map<String, String> getFilterMap() {
        Map<String, String> map = new HashMap<>();
        map.put("status", selectedStatus);
        map.put("search", search.getText().toString());

        return map;
    }

    @Override
    public void applyFilterListener(int returnedSelectedStatusIndex) {
        if (returnedSelectedStatusIndex == -1){
            selectedStatus = "";
        }else {
            selectedStatus = chipsStatus[returnedSelectedStatusIndex];
        }

        selectedStatusIndex = returnedSelectedStatusIndex;

        job_order_list.clear();
        currentPageNum = 1;
        getJobOrders(currentPageNum, getFilterMap());
    }
}