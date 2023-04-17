package com.techvision.aipos;


import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.techvision.aipos.activity.IntroActivity;

public class MainApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("LCCC", "启动软件main! ");

        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
