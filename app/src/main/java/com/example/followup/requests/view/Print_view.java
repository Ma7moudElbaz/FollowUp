package com.example.followup.requests.view;

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

public class Print_view extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_print_view, container, false);
    }

    TextView item_name, quantity, description, pages, paper_weight, colors, di_cut, delivery_address, notes, designer_in_charge, print_type, lamination, binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(view);
    }

    private void initFields(View view) {
        item_name = view.findViewById(R.id.item_name);
        quantity = view.findViewById(R.id.quantity);
        description = view.findViewById(R.id.description);
        pages = view.findViewById(R.id.pages);
        paper_weight = view.findViewById(R.id.paper_weight);
        colors = view.findViewById(R.id.colors);
        di_cut = view.findViewById(R.id.di_cut);
        delivery_address = view.findViewById(R.id.delivery_address);
        notes = view.findViewById(R.id.notes);
        designer_in_charge = view.findViewById(R.id.designer_in_charge);
        print_type = view.findViewById(R.id.print_type);
        lamination = view.findViewById(R.id.lamination);
        binding = view.findViewById(R.id.binding);


        RequestDetailsActivity activity = (RequestDetailsActivity) getActivity();

        try {
            assert activity != null;
            setFields(activity.getDataObj());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setFields(JSONObject dataObj) throws JSONException {
        item_name.setText(dataObj.getString("item_name"));
        quantity.setText(dataObj.getString("quantity"));
        description.setText(dataObj.getString("description"));
        pages.setText(dataObj.getString("pages"));
        paper_weight.setText(dataObj.getString("paper_weight"));
        colors.setText(dataObj.getString("color"));
        di_cut.setText(dataObj.getString("di_cut"));
        delivery_address.setText(dataObj.getString("delivery_address"));
        notes.setText(dataObj.getString("note"));
        designer_in_charge.setText(dataObj.getString("designer_name"));
        print_type.setText(dataObj.getString("print_type"));
        lamination.setText(dataObj.getString("lamination"));
        binding.setText(dataObj.getString("binding"));
    }
}