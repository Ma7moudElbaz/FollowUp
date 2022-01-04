package com.example.followup.home.projects;

import android.content.Intent;
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
import com.example.followup.utils.UserUtils;
import com.example.followup.webservice.Webservice;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectsFragment extends Fragment implements Projects_adapter_with_callback.AdapterCallback {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    FloatingActionButton fab_addProject;
    RecyclerView recyclerView;
    ProgressBar loading;

    ArrayList<Project_item> projects_list;
    Projects_adapter_with_callback projects_adapter;

    int currentPageNum = 1;
    int lastPageNum;
    boolean mHasReachedBottomOnce = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(view);
        fab_addProject.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddProjectActivity.class)));

    }

    public void getProjects(int pageNum) {
        loading.setVisibility(View.VISIBLE);

        Webservice.getInstance().getApi().getProjects(UserUtils.getAccessToken(getContext()), pageNum).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                try {
                    JSONObject responseObject = new JSONObject(response.body().string());
                    JSONArray projectsArray = responseObject.getJSONArray("data");
                    setProjectsList(projectsArray);
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

    public void setProjectsList(JSONArray list) {
        try {
            for (int i = 0; i < list.length(); i++) {
                JSONObject currentObject = list.getJSONObject(i);
                final int id = currentObject.getInt("id");
                final int user_id = currentObject.getInt("user_id");
                final int status_code = currentObject.getInt("status");
                final String status_message = currentObject.getString("status_message");
                final String client_company = currentObject.getString("client_company");
                final String project_name = currentObject.getString("project_name");
                final String client_name = currentObject.getString("client_name");
                final String project_country = currentObject.getString("project_country");
                final String project_timeline = currentObject.getString("project_timeline");
                final String created_at = currentObject.getString("created_at");
                final String created_by = currentObject.getJSONObject("user").getString("name");
                final int created_by_id = currentObject.getJSONObject("user").getInt("id");
                final String assigned_to = currentObject.getString("assign_to");
                int assigned_to_id=0;
                if (!assigned_to.equals("null")){
                    assigned_to_id = Integer.parseInt(assigned_to);
                }

                projects_list.add(new Project_item(id,user_id,status_code, created_by_id, assigned_to_id, status_message, client_company,project_name,client_name,project_country,project_timeline,created_at,created_by));

            }

            projects_adapter.notifyDataSetChanged();
            mHasReachedBottomOnce = false;
            currentPageNum++;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initFields(View view){
        fab_addProject = view.findViewById(R.id.fab_add_project);
        loading = view.findViewById(R.id.loading);
        recyclerView = view.findViewById(R.id.recycler_view);
        projects_list = new ArrayList<>();
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        projects_adapter = new Projects_adapter_with_callback(getContext(),this, projects_list);
        recyclerView.setAdapter(projects_adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !mHasReachedBottomOnce) {
                    mHasReachedBottomOnce = true;

                    if (currentPageNum <= lastPageNum)
                        getProjects(currentPageNum);

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        projects_list.clear();
        currentPageNum = 1;
        getProjects(currentPageNum);
    }


    @Override
    public void adapterCallback(String action, int project_id) {
        if (action.equals("cancel")){
            cancelProject(project_id);
        }else if (action.equals("done")){
            doneProject(project_id);
        }
    }

    public void cancelProject(int project_id) {

        loading.setVisibility(View.VISIBLE);
        Webservice.getInstance().getApi().projectCancel(UserUtils.getAccessToken(getContext()), project_id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_LONG).show();
                        projects_list.clear();
                        currentPageNum = 1;
                        getProjects(currentPageNum);
                    } else {
                        Toast.makeText(getContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
            }
        });
    }

    public void doneProject(int project_id) {

        loading.setVisibility(View.VISIBLE);
        Webservice.getInstance().getApi().projectDone(UserUtils.getAccessToken(getContext()), project_id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200 || response.code() == 201) {
                        Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_LONG).show();
                        projects_list.clear();
                        currentPageNum = 1;
                        getProjects(currentPageNum);
                    } else {
                        Toast.makeText(getContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
            }
        });
    }
}