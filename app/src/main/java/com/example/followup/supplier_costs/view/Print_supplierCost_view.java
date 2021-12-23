package com.example.followup.supplier_costs.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.followup.R;
import com.example.followup.requests.RequestDetailsActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Print_supplierCost_view extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_print_supplier_cost_view, container, false);
    }

    TextView supplier_name, cost, delivery_date, expiry_date, notes,print_type;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(view);
    }
    private void initFields(View view) {
        supplier_name = view.findViewById(R.id.supplier_name);
        cost = view.findViewById(R.id.cost);
        delivery_date = view.findViewById(R.id.delivery_date);
        expiry_date = view.findViewById(R.id.expiry_date);
        notes = view.findViewById(R.id.notes);
        print_type = view.findViewById(R.id.printing_type);


        RequestDetailsActivity activity = (RequestDetailsActivity) getActivity();

        try {
            assert activity != null;
            setFields(activity.getDataObj());
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
        print_type.setText(costObj.getString("print_type"));
    }
}