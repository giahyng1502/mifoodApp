package com.fpoly.nhom2.mifoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import FPT.PRO1122.Nhom3.DuAn1.adapter.AdapterFavorite;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityListMonAnBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.Catagory;
import FPT.PRO1122.Nhom3.DuAn1.model.Foods;

public class ListMonAn extends AppCompatActivity {
    ActivityListMonAnBinding binding;
    private RecyclerView.Adapter adapterFoodListView;
    Catagory catagory;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListMonAnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        getIntentExtra();
        initList();
        setVariable();
    }

    private void setVariable() {
    }


    private void initList() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBarFoods.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        Query query;
            query = myRef.orderByChild("categoryId").equalTo(catagory.getId());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        list.add(issue.getValue(Foods.class));
                    }
                    if (!list.isEmpty()){
                        binding.foodListView.setLayoutManager(new GridLayoutManager(ListMonAn.this, 1));
                        adapterFoodListView = new AdapterFavorite(list);
                        binding.foodListView.setAdapter(adapterFoodListView);
                    }
                    binding.progressBarFoods.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getIntentExtra() {
        Intent intent = getIntent();
        catagory = (Catagory) intent.getSerializableExtra("Category");

        assert catagory != null;
        binding.titleCategoryTxt.setText(catagory.getName());

        binding.backBtn.setOnClickListener(v -> startActivity(new Intent(ListMonAn.this, MainActivity.class)));
    }
}