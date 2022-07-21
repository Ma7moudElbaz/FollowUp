package com.example.followup.supplier_costs.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.followup.R;
import com.example.followup.requests.RequestDetailsActivity;
import com.example.followup.requests.view.attaches.Attach_item;
import com.example.followup.requests.view.attaches.Attaches_adapter;
import com.example.followup.utils.StringCheck;
import com.example.followup.utils.UserType;
import com.example.followup.utils.UserUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Extras_supplierCost_view extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_extras_supplier_cost_view, container, false);
    }


    TextView supplier_name, cost, delivery_date, notes;
    LinearLayout nagat_approval_container;
    ImageView nagat_approve, nagat_reject;
    int costStatus;
    RequestDetailsActivity activity;

    RecyclerView attach_recycler;
    ArrayList<Attach_item> attaches_list;
    Attaches_adapter attaches_adapter;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(view);
        nagat_reject.setOnClickListener(v -> activity.updateStatusDialog(3, ""));
        nagat_approve.setOnClickListener(v -> activity.updateStatusDialog(4, ""));
    }
    private void initFields(View view) {
        attach_recycler = view.findViewById(R.id.attaches_recycler);
        attaches_list = new ArrayList<>();
        initAttachesRecyclerView();


        activity = (RequestDetailsActivity) getActivity();
        supplier_name = view.findViewById(R.id.supplier_name);
        cost = view.findViewById(R.id.cost);
        delivery_date = view.findViewById(R.id.delivery_date);
        notes = view.findViewById(R.id.notes);

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

    private void initAttachesRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        attach_recycler.setLayoutManager(layoutManager);
        attaches_adapter = new Attaches_adapter(getContext(), attaches_list);
        attach_recycler.setAdapter(attaches_adapter);
    }


    private void setFields(JSONObject dataObj) throws JSONException {
        JSONObject costObj = dataObj.getJSONObject("cost");
        supplier_name.setText(costObj.getString("supplier_name"));
        String cost_text = costObj.getString("total_cost_per_type") + "  "+ costObj.getString("currency_name") ;
        cost.setText(cost_text);
        delivery_date.setText(StringCheck.returnText(costObj.getString("delivery_date")));
        notes.setText(StringCheck.returnText(costObj.getString("note")));

        if (!costObj.getString("reference").equals("null")) {
            setAttachesList(costObj.getString("reference"));
        }
    }

    public void setAttachesList(String reference) {
        attaches_list.add(new Attach_item(0, reference, "1"));

        attaches_adapter.notifyDataSetChanged();
    }

    private void setUserCostPermissions(int costStatus) {
        String loggedInUser = UserType.getUserType(UserUtils.getParentId(getContext()), UserUtils.getChildId(getContext()));
        if (costStatus == 2 && loggedInUser.equals("nagat")) {
            nagat_approval_container.setVisibility(View.VISIBLE);
        } else {
            nagat_approval_container.setVisibility(View.GONE);
        }
    }
}