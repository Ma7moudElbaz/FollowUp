package com.example.followup.supplier_costs.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.followup.R;
import com.example.followup.requests.RequestDetailsActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Production_supplierCost_view extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_production_supplier_cost_view, container, false);
    }

    TextView supplier_name, cost, delivery_date, expiry_date, notes,assembly_dismantling,storage;
    LinearLayout nagat_approval_container;
    ImageView nagat_approve,nagat_reject;
    int costStatus;
    RequestDetailsActivity activity;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(view);
        nagat_reject.setOnClickListener(v -> activity.updateStatus(3,""));
        nagat_approve.setOnClickListener(v -> activity.updateStatus(4,""));
    }
    private void initFields(View view) {
        activity = (RequestDetailsActivity) getActivity();
        supplier_name = view.findViewById(R.id.supplier_name);
        cost = view.findViewById(R.id.cost);
        delivery_date = view.findViewById(R.id.delivery_date);
        expiry_date = view.findViewById(R.id.expiry_date);
        notes = view.findViewById(R.id.notes);
        assembly_dismantling = view.findViewById(R.id.assembly_dismantling);
        storage = view.findViewById(R.id.storage);

        nagat_approval_container = view.findViewById(R.id.nagat_approval_container);
        nagat_approve = view.findViewById(R.id.nagat_approve);
        nagat_reject = view.findViewById(R.id.nagat_reject);

        RequestDetailsActivity activity = (RequestDetailsActivity) getActivity();

        try {
            assert activity != null;
            costStatus = activity.getCostStatus();
            setFields(activity.getDataObj());
            setUserCostPermissions(costStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setFields(JSONObject dataObj) throws JSONException {
        JSONObject costObj = dataObj.getJSONObject("cost");
        supplier_name.setText(costObj.getString("supplier_name"));
        cost.setText(costObj.getString("cost"));
        delivery_date.setText(costObj.getString("delivery_date"));
        expiry_date.setText(costObj.getString("expiry_date"));
        notes.setText(costObj.getString("note"));
        assembly_dismantling.setText(costObj.getString("assembly_dimension"));
        storage.setText(costObj.getString("storage"));
    }

    private void setUserCostPermissions(int costStatus) {
        if (costStatus == 2){
            nagat_approval_container.setVisibility(View.VISIBLE);
        }else {
            nagat_approval_container.setVisibility(View.GONE);
        }
    }
}