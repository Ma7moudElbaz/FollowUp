package com.example.followup.requests;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.example.followup.R;
import com.example.followup.requests.view.Photography_view;
import com.example.followup.requests.view.Print_view;
import com.example.followup.requests.view.Production_view;
import com.example.followup.requests.view.Purchase_view;
import com.example.followup.supplier_costs.add.AddPhotographySupplierCostActivity;
import com.example.followup.supplier_costs.add.AddPrintSupplierCostActivity;
import com.example.followup.supplier_costs.add.AddProductionSupplierCostActivity;
import com.example.followup.supplier_costs.add.AddPurchaseSupplierCostActivity;
import com.example.followup.supplier_costs.view.Photography_supplierCost_view;
import com.example.followup.supplier_costs.view.Print_supplierCost_view;
import com.example.followup.supplier_costs.view.Production_supplierCost_view;
import com.example.followup.supplier_costs.view.Purchase_supplierCost_view;
import com.example.followup.utils.UserType;
import com.example.followup.utils.UserUtils;
import com.example.followup.webservice.Webservice;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import params.com.stepview.StatusView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDetailsActivity extends LocalizationActivity {

    public int getCostStatus() {
        return costStatus;
    }

    boolean isDetailsExpanded = false;
    boolean isCostExpanded = false;
    private ProgressDialog dialog;
    ImageView back, expandDetails, expandCost,editCost;
    FrameLayout request_details_content, cost_details_content;
    RelativeLayout request_cost_container;
    LinearLayout no_cost_container,sales_approval_layout;
    Button sales_approve,sales_reject;
    ProgressBar loading;
    Button add_cost;
    int costStatus;
    int costId;


    int request_id, type_id;
    JSONObject dataObj;
    StatusView steps;

    public JSONObject getDataObj() {
        return dataObj;
    }

    public void setDetailsFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.request_details_content, fragment);
        fragmentTransaction.commit();
    }

    public void setCostFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.cost_details_content, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);
        initFields();

        back.setOnClickListener(v -> onBackPressed());
        expandDetails.setOnClickListener(v -> toggleDetails(isDetailsExpanded));
        expandCost.setOnClickListener(v -> toggleCost(isCostExpanded));
        add_cost.setOnClickListener(v -> gotoAddCost(request_id, type_id));

        sales_reject.setOnClickListener(v -> updateStatus(5,""));
        sales_approve.setOnClickListener(v -> updateStatus(6,""));


    }

    private void gotoAddCost(int request_id, int type_id) {
        Intent i;
        switch (type_id) {
            case 1:
                i = new Intent(getBaseContext(), AddPurchaseSupplierCostActivity.class);
                break;
            case 2:
                i = new Intent(getBaseContext(), AddPrintSupplierCostActivity.class);
                break;
            case 3:
                i = new Intent(getBaseContext(), AddProductionSupplierCostActivity.class);
                break;
            case 4:
                i = new Intent(getBaseContext(), AddPhotographySupplierCostActivity.class);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type_id);
        }
        i.putExtra("request_id", request_id);
        startActivity(i);

    }

    private void initFields() {

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please, Wait...");
        dialog.setCancelable(false);

        request_id = getIntent().getIntExtra("request_id", 0);
        type_id = getIntent().getIntExtra("type_id", 0);
        loading = findViewById(R.id.loading);
        expandDetails = findViewById(R.id.expand_details);
        expandCost = findViewById(R.id.expand_cost);
        back = findViewById(R.id.back);
        request_details_content = findViewById(R.id.request_details_content);
        request_cost_container = findViewById(R.id.request_cost_container);
        cost_details_content = findViewById(R.id.cost_details_content);
        no_cost_container = findViewById(R.id.no_cost_container);
        add_cost = findViewById(R.id.add_cost);
        steps = findViewById(R.id.steps);
        sales_approval_layout = findViewById(R.id.sales_approval_layout);
        sales_approve = findViewById(R.id.sales_approve);
        sales_reject = findViewById(R.id.sales_reject);
        editCost = findViewById(R.id.edit_cost);

    }

    private void expandDetails() {
        expandDetails.setImageResource(R.drawable.ic_arrow_up);
        request_details_content.setVisibility(View.VISIBLE);
        isDetailsExpanded = true;

        expandCost.setImageResource(R.drawable.ic_arrow_down);
        request_cost_container.setVisibility(View.GONE);
        isCostExpanded = false;
    }

    private void expandCost() {
        expandCost.setImageResource(R.drawable.ic_arrow_up);
        request_cost_container.setVisibility(View.VISIBLE);
        isCostExpanded = true;

        expandDetails.setImageResource(R.drawable.ic_arrow_down);
        request_details_content.setVisibility(View.GONE);
        isDetailsExpanded = false;
    }

    private void toggleCost(boolean expanded) {
        if (expanded) {
            expandCost.setImageResource(R.drawable.ic_arrow_down);
            request_cost_container.setVisibility(View.GONE);
            isCostExpanded = false;
        } else {
            expandCost();
        }
    }

    private void toggleDetails(boolean expanded) {
        if (expanded) {
            expandDetails.setImageResource(R.drawable.ic_arrow_down);
            request_details_content.setVisibility(View.GONE);
            isDetailsExpanded = false;
        } else {
            expandDetails();
        }
    }

    private void getRequestDetails() {
        loading.setVisibility(View.VISIBLE);

        Webservice.getInstance().getApi().getRequestDetails(UserUtils.getAccessToken(getBaseContext()), request_id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    JSONObject responseObject = new JSONObject(response.body().string());
                    dataObj = responseObject.getJSONObject("data");
                    if (dataObj.getString("cost").equals("null")) {
                        costStatus = 1;
                        setUserCostPermissions(1);
                    }else {
                        costStatus = dataObj.getJSONObject("cost").getInt("status");
                        costId = dataObj.getJSONObject("cost").getInt("id");
                        setUserCostPermissions(costStatus);
                    }
                    setFragments(type_id,costStatus);
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

    private void setCostContainer(boolean hasCost) {
        if (hasCost) {
            cost_details_content.setVisibility(View.VISIBLE);
            no_cost_container.setVisibility(View.GONE);
        } else {
            cost_details_content.setVisibility(View.GONE);
            no_cost_container.setVisibility(View.VISIBLE);
        }
    }

    private void setFragments(int type_id,int cost_status) {
        switch (type_id) {
            case 1:
                setDetailsFragment(new Purchase_view());
                if (cost_status!=0)
                setCostFragment(new Purchase_supplierCost_view());
                break;
            case 2:
                setDetailsFragment(new Print_view());
                if (cost_status!=0)
                setCostFragment(new Print_supplierCost_view());
                break;
            case 3:
                setDetailsFragment(new Production_view());
                if (cost_status!=0)
                setCostFragment(new Production_supplierCost_view());
                break;
            case 4:
                setDetailsFragment(new Photography_view());
                if (cost_status!=0)
                setCostFragment(new Photography_supplierCost_view());
                break;
        }
    }

    private void setUserCostPermissions(int costStatus) {
        Log.e("costStatus", String.valueOf(costStatus) );
        String loggedInUser = UserType.getUserType(UserUtils.getParentId(getBaseContext()), UserUtils.getChildId(getBaseContext()));
        Log.e("loggedInUser", loggedInUser);
        resetData();
        switch (costStatus) {
            case 1: {
                setCostContainer(false);
                steps.setCurrentCount(1);
                if (loggedInUser.equals("nagatTeam") || loggedInUser.equals("nagat")){
                    add_cost.setVisibility(View.VISIBLE);
                }else {
                    add_cost.setVisibility(View.GONE);
                }
                break;
            }
            case 2:{
                steps.setCurrentCount(2);
                //handle buttons in SupplierCost fragments
            }
            case 3:
            case 5:{
                steps.setCurrentCount(2);
                if (loggedInUser.equals("nagatTeam") || loggedInUser.equals("nagat")){
                    editCost.setVisibility(View.VISIBLE);
                }else {
                    editCost.setVisibility(View.GONE);
                }
            }
            case 4:{
                steps.setCurrentCount(3);
                if (loggedInUser.equals("sales")){
                    sales_approval_layout.setVisibility(View.VISIBLE);
                }else {
                    sales_approval_layout.setVisibility(View.GONE);
                }
            }
            case 6:{
                steps.setCurrentCount(4);
            }
        }
    }

    private void resetData(){
        setCostContainer(true);
        editCost.setVisibility(View.GONE);
        sales_approval_layout.setVisibility(View.GONE);
    }

    public void updateStatus(int status,String reason) {
        Map<String, String> map = new HashMap<>();
        map.put("cost_id", String.valueOf(costId));
        map.put("status", String.valueOf(status));
        map.put("reason", reason);

        dialog.show();
        Webservice.getInstance().getApi().changeCostStatus(UserUtils.getAccessToken(getBaseContext()), map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        Toast.makeText(getBaseContext(), "Updated successfully", Toast.LENGTH_LONG).show();
                        getRequestDetails();
                    } else {
                        Toast.makeText(getBaseContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getBaseContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRequestDetails();
    }
}