package com.example.alex.fitofan.interfaces;

import com.example.alex.fitofan.models.GetPlansModel;

public interface ILoadingStatusUserPlans {
    void onSuccess(GetPlansModel info);

    void onFailure(String message);
}
