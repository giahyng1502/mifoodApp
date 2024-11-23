package com.fpoly.nhom2.mifoodapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import FPT.PRO1122.Nhom3.DuAn1.R;

public class AdapterBanner extends RecyclerView.Adapter<AdapterBanner.ViewHolder> {
    List<String> list;
    Context context;

    public AdapterBanner( Context context,List<String> list) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String src = list.get(position);
        Glide.with(context)
                .load(src)
                .thumbnail(Glide.with(holder.itemView.getContext()).load(R.drawable.loading_image))
                .fitCenter()
                .into(holder.ivBanner);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivbanner);
        }
    }
}
