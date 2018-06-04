package com.example.alex.fitofan.ui.fragments.wall;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.fitofan.R;
import com.example.alex.fitofan.client.Request;
import com.example.alex.fitofan.databinding.FragmentWallBinding;
import com.example.alex.fitofan.interfaces.ILoadingStatus;
import com.example.alex.fitofan.interfaces.LikeStatus;
import com.example.alex.fitofan.interfaces.SearchStatus;
import com.example.alex.fitofan.interfaces.SubStatus;
import com.example.alex.fitofan.models.GetSearchPlansModel;
import com.example.alex.fitofan.models.GetSearchUsersModel;
import com.example.alex.fitofan.models.GetTrainingModel;
import com.example.alex.fitofan.models.GetUserModel;
import com.example.alex.fitofan.models.GetWallModel;
import com.example.alex.fitofan.settings.MSharedPreferences;
import com.example.alex.fitofan.ui.activity.create_plan.CreatePlanActivity;
import com.example.alex.fitofan.utils.Connection;
import com.example.alex.fitofan.utils.CountData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class WallFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ILoadingStatus<GetWallModel>, LikeStatus, SearchStatus, SubStatus {

    FragmentWallBinding mBinding;
    private RecyclerAdapterWall adapter;
    private int scrolling;
    private ArrayList<GetTrainingModel> models;

    private boolean isLoading;
    private boolean isRefresh;
    private ImageView like;
    private int position;
    private ImageView save;
    private TextView countLike;
    private TextView countSaved;
    private boolean isStop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_wall, container, false);

        mBinding = DataBindingUtil.bind(view);
        models = new ArrayList<>();
        registerForContextMenu(mBinding.itemSearch.btFilters);
        initListeners();
        initRecyclerView();
        startRequest();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        onRefresh();
        super.onResume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.preview_plan_wall_with_del, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    private void startRequest() {
        if (Connection.isNetworkAvailable(getContext())) {
            scrolling = 1;
            mBinding.refresh.setRefreshing(true);
            goNexLoad();
        }
    }

    private void initListeners() {
        mBinding.refresh.setOnRefreshListener(this);

        mBinding.fabAddTraining.setOnClickListener(view1 -> {
            startActivity(new Intent(getContext(), CreatePlanActivity.class));
        });

        mBinding.itemSearch.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                search(mBinding.itemSearch.search.getText().toString());
            }
        });

        mBinding.itemSearch.btFilters.setOnClickListener(Objects.requireNonNull(this.getActivity())::openContextMenu);
    }

    private void search(String s) {
        if (Connection.isNetworkAvailable(getContext())) {
            HashMap<String, String> map = new HashMap<>();
            map.put("uid", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getUid());
            map.put("signature", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getSignature());
            map.put("search", s);
            Request.getInstance().searchPlans(map, this);
        }
    }

    protected void likePlan(String id, ImageView like, TextView countLike, boolean isButton, int position) {
        this.countLike = countLike;
        this.like = like;
        this.position = position;
        HashMap<String, String> map = new HashMap<>();
        map.put("uid", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getUid());
        map.put("signature", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getSignature());
        map.put("plan_id", id);
        if (Connection.isNetworkAvailable(getContext())) {
            if (!isButton) {
                Request.getInstance().like(map, this);
            }
            if (isButton) {
                if (models.get(position).getLiked() == 1)
                    Request.getInstance().dislikePlan(map, this);
                if (models.get(position).getLiked() != 1)
                    Request.getInstance().like(map, this);
            }
        }
    }

    protected void savePlan(String id, ImageView save, TextView countSaved, int position) {
        this.countSaved = countSaved;
        this.save = save;
        this.position = position;
        HashMap<String, String> map = new HashMap<>();
        map.put("uid", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getUid());
        map.put("signature", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getSignature());
        map.put("plan_id", id);
        if (Connection.isNetworkAvailable(getContext())) {
            if (models.get(position).getIsSaved() == 1)
                Request.getInstance().unSavePlan(map, this);
            if (models.get(position).getIsSaved() != 1)
                Request.getInstance().savePlan(map, this);
        }
    }

    private void initRecyclerView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mBinding.rvWall.setItemViewCacheSize(100);
        mBinding.rvWall.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerAdapterWall(models, this);
        mBinding.rvWall.setAdapter(adapter);
        mBinding.rvWall.setNestedScrollingEnabled(true);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = linearLayoutManager.getChildCount();//смотрим сколько элементов на экране
                int totalItemCount = linearLayoutManager.getItemCount();//сколько всего элементов
                int firstVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();//какая позиция первого элемента

                if (!isLoading) {//проверяем, грузим мы что-то или нет, эта переменная должна быть вне класса  OnScrollListener
                    if ((visibleItemCount + firstVisibleItems) >= totalItemCount) {
                        isLoading = true;//ставим флаг что мы попросили еще элемены
                        if (!isStop)
                            goNexLoad();
                    }
                }

                if (dy > 0 || dy < 0 && mBinding.fabAddTraining.isShown())
                    mBinding.fabAddTraining.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mBinding.fabAddTraining.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        };
        mBinding.rvWall.setOnScrollListener(scrollListener);
    }

    private void goNexLoad() {
        if (Connection.isNetworkAvailable(getContext()) && isLoading) {
            HashMap<String, String> map = new HashMap<>();
            map.put("uid", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getUid());
            map.put("signature", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getSignature());
            map.put("page", String.valueOf(scrolling));
            Request.getInstance().getWall(map, this);
            scrolling++;
        }
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mBinding.itemSearch.search.setText("");
        isRefresh = true;
        isLoading = true;
        isStop = false;
        scrolling = 1;
        if (Connection.isNetworkAvailable(getContext())) {
            HashMap<String, String> map = new HashMap<>();
            map.put("uid", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getUid());
            map.put("signature", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getSignature());
            map.put("page", "1");
            Request.getInstance().getWall(map, this);
        } else {
            mBinding.refresh.setRefreshing(false);
        }
    }

    @Override
    public void onSuccess(GetWallModel info) {
        if (isRefresh) {
            models.clear();
            models.addAll(info.getTrainings());
            adapter.setWallModels(models);
            adapter.notifyDataSetChanged();
            scrolling++;
            isLoading = false;
            isRefresh = false;
        } else if (info.getTrainings() != null && info.getTrainings().size() > 0) {
            models.addAll(info.getTrainings());
            adapter.setWallModels(models);
            adapter.notifyDataSetChanged();
            isLoading = false;
        } else {
            isStop = true;
        }
        mBinding.refresh.setRefreshing(false);
    }

    @Override
    public void onSuccessPlans(GetSearchPlansModel info) {
        if (info.getTrainings() != null && info.getTrainings().size() > 0) {
            models.clear();
            models.addAll(info.getTrainings());
            adapter.setWallModels(models);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSuccessUsers(GetSearchUsersModel info) {

    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    @Override
    public void onSuccess(String info) {
        if (info.equals("like") || info.equals("dislike")) {
            if (info.equals("like")) {
                if (models.get(position).getLiked() != 1) {
                    like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_full_red));
                    countLike.setText(
                            CountData.mathLikes(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getLikes()) + 1))
                    );
                    countLike.setTextColor(Color.parseColor("#FFFFFF"));
                    adapter.getmWallModels().get(position).setLikes(
                            CountData.mathLikes(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getLikes()) + 1))
                    );
                    models.get(position).setLiked(1);
                }
            }
            if (info.equals("dislike")) {
                if (models.get(position).getLiked() == 1) {
                    like.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_black));
                    countLike.setText(
                            CountData.mathLikes(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getLikes()) - 1))
                    );
                    countLike.setTextColor(Color.parseColor("#000000"));
                    adapter.getmWallModels().get(position).setLikes(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getLikes()) - 1));
                    models.get(position).setLiked(0);
                }
            }
            like.startAnimation(AnimationUtils.loadAnimation(getContext(), R.animator.animation_scale_like));
        }

        if (info.equals("save") || info.equals("unsave")) {
            if (info.equals("save")) {
                if (models.get(position).getIsSaved() != 1) {
                    save.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_full_black));
                    countSaved.setText(
                            CountData.mathLikes(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getSaved()) + 1))
                    );
                    countSaved.setTextColor(Color.parseColor("#ffffff"));
                    adapter.getmWallModels().get(position).setSaved(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getSaved()) + 1));
                    models.get(position).setIsSaved(1);
                }
            }
            if (info.equals("unsave")) {
                if (models.get(position).getIsSaved() == 1) {
                    save.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_black));
                    countSaved.setText(
                            CountData.mathLikes(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getSaved()) - 1))
                    );
                    countSaved.setTextColor(Color.parseColor("#000000"));
                    adapter.getmWallModels().get(position).setSaved(String.valueOf(Integer.valueOf(adapter.getmWallModels().get(position).getSaved()) - 1));
                    models.get(position).setIsSaved(0);
                }
            }
            save.startAnimation(AnimationUtils.loadAnimation(getContext(), R.animator.animation_scale_like));
        }
    }

    @Override
    public void onFailure(String message) {
        mBinding.refresh.setRefreshing(false);
    }
}
