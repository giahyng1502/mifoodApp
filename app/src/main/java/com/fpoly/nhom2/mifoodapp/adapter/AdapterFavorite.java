package com.fpoly.nhom2.mifoodapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

import FPT.PRO1122.Nhom3.DuAn1.Activity.ChiTietMonAn;
import FPT.PRO1122.Nhom3.DuAn1.Activity.MainActivity;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.model.Cart;
import FPT.PRO1122.Nhom3.DuAn1.model.Foods;

public class AdapterFavorite extends RecyclerView.Adapter<AdapterFavorite.ViewHolder> {
    List<Foods> items;
    Context context;

    public AdapterFavorite(List<Foods> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_list_mon_an, parent, false);
        return new ViewHolder(inflate);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NumberFormat vietnameseCurrencyFormat = NumberFormat.getCurrencyInstance();
        vietnameseCurrencyFormat.setMaximumFractionDigits(0);
        vietnameseCurrencyFormat.setCurrency(Currency.getInstance("VND"));
        holder.titleTxt.setText(items.get(position).getTitle());
        double priceFormated = items.get(position).getPrice();
        String formattedPrice = vietnameseCurrencyFormat.format(priceFormated);
        holder.priceTxt.setText(formattedPrice);
        holder.starTxt.setText("" + items.get(position).getStar());

        Glide.with(context).load(items.get(position).getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .thumbnail(Glide.with(holder.itemView.getContext()).load(R.drawable.loading_image))
                .fitCenter()
                .into(holder.foodsImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietMonAn.class);
            intent.putExtra("object", items.get(position));
            context.startActivity(intent);
        });
        holder.btnAddCart.setOnClickListener(v -> {
            Foods object = items.get(position);
            Cart cartItem = new Cart();
            cartItem.setTitle(object.getTitle());
            cartItem.setCartId(object.getId() + "");
            cartItem.setPrice(object.getPrice());
            cartItem.setTotal(object.getPrice());
            cartItem.setImagePath(object.getImagePath());
            cartItem.setQuantity(1);
            cartItem.setFoodID(object.getId());
            addToCart(cartItem);
        });
    }

    private void addToCart(Cart cart) {
        // Khởi tạo FirebaseDatabase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Tham chiếu đến nút 'Carts' trong Realtime Database
        DatabaseReference cartRef = database.getReference("Carts").child(MainActivity.id);

        // Sử dụng addListenerForSingleValueEvent để đọc dữ liệu một lần
        cartRef.child(cart.getCartId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Nếu món ăn đã tồn tại trong giỏ hàng, cập nhật số lượng
                            Cart existingCartItem = dataSnapshot.getValue(Cart.class);
                            if (existingCartItem != null) {
                                int currentQuantity = existingCartItem.getQuantity();
                                int newQuantity = currentQuantity + cart.getQuantity();
                                double newTotal = existingCartItem.getPrice() * newQuantity;

                                // Cập nhật số lượng và tổng tiền
                                cartRef.child(cart.getCartId()).child("quantity").setValue(newQuantity);
                                cartRef.child(cart.getCartId()).child("total").setValue(newTotal)
                                        .addOnSuccessListener(aVoid -> {
                                            // Xử lý khi cập nhật thành công
                                            Toast.makeText(context, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Xử lý khi có lỗi
                                            Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // Nếu món ăn chưa tồn tại trong giỏ hàng, thêm mới
                            cartRef.child(cart.getCartId()).setValue(cart)
                                    .addOnSuccessListener(aVoid -> {
                                        // Xử lý khi thêm thành công
                                        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi có lỗi
                                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý khi có lỗi truy vấn dữ liệu
                        Toast.makeText(context, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, priceTxt, starTxt, timeTxt, btnAddCart;
        ImageView foodsImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleListMonTxt);
            timeTxt = itemView.findViewById(R.id.timeListMonTxt);
            starTxt = itemView.findViewById(R.id.starListMonTxt);
            priceTxt = itemView.findViewById(R.id.priceListMonTxt);
            foodsImage = itemView.findViewById(R.id.foodsListMonImage);
            btnAddCart = itemView.findViewById(R.id.btnAddCart);
        }
    }
}
