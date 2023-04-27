package com.techvision.aipos.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.techvision.aipos.ActivityCollector;
import com.techvision.aipos.HwAIposNative;
import com.techvision.aipos.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import cn.com.techvision.ai_pos_core_aidl.IDeviceAPI;
import cn.com.techvision.ai_pos_core_aidl.NormalResponse;
import cn.com.techvision.ai_pos_core_aidl.entity.AIPosStatus;

public class IntroActivity extends BaseActivity {

    IDeviceAPI mService = null;
    LinkedBlockingQueue<Integer> retQueue = new LinkedBlockingQueue<Integer>();
    private final ServiceConnection scn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("LCCC", "onServiceConnected");
            mService = IDeviceAPI.Stub.asInterface(service);
            retQueue.add(1);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("LCCC", "onServiceDisconnected");
            mService = null;
            tryBindService();
        }
    };

    private void tryBindService() {
        Log.d("LCCC", "tryBindService");
        Intent intent = new Intent();
        intent.setPackage("cn.com.techvision.iot.aipos");
        intent.setAction("cn.com.techvision.action.BIND_AI_POS_SERVICE");
        bindService(intent, scn, Context.BIND_AUTO_CREATE);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Log.d("LCCC", "启动软件! ");

        tryBindService();
        Executors.newSingleThreadExecutor().execute(()->{
            try {
                retQueue.take();
                Log.d("LCCC", "onCreate");
                NormalResponse authResponse = mService.activateAuthorization();
                Log.d("LCCC", "authResponse: " + authResponse.message);
                if(authResponse.status == AIPosStatus.Success){
                    mService.startIOTService();
                    NormalResponse initResponse = mService.initAPI();
                    Log.d("LCCC", "initResponse: " + initResponse.message);
                    if(initResponse.status == AIPosStatus.Success){
                        mService.stopIOTService();
                    }
                }
            } catch (RemoteException | InterruptedException e) {
                e.printStackTrace();
            }
        });


        TextView mBtnManage = (TextView) findViewById(R.id.btn_manage);
        TextView mBtnPos = (TextView) findViewById(R.id.btn_pos);
        mBtnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IntroActivity.this, PosActivity.class));
            }
        });
        mBtnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(IntroActivity.this, ManagerActivity.class));
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mService.stopIOTService();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d("LCCC", "onDestroy: 111");
    }
}