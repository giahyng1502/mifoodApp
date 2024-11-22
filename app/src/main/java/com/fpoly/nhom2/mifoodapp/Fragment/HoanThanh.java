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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.adapter.OrderHistoryAdapter;
import FPT.PRO1122.Nhom3.DuAn1.model.Order;

public class HoanThanh extends Fragment {
    private OrderHistoryAdapter adapter;
    private ArrayList<Order> orderList;
    private RecyclerView recyclerViewOrderHistory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hoan_thanh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewOrderHistory = view.findViewById(R.id.recycler_viewHoanThanh);
        orderList = new ArrayList<>();
        setupRecyclerView();
        loadOrderHistory();
    }

    private void setupRecyclerView() {
        recyclerViewOrderHistory.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new OrderHistoryAdapter(requireActivity(), orderList);
        recyclerViewOrderHistory.setAdapter(adapter);
    }

    private void loadOrderHistory() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Duyệt qua từng đơn hàng
                    for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                        // Chuyển đổi thành đối tượng OrderHistory
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null && order.getStatus() == 0) {
                            orderList.add(order);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
