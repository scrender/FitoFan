package com.example.alex.fitofan.ui.activity.forgot_password;

import android.content.Context;

public interface ForgotPasswordContact {

    interface View {
        Context getContext();

    }

    interface EventListener{
        void onContinue(int count);
    }

}
