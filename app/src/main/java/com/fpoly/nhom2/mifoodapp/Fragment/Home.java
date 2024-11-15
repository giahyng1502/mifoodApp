package com.fpoly.nhom2.mifoodapp.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import FPT.PRO1122.Nhom3.DuAn1.Activity.MainActivity;
import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Admin.FoodManagement;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.adapter.AdapterBanner;
import FPT.PRO1122.Nhom3.DuAn1.adapter.DoAnBanChayAdapter;
import FPT.PRO1122.Nhom3.DuAn1.adapter.MenuMonAnAdapter;
import FPT.PRO1122.Nhom3.DuAn1.databinding.FragmentHomeBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.Catagory;
import FPT.PRO1122.Nhom3.DuAn1.model.Foods;
import FPT.PRO1122.Nhom3.DuAn1.model.User;

public class Home extends Fragment {
    private FragmentHomeBinding binding;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    List<String> banners;
    AdapterBanner adapterBanner;
    int index = -1;
    DatabaseReference mFood;
    Dialogs dialogs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Khởi tạo các view
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dialogs = new Dialogs();
        dialogs.showProgressBar(requireContext());
        dialogs.show();

        mFood = FirebaseDatabase.getInstance().getReference("foods");
        binding.searchView.setOnClickListener(e -> goToTargetFragment());
        getBanner();
        setInformationCurrentUser();
        monAnBanChayRecyclerview();
        menuMonAn();
    }

    private void goToTargetFragment() {
        // Tạo instance của Fragment đích
        FoodManagement targetFragment = new FoodManagement();

        // Sử dụng FragmentManager và FragmentTransaction để chuyển đổi
        FragmentManager fragmentManager = getParentFragmentManager(); // Hoặc getActivity().getSupportFragmentManager() nếu dùng trong Activity
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Thay thế Fragment hiện tại bằng Fragment đích
        fragmentTransaction.replace(R.id.frameLayout, targetFragment);

        // Thêm vào back stack để có thể quay lại Fragment trước đó
        fragmentTransaction.addToBackStack(null);

        // Hoàn thành giao dịch
        fragmentTransaction.commit();
    }

    private void getBanner() {
        // lấy banner
        banners = new ArrayList<>();
        database.getReference("Banner").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    banners.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        banners.add(snapshot1.getValue(String.class));
                        dialogs.dismiss();
                    }
                    if (!banners.isEmpty()) {
                        setSlider();
                        dialogs.dismiss();
                    }
                }
                dialogs.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogs.dismiss();
                Toast.makeText(requireContext(), "error" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void menuMonAn() {
        DatabaseReference myRef = database.getReference("Category");
        ArrayList<Catagory> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Catagory catagory = issue.getValue(Catagory.class);
                        list.add(catagory);
                        dialogs.dismiss();
                    }
                    if (!list.isEmpty()) {
                        binding.recMenuMonAn.setLayoutManager(new GridLayoutManager(getContext(), 4));
                        RecyclerView.Adapter<MenuMonAnAdapter.ViewHolder> adapter = new MenuMonAnAdapter(list);
                        binding.recMenuMonAn.setAdapter(adapter);
                        dialogs.dismiss();
                    }
                }
                dialogs.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogs.dismiss();
                Toast.makeText(requireContext(), "error" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void monAnBanChayRecyclerview() {
        DatabaseReference myRef = database.getReference("Foods");
        ArrayList<Foods> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(Foods.class));
                        dialogs.dismiss();
                    }
                    if (!list.isEmpty()) {
                        //sap xep
                        list.sort((item1, item2) -> Double.compare(item2.getStar(), item1.getStar()));
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1, LinearLayoutManager.HORIZONTAL, false);
                        binding.recyclerFood.setLayoutManager(gridLayoutManager);
                        // top 5
                        List<Foods> top5Items = list.subList(0, Math.min(5, list.size()));
                        RecyclerView.Adapter<DoAnBanChayAdapter.ViewHolder> adapter = new DoAnBanChayAdapter(top5Items);
                        binding.recyclerFood.setAdapter(adapter);
                        dialogs.dismiss();
                    }
                }
                dialogs.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogs.dismiss();
                Toast.makeText(requireContext(), "error" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setInformationCurrentUser() {
        FirebaseDatabase.getInstance().getReference("users")
                .child(MainActivity.id).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                User user = snapshot.getValue(User.class);
                                assert user != null;
                                Glide.with(requireContext()).load(user.getImageAvatar())
                                        .thumbnail(Glide.with(requireContext()).load(R.drawable.loading_image))
                                        .fitCenter()
                                        .error(R.drawable.none_avatar)
                                        .into(binding.ivAvatarUserHome);
                                binding.tvNameUserHome.setText(user.getName() + " \uD83C\uDF3F");
                                dialogs.dismiss();
                            } catch (Exception e) {
                                dialogs.dismiss();
                                Toast.makeText(requireContext(), "fail" + e, Toast.LENGTH_SHORT).show();
                                Log.d("home 168", e + "");
                            }
                        }
                        dialogs.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialogs.dismiss();
                        Toast.makeText(requireContext(), "fail" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setSlider() {
        adapterBanner = new AdapterBanner(getContext(), banners);
        binding.viewPage2.setAdapter(adapterBanner);
        binding.viewPage2.setClipToPadding(false);
        binding.viewPage2.setClipChildren(false);
        binding.viewPage2.setOffscreenPageLimit(3);
        binding.viewPage2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        // tạo hiệu ứng khi vuốt
        binding.viewPage2.setPageTransformer((page, position) -> {
            float absolute = Math.abs(position);
            page.setScaleY(0.85f + absolute * 0.15f); // thu nhỏ theo trục y tạo hiệu ứng trượt
        });

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                next();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(runnable);
    }

    public void next() {
        if (index < 3) {
            index++;
            binding.viewPage2.setCurrentItem(index);
        } else {
            index = -1;
        }
    }
}