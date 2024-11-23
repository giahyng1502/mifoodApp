package com.fpoly.nhom2.mifoodapp.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import FPT.PRO1122.Nhom3.DuAn1.Activity.MainActivity;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.adapter.AdapterFavorite;
import FPT.PRO1122.Nhom3.DuAn1.model.Foods;

@SuppressLint("NotifyDataSetChanged")
public class Favorite extends Fragment {
    RecyclerView recyclerView;
    AdapterFavorite adtFavorite;
    ArrayList<Foods> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_Favorite);
        list = new ArrayList<>();
        getBestFood();
        setBestFoodToView();
    }

    private void setBestFoodToView() {
        adtFavorite = new AdapterFavorite(list);
        StaggeredGridLayoutManager linearLayoutManager = new StaggeredGridLayoutManager(1
                , StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adtFavorite);
        adtFavorite.notifyDataSetChanged();
    }

    private void getBestFood() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Favorite").child(MainActivity.id);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Clear the list before adding new data
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Foods monAn = issue.getValue(Foods.class);
                        if (monAn != null) {
                            list.add(monAn);
                        }
                    }
                }
                // Always notify the adapter about data changes
                adtFavorite.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "fail" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}