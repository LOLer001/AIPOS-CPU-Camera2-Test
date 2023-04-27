package com.techvision.aipos.activity;

import static com.techvision.aipos.activity.IntroActivity.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.techvision.aipos.HwAIposNative;
import com.techvision.aipos.NameListResult;
import com.techvision.aipos.PerformResult;
import com.techvision.aipos.R;
import com.techvision.aipos.adapter.ManagerAdapter;
import com.techvision.aipos.databinding.LayoutManagerBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import cn.com.techvision.ai_pos_core_aidl.DataBaseResponse;
import cn.com.techvision.ai_pos_core_aidl.IDeviceAPI;
import cn.com.techvision.ai_pos_core_aidl.IdentifyResponse;
import cn.com.techvision.ai_pos_core_aidl.NormalResponse;
import cn.com.techvision.ai_pos_core_aidl.entity.AIPosStatus;

public class ManagerActivity extends BaseActivity {

    private TextView mBtnAdd;
    private TextView mBtnShow;
    private TextView mBtnThreshold;
    public TextView mBtnRebootDevice;
    public TextView mBtnResetDevice;
    private TextView mBtnAuth;
    public static int wipeFlag = 0;
    private NameListResult nameListResult;
    private Process mProcess;
    private TextView mBtnTest;
    private TextView mBtnSetFreeRatio;
    byte[] data;
    IDeviceAPI mService = null;
    private String feedName;
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        mBtnAdd = (TextView) findViewById(R.id.btn_add);
        mBtnShow = (TextView) findViewById(R.id.btn_show);
        mBtnThreshold = (TextView) findViewById(R.id.btn_threshold);
        mBtnRebootDevice = (TextView) findViewById(R.id.btn_reboot_device);
        mBtnResetDevice = (TextView) findViewById(R.id.btn_reset_device);
        mBtnAuth = (TextView) findViewById(R.id.btn_auth);
        mBtnTest = (TextView) findViewById(R.id.btn_test);
        mBtnSetFreeRatio = (TextView) findViewById(R.id.btn_setfreeratio);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.setfree);

        EditText editText1 = dialog.findViewById(R.id.editText1);
        EditText editText2 = dialog.findViewById(R.id.editText2);
        EditText editText3 = dialog.findViewById(R.id.editText3);
        EditText editText4 = dialog.findViewById(R.id.editText4);

        Button btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int left = Integer.parseInt(editText1.getText().toString());
                int top = Integer.parseInt(editText2.getText().toString());
                int width = Integer.parseInt(editText3.getText().toString());
                int height = Integer.parseInt(editText4.getText().toString());
                try {
                    mService.setRectangularArea(left,top,width,height);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        tryBindService();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                retQueue.take();
                Log.d("LCCC", "Managee onCreate");
//                NormalResponse authResponse = mService.activateAuthorization();
//                Log.d("LCCC", "authResponse: " + authResponse.message);
//                if(authResponse.status == AIPosStatus.Success){
//                    NormalResponse initResponse = mService.initAPI();
//                    Log.d("LCCC", "initResponse: " + initResponse.message);
//                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        mBtnSetFreeRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        mBtnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    try {
                        long time = SystemClock.elapsedRealtime();
                        Log.d("LCCC2", "onCreate: 调用Perform");
                        IdentifyResponse identifyResponse = mService.identify();
                        Log.d("LCCC2", "onCreate: 调用完成 " + identifyResponse.message + " " + (SystemClock.elapsedRealtime() - time));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });


        mBtnThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputServer = new EditText(ManagerActivity.this);
                inputServer.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
                AlertDialog.Builder builder = new AlertDialog.Builder(ManagerActivity.this);
                builder.setTitle("设置识别阈值数:").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                        .setNegativeButton("取消", null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String threshold = inputServer.getText().toString();
                        if (threshold != null && !threshold.isEmpty()) {
                            NormalResponse ret = null;
                            try {
                                ret = mService.setIdentifyThreshold(Integer.parseInt(threshold));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            if (ret.status != AIPosStatus.Success) {
                                TipDialog.show("设置失败,请检查配置");
                            } else {
                                Toast.makeText(ManagerActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ManagerActivity.this, "阈值数为空", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            }
        });
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManagerActivity.this, RTSPActivity.class));
            }
        });
        mBtnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doQueryDatabaseInfo();
            }
        });
        mBtnRebootDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        Log.d("LCCC", "reboot: " + mService.reboot().message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        mBtnResetDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wipeFlag == 0) {
                    TipDialog.show("恢复出厂设置会清除重要数据，请慎重考虑！");
                    wipeFlag = 1;
                } else if (wipeFlag == 1) {
                    Log.d("LCCC", "mBtnResetDevice: ");
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            Log.d("LCCC", "resetFactory: " + mService.resetFactory().message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    });
                    wipeFlag = 0;
                }
            }
        });

        mBtnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d("LCCC", "activateAuthorization: " + mService.activateAuthorization().message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String exec(String command) {

        Process process = null;
        BufferedReader reader = null;
        InputStreamReader is = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("su");
            is = new InputStreamReader(process.getInputStream());
            reader = new BufferedReader(is);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            process.waitFor();
            return output.toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }

                if (is != null) {
                    is.close();
                }

                if (reader != null) {
                    reader.close();
                }

                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doQueryDatabaseInfo() {
        WaitDialog.show("正在查询");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DataBaseResponse dataBaseResponse = mService.queryDatabase();
                Log.d("LCCC", "queryDatabase: " + dataBaseResponse.message);
                if (dataBaseResponse.status == AIPosStatus.Success) {
                    runOnUiThread(() -> {
                        handleDatabaseInfo(dataBaseResponse);
                    });
                    for (int i = 0; i < dataBaseResponse.featureCount; i++) {
                        Log.d("LCCC", "queryDatabase: " + dataBaseResponse.featureList.get(i).name + " " + dataBaseResponse.featureList.get(i).count);
                    }
                    Log.d("LCCC", "queryDatabase: " + dataBaseResponse.message + "  " + dataBaseResponse.threshold + "  " + dataBaseResponse.featureCount);
                    WaitDialog.dismiss();
                } else {
                    WaitDialog.dismiss();
                    Log.d("LCCC", "查询结果为空！");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleDatabaseInfo(DataBaseResponse dataBaseResponse) {
        NestedScrollView nestedScrollView = (NestedScrollView) getLayoutInflater().inflate(R.layout.layout_manager, null);
        RecyclerView recyclerView = (RecyclerView) nestedScrollView.findViewById(R.id.rv_list);
        TextView tvEnable = (TextView) nestedScrollView.findViewById(R.id.tv_enable);
        TextView tvCount = (TextView) nestedScrollView.findViewById(R.id.tv_count);
        TextView tvThreshold = (TextView) nestedScrollView.findViewById(R.id.tv_threshold);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());  //线性布局
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        ManagerAdapter adapter = new ManagerAdapter(dataBaseResponse,mService);
        tvEnable.setText("true");
        tvCount.setText(String.valueOf(dataBaseResponse.featureCount));
        tvThreshold.setText(String.valueOf(dataBaseResponse.threshold));
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(ManagerActivity.this)
                .setTitle("自主学习识别库管理")
                .setCancelable(false)
                .setView(nestedScrollView)
                .setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ManagerActivity.this, "已关闭!", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        dialog.show();
    }
}