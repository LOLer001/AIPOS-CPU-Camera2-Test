package com.techvision.aipos.adapter;

import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.techvision.aipos.HwAIposNative;
import com.techvision.aipos.NameListResult;
import com.techvision.aipos.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.com.techvision.ai_pos_core_aidl.DataBaseResponse;
import cn.com.techvision.ai_pos_core_aidl.IDeviceAPI;

public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.ViewHolder>{

    private DataBaseResponse mNameListResult;
    private List<String> nameList = new ArrayList();
    private List<Integer> cntList = new ArrayList();
    private IDeviceAPI mService;

    public  ManagerAdapter(DataBaseResponse nameListResult, IDeviceAPI mService){
        mNameListResult = nameListResult;
        this.mService = mService;
        for (int i = 0; i < mNameListResult.featureList.size(); i++) {
            cntList.add(mNameListResult.featureList.get(i).count);
            nameList.add(mNameListResult.featureList.get(i).name);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_features, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),holder.tvName.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.tvCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),holder.tvCount.getText(),Toast.LENGTH_SHORT).show();
            }
        });
        holder.tvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mService.delete(holder.tvName.getText().toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                nameList.remove(holder.getAdapterPosition());
                cntList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                notifyItemRangeChanged(holder.getAdapterPosition(), getItemCount());
                Toast.makeText(view.getContext(),"删除"+holder.tvName.getText().toString(),Toast.LENGTH_SHORT).show();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name  = nameList.get(position);
        int cn = Integer.parseInt(cntList.get(position).toString());
        holder.tvName.setText(name);
        holder.tvCount.setText(cn+"");
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvDel;
        TextView tvName;
        TextView tvCount;
        public ViewHolder(View view){
            super(view);
            tvDel = (TextView) view.findViewById(R.id.tv_del);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvCount = (TextView) view.findViewById(R.id.tv_count);
        }
    }
}
