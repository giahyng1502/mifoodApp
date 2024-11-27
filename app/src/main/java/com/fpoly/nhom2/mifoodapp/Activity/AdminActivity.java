package com.fpoly.nhom2.mifoodapp.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import FPT.PRO1122.Nhom3.DuAn1.Fragment.Admin.BannerManagement;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Admin.FoodManagement;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Admin.OrderManagement;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Admin.RevenueManager;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Admin.UserManagement;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityAdminBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.User;

public class AdminActivity extends AppCompatActivity {
    ActivityAdminBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getHeaderAdmin();

        // Thiết lập Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, new OrderManagement())
                .commit();

        // Thiết lập DrawerLayout

        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_user_management) {
                    fragment = new UserManagement();
                } else if (itemId == R.id.nav_food_management) {
                    fragment = new FoodManagement();
                } else if (itemId == R.id.nav_order_management) {
                    fragment = new OrderManagement();
                } else if (itemId == R.id.nav_banner_management) {
                    fragment = new BannerManagement();
                }else if (itemId == R.id.nav_revenue_management) {
                    fragment = new RevenueManager();
                }
                else if (itemId == R.id.nav_BackHome) {
                    finish();
                }
                // Thay thế Fragment hiện tại bằng Fragment mới
                if (fragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.nav_host_fragment, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }

                // Đóng Drawer sau khi chọn một mục
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void getHeaderAdmin() {
        View view = binding.navView.getHeaderView(0);
        TextView tvUserName,tvGmail;
        ImageView ivAvatar;
        tvUserName = view.findViewById(R.id.tvNameHeader);
        tvGmail = view.findViewById(R.id.tvMailHeader);
        ivAvatar = view.findViewById(R.id.ivHeaderAvatar);
        FirebaseDatabase.getInstance().getReference("users").child(MainActivity.id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user!= null) {
                                    tvGmail.setText(user.getEmail());
                                    tvUserName.setText(user.getName());
                                    Glide.with(AdminActivity.this).load(user.getImageAvatar()).into(ivAvatar);
                                }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}