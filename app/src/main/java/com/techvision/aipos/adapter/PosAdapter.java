package com.techvision.aipos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techvision.aipos.HwAIposNative;
import com.techvision.aipos.NameListResult;
import com.techvision.aipos.PerformResult;
import com.techvision.aipos.R;
import com.techvision.aipos.activity.PosActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.com.techvision.aipos.entity.ui.PosEntity;

public class PosAdapter extends RecyclerView.Adapter<PosAdapter.ViewHolder>{
    private List<PosEntity> entities = new ArrayList<PosEntity>();

    public  PosAdapter(List<PosEntity> mentities ){
        entities = mentities;
    }

    @Override
    public PosAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
        final PosAdapter.ViewHolder holder = new PosAdapter.ViewHolder(view);
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PosActivity.conret = holder.tvName.getText().toString();
                Toast.makeText(view.getContext(),holder.tvName.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.tvProbability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PosActivity.conret = holder.tvName.getText().toString();
                Toast.makeText(view.getContext(),holder.tvProbability.getText(),Toast.LENGTH_SHORT).show();
            }
        });
        holder.ivItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PosActivity.conret = holder.tvName.getText().toString();
                Toast.makeText(view.getContext(),holder.tvName.getText(),Toast.LENGTH_SHORT).show();
                PosActivity.setItem(holder.tvName.getText().toString(),entities.get(holder.getAdapterPosition()).getIds());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(PosAdapter.ViewHolder holder, int position) {
        String name  = entities.get(position).getName();
        String confidence = entities.get(position).getProbability();

        holder.ivItem.setImageResource(entities.get(position).getIds());
        holder.tvName.setText(name);
        holder.tvProbability.setText(confidence);
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivItem;
        TextView tvName;
        TextView tvProbability;
        ImageView mIvItem;
        TextView mTvGoodName;

        public ViewHolder(View view){
            super(view);
            mIvItem = (ImageView)view.findViewById(R.id.iv_item_main);
            mTvGoodName = (TextView)view.findViewById(R.id.tv_good_name);
            ivItem = (ImageView) view.findViewById(R.id.iv_item);
            tvName = (TextView) view.findViewById(R.id.tv_name_item);
            tvProbability = (TextView) view.findViewById(R.id.tv_probability);
        }
    }
}
