package com.techvision.aipos.activity;

import static java.lang.Math.abs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.os.IBinder;
import android.os.RemoteException;
import android.text.InputFilter;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kongzue.dialogx.dialogs.TipDialog;
import com.techvision.aipos.HwAIposNative;
import com.techvision.aipos.PerformResult;
import com.techvision.aipos.QueueInfo;
import com.techvision.aipos.R;
import com.techvision.aipos.adapter.PosAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.techvision.aipos.entity.preset.PresetItem;
import com.techvision.aipos.util.DrawView;
import com.techvision.aipos.util.ImageFormat;

import cn.com.techvision.ai_pos_core_aidl.AspectFreeRatioResponse;
import cn.com.techvision.ai_pos_core_aidl.IDeviceAPI;
import cn.com.techvision.ai_pos_core_aidl.IdentifyResponse;
import cn.com.techvision.ai_pos_core_aidl.JpegImageResponse;
import cn.com.techvision.ai_pos_core_aidl.NormalResponse;
import cn.com.techvision.ai_pos_core_aidl.RectangularAreaResponse;
import cn.com.techvision.ai_pos_core_aidl.entity.AIPosStatus;
import cn.com.techvision.aipos.entity.ui.PosEntity;

public class PosActivity extends BaseActivity {

    private PerformResult ret;
    public static String conret;
    private TextView mBtnClean;
    private TextView mBtnConfirm;
    private DrawView drawView;
    private RecyclerView recyclerView;
    //    private TextureView mTexPreview = (TextureView)findViewById(R.id.tex_preview);
    private TextView mMaskLogo;
    private static ImageView mIvItem;
    private static TextView mTvGoodName;
//    private RecyclerView mRvItem = (RecyclerView)findViewById(R.id.rv_item);
    private TextView mTvState;
    //    private TextView mTvPriceAll = (TextView)findViewById(R.id.tv_price_all);
//    private TextView mTvPrice = (TextView)findViewById(R.id.tv_price);
//    private TextView mTvWeightGood = (TextView)findViewById(R.id.tv_weight_good);
//    private TextView mTvWeightAll = (TextView)findViewById(R.id.tv_weight_all);
    private TextView mTvOpenRtsp;
    private int data0;

    private ImageView mImage;
    private String testName = null;
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
        Log.d("LCCC", "onCreate: ");
        setContentView(R.layout.activity_pos);
        mBtnConfirm = (TextView) findViewById(R.id.btn_confirm);
        mTvState = (TextView) findViewById(R.id.tv_state);
        mImage = (ImageView) findViewById(R.id.sv_recording);
        mTvOpenRtsp = (TextView) findViewById(R.id.tv_open_rtsp);
        mTvOpenRtsp.setVisibility(View.GONE);
        mBtnClean = (TextView) findViewById(R.id.btn_clean);
        recyclerView = (RecyclerView) findViewById(R.id.rv_item);
        mIvItem = (ImageView)findViewById(R.id.iv_item_main);
        mMaskLogo = (TextView)findViewById(R.id.mask_logo);
        mTvGoodName = (TextView)findViewById(R.id.tv_good_name);
        drawView = (DrawView) findViewById(R.id.drawview);

        tryBindService();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                retQueue.take();
                Log.d("LCCC", "Pos onCreate");
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

        mBtnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView.getAdapter() != null){
                    recyclerView.setAdapter(null);
                    ret = null;
                }
                mMaskLogo.setVisibility(View.VISIBLE);
                mTvGoodName.setText("物品代号");
                data0 = 0;
            }
        });

        mTvState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if(mTvOpenRtsp.getVisibility() == View.VISIBLE) {
                        TipDialog.show("请先打开相机预览");
                    }else {
                        mBtnClean.performClick();
                        Log.d("LCCC", "onClickmTvState: ");
                        Executors.newSingleThreadExecutor().execute(()->{
                            try {
                                Log.d("LCCCtest", "onClick: identify");
                                IdentifyResponse identifyResponse = mService.identify();
                                Log.d("LCCCtest", "onClick: identify "+ identifyResponse.message);
                                if(identifyResponse.status == AIPosStatus.Success) {
                                    if(identifyResponse.identifyList != null) {
                                        if (!identifyResponse.identifyList.isEmpty()) {
                                            Log.d("LCCCtest", "yes!!!");
                                            data0 = 1;
                                            feedName = identifyResponse.identifyList.get(0).name;
                                            Log.d("LCCC", "identify: " + identifyResponse.message);
                                            List<PosEntity> entities = new ArrayList<>();
                                            for (int i = 0; i < identifyResponse.identifyList.size(); i++) {
                                                Log.d("LCCC", "identify: " + identifyResponse.identifyList.get(i).name + " " + identifyResponse.identifyList.get(i).probability);
                                                String name = identifyResponse.identifyList.get(i).name;
                                                float confidence = Float.parseFloat(identifyResponse.identifyList.get(i).probability);
                                                if (confidence < 0.001) {
                                                    confidence = 0.000F;
                                                }
                                                PresetItem item = PresetItem.Companion.getItemHashMap().get(name);
                                                if (item != null) {
                                                    PosEntity entity = new PosEntity();
                                                    entity.setIds(item.getIds());
                                                    entity.setName(name);
                                                    entity.setUiName(item.getUiName());
                                                    entity.setProbability(String.valueOf(confidence));
                                                    entities.add(entity);
                                                } else {
                                                    PosEntity entity = new PosEntity();
                                                    entity.setName(name);
                                                    entity.setProbability(String.valueOf(confidence));
                                                    entities.add(entity);
                                                }
                                                runOnUiThread(() -> {
                                                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_item);
                                                    GridLayoutManager gridLayoutManager = new GridLayoutManager(PosActivity.this, 4);  //网格布局
                                                    recyclerView.setLayoutManager(gridLayoutManager);
                                                    PosAdapter adapter = new PosAdapter(entities);
                                                    recyclerView.setAdapter(adapter);
                                                });
                                            }
                                        }
                                    }
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        });
                        Executors.newSingleThreadExecutor().execute(()->{
                            try {
                                JpegImageResponse jpegImageResponse = mService.getPreviewFrame();
                                if(jpegImageResponse.status == AIPosStatus.Success) {
                                    Log.d("LCCC", "getPreviewFrame: " + jpegImageResponse.message + " " + jpegImageResponse.jpegImage.length);
                                    runOnUiThread(()->{
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 1;
                                        Bitmap bmp = BitmapFactory.decodeByteArray(jpegImageResponse.jpegImage, 0,jpegImageResponse.jpegImage.length,options);
                                        mImage.setImageBitmap(bmp);
                                        mIvItem.setImageBitmap(bmp);
                                        mMaskLogo.setVisibility(View.GONE);
                                    });
                                }
                            }catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        });
                        try {
                            RectangularAreaResponse rectangularAreaResponse = mService.getRectangularArea();
                            float x1 = (float) rectangularAreaResponse.left/640;
                            float y1 = (float) rectangularAreaResponse.top/320;
                            float x2 = x1+(float) rectangularAreaResponse.width/640;
                            float y2 = y1+(float) rectangularAreaResponse.height/320;
                            drawView.setPoints(x1,y1, x2,y2);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(conret == null && ret == null){
                    return;
                } else if(conret == null) {
                    conret = ret.category[0];
                }
                Executors.newSingleThreadExecutor().execute(()->{
                    try {
                        Log.d("LCCC", "feedback: " + mService.feedback("0",conret).message);
                        conret = null;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
                Log.d("LCCC", "onClickcon: " + conret);
            }
        });
        mTvOpenRtsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTvOpenRtsp.setVisibility(View.GONE);
                Log.d("LCCC", "onClick: " + "open rtsp");
            }
        });

        mTvGoodName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data0 == 0 ){
                    Toast.makeText(PosActivity.this,"物品图片为空!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mTvGoodName.getText().toString().equals("物品代号")) {
                    final EditText inputServer = new EditText(PosActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(PosActivity.this);
                    builder.setTitle("新增识别:").setMessage("请输入物品代号:").setView(inputServer)
                            .setNegativeButton("关闭", null);
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String name = inputServer.getText().toString();
                            if (name != null && !name.isEmpty() && name.length() > 2) {
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    try {
                                        NormalResponse ret = mService.update(name);
                                        JpegImageResponse jpegImageResponse = mService.getPreviewFrame();
                                        if(jpegImageResponse.status == AIPosStatus.Success) {
                                            Log.d("LCCC", "getPreviewFrame: " + jpegImageResponse.message + " " + jpegImageResponse.jpegImage.length);
                                            runOnUiThread(()->{
                                                BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inSampleSize = 1;
                                                Bitmap bmp = BitmapFactory.decodeByteArray(jpegImageResponse.jpegImage, 0,jpegImageResponse.jpegImage.length,options);
                                                mImage.setImageBitmap(bmp);
                                            });
                                        }
                                        if (ret.status == AIPosStatus.Success) {
                                            Toast.makeText(PosActivity.this, "添加成功!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(PosActivity.this, "添加失败!", Toast.LENGTH_SHORT).show();
                                        }
                                        Log.d("LCCC", "upadate: " + ret.message);
                                        Toast.makeText(PosActivity.this, ret.message, Toast.LENGTH_SHORT).show();
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else if (name != null && !name.isEmpty() && name.length() <= 2) {
                                Toast.makeText(PosActivity.this, "请输入正确物品代号!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PosActivity.this, "物品代号为空!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.show();
                }
            }
        });
    }



    public static void setItem(String name, int ids) {
        mTvGoodName.setText(name);
        mIvItem.setImageResource(ids);
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