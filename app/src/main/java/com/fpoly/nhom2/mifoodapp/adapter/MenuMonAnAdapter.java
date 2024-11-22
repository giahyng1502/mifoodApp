package com.fpoly.nhom2.mifoodapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import FPT.PRO1122.Nhom3.DuAn1.Activity.ListMonAn;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.model.Catagory;

@SuppressLint("RecyclerView")
public class MenuMonAnAdapter extends RecyclerView.Adapter<MenuMonAnAdapter.ViewHolder> {
    ArrayList<Catagory> items;
    Context context;

    public MenuMonAnAdapter(ArrayList<Catagory> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_categories, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        Catagory category = items.get(i);
        holder.catNameTxt.setText(category.getName());

        switch (i){
            case 0:
            case 4: {
                holder.imgCat.setBackgroundResource(R.drawable.cat_8_background);
                break;
            }
            case 1:{
                holder.imgCat.setBackgroundResource(R.drawable.cat_1_background);
                break;
            }
            case 2:
            case 7: {
                holder.imgCat.setBackgroundResource(R.drawable.cat_2_background);
                break;
            }
            case 3:{
                holder.imgCat.setBackgroundResource(R.drawable.cat_4_background);
                break;
            }
            case 5:{
                holder.imgCat.setBackgroundResource(R.drawable.cat_5_background);
                break;
            }
            case 6:{
                holder.imgCat.setBackgroundResource(R.drawable.cat_7_background);
                break;
            }
        }

        int drawableResourceId = context.getResources().getIdentifier(
                category.getImagePath()
                        , "drawable"
                        , holder.itemView.getContext().getPackageName()
                );

        Glide.with(context)
                .load(drawableResourceId)
                .thumbnail(Glide.with(holder.itemView.getContext()).load(R.drawable.loading_image))
                .fitCenter()
                .into(holder.imgCat);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Catagory catagory = items.get(i);
                Intent intent = new Intent(context, ListMonAn.class);
                intent.putExtra("Category", catagory);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView catNameTxt;
        ImageView imgCat;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCat = itemView.findViewById(R.id.imgCat);
            catNameTxt = itemView.findViewById(R.id.catNameTxt);
        }
    }
}
