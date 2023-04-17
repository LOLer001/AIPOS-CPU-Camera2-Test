package com.techvision.aipos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.techvision.aipos.activity.IntroActivity;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        XXPermissions.with(MainActivity.this)
                .permission(Permission.CAMERA)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request((permissions, all) -> {
                    startActivity(new Intent(MainActivity.this, IntroActivity.class));
                    finish();
                });
    }


}