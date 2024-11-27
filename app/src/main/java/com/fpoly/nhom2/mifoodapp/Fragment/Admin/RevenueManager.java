package com.fpoly.nhom2.mifoodapp.Fragment.Admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.model.NumberToWordsConverter;
import FPT.PRO1122.Nhom3.DuAn1.model.Order;

public class RevenueManager extends Fragment {
    Button btnCheck;
    EditText edtTuNgay, edtDenNgay, edtDoanhThu;
    SimpleDateFormat sdf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_revenua_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnCheck = view.findViewById(R.id.btnCheckDoanhThu);
        edtTuNgay = view.findViewById(R.id.edtTuNgay);
        edtDenNgay = view.findViewById(R.id.edtDenNgay);
        edtDoanhThu = view.findViewById(R.id.edtDoanhThu);
        sdf = new SimpleDateFormat("yyyy-MM-dd");

        edtDenNgay.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view1, year1, month1, dayOfMonth) -> {
                GregorianCalendar c = new GregorianCalendar(year1, month1, dayOfMonth);
                edtDenNgay.setText(sdf.format(c.getTime()));
            }, year, month, day);
            dialog.show();
        });

        edtTuNgay.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view12, year12, month12, dayOfMonth) -> {
                GregorianCalendar c = new GregorianCalendar(year12, month12, dayOfMonth);
                edtTuNgay.setText(sdf.format(c.getTime()));
            }, year, month, day);
            dialog.show();
        });

        btnCheck.setOnClickListener(v -> {
            String tuNgay = edtTuNgay.getText().toString();
            String denNgay = edtDenNgay.getText().toString();

            if (tuNgay.isEmpty() || denNgay.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchOrdersFromFirebase(tuNgay, denNgay);
        });
    }

    private void fetchOrdersFromFirebase(String startDate, String endDate) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalRevenue = 0.0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order.getStatus() == 0) {
                            if (order != null && isWithinDateRange(order.getOrderDate(), startDate, endDate)) {
                                totalRevenue += order.getTotalAmount();
                            }
                        }
                    }
                }
                String doanhThuChu = NumberToWordsConverter.convertToWords((long) totalRevenue);

                // Display revenue in words
                edtDoanhThu.setText(doanhThuChu);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWithinDateRange(String orderDate, String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date order = sdf.parse(orderDate.split(" ")[0]);
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            return order.compareTo(start) >= 0 && order.compareTo(end) <= 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
