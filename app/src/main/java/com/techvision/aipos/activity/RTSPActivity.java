package com.techvision.aipos.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kongzue.dialogx.dialogs.TipDialog;
import com.techvision.aipos.HwAIposNative;
import com.techvision.aipos.PerformResult;
import com.techvision.aipos.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import cn.com.techvision.ai_pos_core_aidl.IDeviceAPI;
import cn.com.techvision.ai_pos_core_aidl.JpegImageResponse;
import cn.com.techvision.ai_pos_core_aidl.NormalResponse;
import cn.com.techvision.ai_pos_core_aidl.entity.AIPosStatus;


public class RTSPActivity extends BaseActivity {

    private SurfaceView mSurfaceView;
    private TextView mBtnAdd;
    private EditText mEtName;
    private int lock = 0;
    private Timer timerStart;

    private ImageView mImage;
    private String testName = null;
    IDeviceAPI mService = null;
    private String updateName;
    private Handler uiHandler;
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
        setContentView(R.layout.activity_rtsp_vlc);
        mImage = (ImageView) findViewById(R.id.image);
        uiHandler = new Handler(getMainLooper());
        Log.d("LCCC", "mImage: " + mImage.getWidth() + " " +mImage.getHeight());
        tryBindService();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                retQueue.take();
                Log.d("LCCC", "RTSP onCreate");
//                NormalResponse authResponse = mService.activateAuthorization();
//                Log.d("LCCC", "authResponse: " + authResponse.message);
//                if(authResponse.status == AIPosStatus.Success){
//                    NormalResponse initResponse = mService.initAPI();
//                    Log.d("LCCC", "initResponse: " + initResponse.message);
//                }

                JpegImageResponse jpegImageResponse = mService.getPreviewFrame();
                if(jpegImageResponse.status == AIPosStatus.Success) {
                    Log.d("LCCC", "getPreviewFrame: " + jpegImageResponse.message + " " + jpegImageResponse.jpegImage.length);
                    uiHandler.post(()->{
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        Bitmap bmp = BitmapFactory.decodeByteArray(jpegImageResponse.jpegImage, 0,jpegImageResponse.jpegImage.length,options);
                        mImage.setImageBitmap(bmp);
                    });
                }
            } catch (InterruptedException | RemoteException e) {
                e.printStackTrace();
            }
        });
        mSurfaceView = (SurfaceView) findViewById(R.id.tv_rtsp);
        mBtnAdd = (TextView) findViewById(R.id.btn_add);
        mEtName = (EditText) findViewById(R.id.et_name);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateName = mEtName.getText().toString().replace(" ", "").trim();
                Log.d("LCC", "onClick: " + updateName + " " + updateName.length() + "  6666");
                if (updateName.length() <= 2) {
                    TipDialog.show("请输入正确的名称");
                } else {
//                        ret = HwAIposNative.CategoryAI_DBAddFeatureByName(data, size.width, size.height,name);
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            NormalResponse ret = mService.update(updateName);
                            JpegImageResponse jpegImageResponse = mService.getPreviewFrame();
                            if(jpegImageResponse.status == AIPosStatus.Success) {
                                Log.d("LCCC", "getPreviewFrame: " + jpegImageResponse.message + " " + jpegImageResponse.jpegImage.length);
                                uiHandler.post(()->{
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = 1;
                                    Bitmap bmp = BitmapFactory.decodeByteArray(jpegImageResponse.jpegImage, 0,jpegImageResponse.jpegImage.length,options);
                                    mImage.setImageBitmap(bmp);
                                });
                            }
                            if (ret.status == AIPosStatus.Success) {
                                Log.d("LCCC", "添加成功!");
                            } else {
                                Log.d("LCCC", "添加失败!");
                            }
                            Log.d("LCCC", "upadate: " + ret.message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}