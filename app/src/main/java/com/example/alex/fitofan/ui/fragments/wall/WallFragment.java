package com.example.alex.fitofan.ui.fragments.wall;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.fitofan.R;
import com.example.alex.fitofan.client.Request;
import com.example.alex.fitofan.databinding.FragmentWallBinding;
import com.example.alex.fitofan.interfaces.ILoadingStatus;
import com.example.alex.fitofan.models.GetUserModel;
import com.example.alex.fitofan.models.WallModel;
import com.example.alex.fitofan.settings.MSharedPreferences;
import com.example.alex.fitofan.ui.activity.create_plan.CreatePlanActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class WallFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ILoadingStatus<String> {

    FragmentWallBinding mBinding;
    private View view;
    private RecyclerAdapterWall adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_wall, container, false);

        mBinding = DataBindingUtil.bind(view);

        return view;
    }

    @Override
    public void onStart() {
        initListeners();
        initRecyclerView();
        super.onStart();
    }

    private void initListeners() {
        mBinding.refresh.setOnRefreshListener(this);

        mBinding.fabAddTraining.setOnClickListener(view1 -> startActivity(new Intent(getContext(), CreatePlanActivity.class)));
    }

    private void initRecyclerView() {

        //Test
        ArrayList<WallModel> models = new ArrayList<>();

        WallModel model1 = new WallModel();
        model1.setImageTraining("http://www.navolne.life/images/post/251769-1510217108.jpg");
        WallModel model2 = new WallModel();
        model2.setImageTraining("https://cdn.allwallpaper.in/wallpapers/1440x1080/13558/men-white-and-black-1440x1080-wallpaper.jpg");
        WallModel model3 = new WallModel();
        model3.setImageTraining("https://img2.goodfon.ru/wallpaper/big/8/ad/tayskiy-boks-devushka.jpg");
        WallModel model4 = new WallModel();
        model4.setImageTraining("http://fonday.ru/images/tmp/16/5/original/16554chTAqVPufiXUGvHrDjmwOg.jpg");
        WallModel model5 = new WallModel();
        model5.setImageTraining("http://kakbegat.com/uploads/images/trenirovka-tabata_1.jpg");
        WallModel model6 = new WallModel();
        model6.setImageTraining("https://wallpaperscraft.com/image/bodybuilding_bodybuilder_muscle_rod_113419_2048x1152.jpg");

        models.add(model1);
        models.add(model2);
        models.add(model3);
        models.add(model4);
        models.add(model5);
        models.add(model6);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mBinding.rvWall.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerAdapterWall(models, this);
        mBinding.rvWall.setAdapter(adapter);
        mBinding.rvWall.setNestedScrollingEnabled(true);
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        mBinding.refresh.setRefreshing(false);
        HashMap<String, String> map = new HashMap<>();
        map.put("plan_id", "65");
        map.put("uid", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getUid());
        map.put("signature", new Gson().fromJson(MSharedPreferences.getInstance().getUserInfo(), GetUserModel.class).getUser().getSignature());
        Request.getInstance().getPlan(map, this);
    }

    @Override
    public void onSuccess(String info) {

    }

    @Override
    public void onFailure(String message) {

    }
}
