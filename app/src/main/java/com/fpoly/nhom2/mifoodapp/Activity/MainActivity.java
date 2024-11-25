package com.fpoly.nhom2.mifoodapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Favorite;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Home;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.MyOrder;
import FPT.PRO1122.Nhom3.DuAn1.R;

public class MainActivity extends AppCompatActivity {
    Fragment fragment;
    BottomNavigationView bottomNavigationItemView;
    FloatingActionButton btnCart;
    public static String id;
    public static int idFrament = R.id.home;
    public static int role = 1;
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler handler = new Handler();
    private final Runnable resetDoubleBackFlag = () -> doubleBackToExitPressedOnce = false;
    Dialogs dialogs;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // neu true thi thoat
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Nhấn trở về lần nữa để thoát ứng dụng", Toast.LENGTH_SHORT).show();

        handler.postDelayed(resetDoubleBackFlag, 2000); // Đặt lại cờ sau 2 giây
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(resetDoubleBackFlag); // Khi Activity bị hủy, xóa callback để tránh rò rỉ bộ nhớ.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        id = getIDCurrentAccount();
        getUserCurrent();
        anhXa();
        setBottomNavigation();
        //
        bottomNavigationItemView.setBackground(null);
        //
        if (idFrament == R.id.home) {
            bottomNavigationItemView.setSelectedItemId(idFrament);
        } else {
            bottomNavigationItemView.setSelectedItemId(idFrament);
            idFrament = R.id.home;
        }
        dialogs = new Dialogs();
        dialogs.showProgressBar(this);
        dialogs.show();
    }
    public String getIDCurrentAccount() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserID", MODE_PRIVATE);
        return sharedPreferences.getString("id", "");
    }

    private void getUserCurrent() {
        FirebaseDatabase.getInstance().getReference("users")
                .child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            getRole(snapshot.child("role").getValue(Integer.class));
                            dialogs.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialogs.dismiss();
                        Toast.makeText(MainActivity.this, "get role fail "+error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void getRole(int role) {
        this.role = role;
    }

    private void setBottomNavigation() {
        // chuyen đến fragment giỏ hàng
        btnCart.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CartActivity.class)));

        // khi click sẽ mở fragment tương ứng
        bottomNavigationItemView.setOnItemSelectedListener(item -> {
            if (R.id.home == item.getItemId()) {
                fragment = new Home();
            } else if (R.id.favorite == item.getItemId()) {
                fragment = new Favorite();
            }else if (R.id.myOrder == item.getItemId()) {
                fragment = new MyOrder();
            }else if (R.id.profile == item.getItemId()) {
                startActivity(new Intent(MainActivity.this, Profile.class));
            }
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.frameLayout,fragment).
                        commit();
            }
            return true;
        });
    }

    private void anhXa() {
        bottomNavigationItemView = findViewById(R.id.bottomNavigationView);
        btnCart = findViewById(R.id.btnCart);
    }
}