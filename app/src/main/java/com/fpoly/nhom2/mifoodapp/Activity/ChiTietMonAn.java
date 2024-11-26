package com.fpoly.nhom2.mifoodapp.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Currency;

import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityChiTietMonAnBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.Cart;
import FPT.PRO1122.Nhom3.DuAn1.model.Foods;

@SuppressLint("SetTextI18n")
public class ChiTietMonAn extends AppCompatActivity {
    ActivityChiTietMonAnBinding binding;
    private Foods object;
    private  int num = 1;
    private long total = 0;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChiTietMonAnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        binding.numTxt.setText(num+"");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getIntentExtra();
        setVariable();
        // add data favorite Food
        favoriteFood();
    }

    private void favoriteFood() {
        // Đảm bảo rằng object không bị null
        if (object == null) {
            return;
        }

        // Khởi tạo DatabaseReference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Favorite").child(MainActivity.id).child(object.getId()+"");

        // Thiết lập OnClickListener cho nút yêu thích
        binding.favBtn.setOnClickListener(v -> {
            if (isFavorite) {
                // Nếu món ăn là món yêu thích -> bỏ yêu thích
                setFavorite(false);
                databaseReference.removeValue().addOnSuccessListener(unused -> {
                    // Đặt lại hình trái tim màu đỏ
                    binding.favBtn.setImageResource(R.drawable.favorite);
                    Toast.makeText(ChiTietMonAn.this, "Đã bỏ yêu thích " + object.getTitle() + " khỏi danh sách", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    // Xử lý lỗi nếu cần
                });
            } else {
                // Nếu món ăn chưa được yêu thích -> yêu thích
                setFavorite(true);
                databaseReference.setValue(object).addOnSuccessListener(unused -> {
                    // Đặt lại hình trái tim màu trắng đầy
                    binding.favBtn.setImageResource(R.drawable.favorite_select);
                    Toast.makeText(ChiTietMonAn.this, "Đã thêm " + object.getTitle() + " vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {

                });
            }
        });
    }

    private void getFavoriteUser(String userID,String foodID) {
        FirebaseDatabase.getInstance().getReference("Favorite")
                .child(userID).child(foodID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            binding.favBtn.setImageResource(R.drawable.favorite_select);
                            setFavorite(true);
                        } else {
                            binding.favBtn.setImageResource(R.drawable.favorite);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    private void setVariable() {
        NumberFormat vietnameseCurrencyFormat = NumberFormat.getCurrencyInstance();
        vietnameseCurrencyFormat.setMaximumFractionDigits(0);
        vietnameseCurrencyFormat.setCurrency(Currency.getInstance("VND"));

        binding.backBtn.setOnClickListener(v -> finish());

        Glide.with(ChiTietMonAn.this).load(object.getImagePath()).into(binding.pic);
        long price = (long) object.getPrice();
        binding.titleTxt.setText(object.getTitle());
        binding.ratingBar.setRating((float) object.getStar());
        binding.rateTxt.setText("" + object.getStar());
        String formattedPrice = vietnameseCurrencyFormat.format(price);
        binding.descriptionTxt.setText(object.getDescription());
        binding.totalTxt.setText(formattedPrice);

        //set status favorite
        getFavoriteUser(MainActivity.id, object.getId()+"");

        binding.plusBtn.setOnClickListener(v -> {
            num = num + 1;
            binding.numTxt.setText(num + "");

            total = (long) (num* object.getPrice());
            String totalString = vietnameseCurrencyFormat.format(total);
            binding.totalTxt.setText(totalString);
        });
        binding.minusBtn.setOnClickListener(v -> {

            if (num <= 1) {
                return;
            } else {
                num = num - 1;
            }
            binding.numTxt.setText(num + "");
            total = (long) (num*object.getPrice());
            String totalString = vietnameseCurrencyFormat.format(total);
            binding.totalTxt.setText(totalString);
        });

        // Khởi tạo DatabaseReference

        binding.AddToCartBtn.setOnClickListener(v -> {
            Cart cartItem = new Cart();
            cartItem.setTitle(object.getTitle());
            cartItem.setCartId(object.getId()+"");
            cartItem.setPrice(object.getPrice());
            cartItem.setTotal(total);
            cartItem.setImagePath(object.getImagePath());
            cartItem.setQuantity(num);
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
                                    Toast.makeText(ChiTietMonAn.this, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Xử lý khi có lỗi
                                    Toast.makeText(ChiTietMonAn.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // Nếu món ăn chưa tồn tại trong giỏ hàng, thêm mới
                    cartRef.child(cart.getCartId()).setValue(cart)
                            .addOnSuccessListener(aVoid -> {
                                // Xử lý khi thêm thành công
                                Toast.makeText(ChiTietMonAn.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Xử lý khi có lỗi
                                Toast.makeText(ChiTietMonAn.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi truy vấn dữ liệu
                Toast.makeText(ChiTietMonAn.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void getIntentExtra() {
        object = (Foods) getIntent().getSerializableExtra("object");
    }
}