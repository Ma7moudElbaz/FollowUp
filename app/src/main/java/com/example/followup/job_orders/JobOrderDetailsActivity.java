package com.example.followup.job_orders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.example.followup.R;
import com.example.followup.utils.UserType;
import com.example.followup.utils.UserUtils;
import com.example.followup.webservice.Webservice;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import params.com.stepview.StatusViewScroller;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobOrderDetailsActivity extends LocalizationActivity {

    private ProgressDialog dialog;
    LinearLayout sales_approval_layout, magdi_approval_layout, hesham_approval_layout, ceo_approval_layout;
    Button sales_approve, sales_reject, magdi_approve, magdi_hold, hesham_approve, hesham_reject, hesham_ceo_approval, ceo_approve, ceo_reject;
    ProgressBar loading;
    ImageView ceoSteps;
    TextView download;
    int jobOrderId;
    int jobOrderStatus;
    StatusViewScroller steps;
    String pdfUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_order_details);
        initFields();
        download.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))));
        sales_reject.setOnClickListener(v -> updateStatus(2,""));
        sales_approve.setOnClickListener(v -> updateStatus(3,""));
        magdi_hold.setOnClickListener(v -> updateStatus(4,""));
        magdi_approve.setOnClickListener(v -> updateStatus(5,""));
        hesham_reject.setOnClickListener(v -> updateStatus(6,""));
        hesham_approve.setOnClickListener(v -> updateStatus(7,""));
        hesham_ceo_approval.setOnClickListener(v -> updateStatus(8,""));
        ceo_reject.setOnClickListener(v -> updateStatus(9,""));
        ceo_approve.setOnClickListener(v -> updateStatus(10,""));

    }

    private void initFields() {

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please, Wait...");
        dialog.setCancelable(false);

        loading = findViewById(R.id.loading);
        download = findViewById(R.id.download);
        sales_approval_layout = findViewById(R.id.sales_approval_layout);
        magdi_approval_layout = findViewById(R.id.magdi_approval_layout);
        hesham_approval_layout = findViewById(R.id.hesham_approval_layout);
        ceo_approval_layout = findViewById(R.id.ceo_approval_layout);
        ceoSteps = findViewById(R.id.ceo_steps);
        sales_approve = findViewById(R.id.sales_approve);
        sales_reject = findViewById(R.id.sales_reject);
        magdi_approve = findViewById(R.id.magdi_approve);
        magdi_hold = findViewById(R.id.magdi_hold);
        hesham_approve = findViewById(R.id.hesham_approve);
        hesham_reject = findViewById(R.id.hesham_reject);
        hesham_ceo_approval = findViewById(R.id.hesham_ceo_approval);
        ceo_approve = findViewById(R.id.ceo_approve);
        ceo_reject = findViewById(R.id.ceo_reject);

        pdfUrl = "https://saudiblood.org/pdf/Founding-Regulations.pdf";

        jobOrderId = getIntent().getIntExtra("job_order_id", 0);
        jobOrderStatus = getIntent().getIntExtra("job_order_status", 0);
        setUserJobOrderPermissions(jobOrderStatus);
    }


    private void setUserJobOrderPermissions(int jobOrderStatus) {
        Log.e("jobOrderStatus", String.valueOf(jobOrderStatus));
        String loggedInUser = UserType.getUserType(UserUtils.getParentId(getBaseContext()), UserUtils.getChildId(getBaseContext()));
        Log.e("loggedInUser", loggedInUser);
        resetData();
        switch (jobOrderStatus) {
            case 1: {
                steps.scrollToStep(2);
                if (loggedInUser.equals("sales")) {
                    sales_approval_layout.setVisibility(View.VISIBLE);
                } else {
                    sales_approval_layout.setVisibility(View.GONE);
                }
                break;
            }
            case 3:
            case 4: {
                steps.scrollToStep(3);
                if (loggedInUser.equals("magdi")) {
                    magdi_approval_layout.setVisibility(View.VISIBLE);
                } else {
                    magdi_approval_layout.setVisibility(View.GONE);
                }
            }
            case 5:
            case 10: {
                steps.scrollToStep(4);
                if (loggedInUser.equals("hesham")) {
                    hesham_approval_layout.setVisibility(View.VISIBLE);
                } else {
                    hesham_approval_layout.setVisibility(View.GONE);
                }
            }
            case 6: {
                //hesham rejected
                steps.scrollToStep(4);
            }
            case 7: {
                steps.scrollToStep(5);
            }
            case 8: {
                steps.setVisibility(View.GONE);
                ceoSteps.setVisibility(View.VISIBLE);
                if (loggedInUser.equals("ceo")) {
                    ceo_approval_layout.setVisibility(View.VISIBLE);
                } else {
                    ceo_approval_layout.setVisibility(View.GONE);
                }
            }
            case 9: {
                //ceo rejected
                steps.setVisibility(View.GONE);
                ceoSteps.setVisibility(View.VISIBLE);
            }
        }
    }

    private void resetData() {
        sales_approval_layout.setVisibility(View.GONE);
        magdi_approval_layout.setVisibility(View.GONE);
        hesham_approval_layout.setVisibility(View.GONE);
        ceo_approval_layout.setVisibility(View.GONE);
        ceoSteps.setVisibility(View.GONE);
    }


    public void updateStatus(int status,String reason) {
        Map<String, String> map = new HashMap<>();
        map.put("job_order_id", String.valueOf(jobOrderId));
        map.put("status", String.valueOf(status));
//        map.put("reason", reason);

        dialog.show();
        Webservice.getInstance().getApi().changeJobOrderStatus(UserUtils.getAccessToken(getBaseContext()), map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        Toast.makeText(getBaseContext(), "Updated successfully", Toast.LENGTH_LONG).show();
                        getJobOrderDetails();
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



    private void getJobOrderDetails() {
        loading.setVisibility(View.VISIBLE);

        Webservice.getInstance().getApi().getJobOrderDetails(UserUtils.getAccessToken(getBaseContext()), jobOrderId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    loading.setVisibility(View.GONE);
                    JSONObject responseObject = new JSONObject(response.body().string());
                    JSONObject dataObj = responseObject.getJSONObject("data");
                    pdfUrl = dataObj.getString("url");
                    jobOrderStatus = dataObj.getInt("status");

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

    @Override
    protected void onResume() {
        super.onResume();
        getJobOrderDetails();
    }
}