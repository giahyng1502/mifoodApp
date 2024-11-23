package com.fpoly.nhom2.mifoodapp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.model.Cart;

@SuppressLint("SetTextI18n")
public class CheckOutAdapter extends RecyclerView.Adapter<CheckOutAdapter.ViewHolder> {
    ArrayList<Cart> list = new ArrayList<>();

    public CheckOutAdapter(ArrayList<Cart> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_thanhtoan_rec, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NumberFormat vietnameseCurrencyFormat = NumberFormat.getCurrencyInstance();
        vietnameseCurrencyFormat.setMaximumFractionDigits(0);
        vietnameseCurrencyFormat.setCurrency(Currency.getInstance("VND"));
        holder.titleTxtCheckout.setText(list.get(position).getTitle());
        holder.numTxtInCheckOut.setText(list.get(position).getQuantity() + "");
        double totalFormated = list.get(position).getTotal();
        String formattedTotal = vietnameseCurrencyFormat.format(totalFormated);
        holder.totalPriceTxtInCheckout.setText(formattedTotal);

        Glide.with(holder.itemView.getContext())
                .load(list.get(position).getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .thumbnail(Glide.with(holder.itemView.getContext()).load(R.drawable.loading_image))
                .fitCenter()
                .into(holder.picCheckout);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxtCheckout, totalPriceTxtInCheckout, numTxtInCheckOut;
        ImageView picCheckout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxtCheckout = itemView.findViewById(R.id.titleTxtCheckout);
            numTxtInCheckOut = itemView.findViewById(R.id.numTxtInCheckOut);
            totalPriceTxtInCheckout = itemView.findViewById(R.id.totalPriceTxtInCheckout);
            picCheckout = itemView.findViewById(R.id.picCheckout);
        }
    }
}
