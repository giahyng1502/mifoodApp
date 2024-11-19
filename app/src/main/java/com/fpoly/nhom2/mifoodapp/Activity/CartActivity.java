package com.fpoly.nhom2.mifoodapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.adapter.CheckOutAdapter;
import FPT.PRO1122.Nhom3.DuAn1.adapter.GioHangAdapter;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityCartBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.Cart;
import FPT.PRO1122.Nhom3.DuAn1.model.Order;
import FPT.PRO1122.Nhom3.DuAn1.model.User;

@SuppressLint("SetTextI18n")
public class CartActivity extends BaseActivity {
    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private  double tax;


    private ArrayList<Cart> cartList;
    private double deliveryFee = 10000;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
        setRecyclerView();
        initList();
        tinhTongGioHang();
        datHang();

        binding.backBtn.setOnClickListener(v-> startActivity(new Intent(
                CartActivity.this,MainActivity.class)));

        binding.btnVoucher.setOnClickListener(v-> {
            String voucher = binding.tvVoucher.getText().toString();
            if (voucher.equalsIgnoreCase("freeship")){
                setDeliveryFee(0);
                tinhTongGioHang();
                Toast.makeText(this, "Sử dụng mã giảm giá thành công", Toast.LENGTH_SHORT).show();
            }else {
                setDeliveryFee(10000);
                tinhTongGioHang();
                Toast.makeText(this, "Mã giảm giá không tồn tại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRecyclerView() {
        cartList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CartActivity.this, LinearLayoutManager.VERTICAL, false);
        binding.cartRec.setLayoutManager(linearLayoutManager);
        adapter = new GioHangAdapter(cartList);
        binding.cartRec.setAdapter(adapter);
    }

    private void datHang() {
        binding.datHangBtn.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(CartActivity.this, "Bạn không có đơn hàng nào", Toast.LENGTH_SHORT).show();
                return;
            }
            Order order = new Order();
            int timestamp = (int) System.currentTimeMillis();
            order.setOrderId("HD"+timestamp);
            order.setTax(tax);
            order.setDeliveryFee(deliveryFee);
            order.setUserID(MainActivity.id);
            order.setOrderDetails(cartList);

            order.setTotalAmount(totalAmount);
            order.setStatus(3);
            showPayMethod(order);
        });
    }


    private void initList() { 
        DatabaseReference myRef = database.getReference("Carts").child(MainActivity.id);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    cartList.clear();
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Cart cart = issue.getValue(Cart.class);
                        if (cart != null) cartList.add(cart);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void tinhTongGioHang() {
        NumberFormat vietnameseCurrencyFormat = NumberFormat.getCurrencyInstance();
        vietnameseCurrencyFormat.setMaximumFractionDigits(0);
        vietnameseCurrencyFormat.setCurrency(Currency.getInstance("VND"));
        double percentTax = 0.02;

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Carts").child(MainActivity.id);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double tongGiaMonAn = 0; // Khởi tạo biến lưu tổng giá sản phẩm

                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Cart cart = itemSnapshot.getValue(Cart.class);

                        if (cart != null) {
                            // Cộng dồn giá sản phẩm (giả sử getPrice() và getQuantity() là các phương thức của GioHang)
                            tongGiaMonAn += cart.getTotal();
                        }
                    }
                }

                // Tính thuế
                double tax = tongGiaMonAn * percentTax;
                setTax(tax);
                // Tính tổng số tiền cần trả (giá sản phẩm + thuế + phí vận chuyển)
                double totalAmount = tongGiaMonAn + getTax() + getDeliveryFee();
                setTotalAmount(totalAmount);

                // Cập nhật giao diện
                double totalFormated = tongGiaMonAn;
                String formattedTotal = vietnameseCurrencyFormat.format(totalFormated);
                double taxFormated = getTax();
                String formattedTax = vietnameseCurrencyFormat.format(taxFormated);
                double deliveryFeeFormated = getDeliveryFee();
                String formattedDeliveryFee = vietnameseCurrencyFormat.format(deliveryFeeFormated);
                double totalAmountFormated = getTotalAmount();
                String formattedTotalAmount = vietnameseCurrencyFormat.format(totalAmountFormated);
                binding.totalFeeTxt.setText(formattedTotal);
                binding.taxTxt.setText(formattedTax);
                binding.deliveryFeeTxt.setText(formattedDeliveryFee);
                binding.totalTxt.setText(formattedTotalAmount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    private void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    private void setTax(double tax) {
        this.tax = tax;
    }

    public double getTax() {
        return tax;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(view -> finish());
    }
    void showPayMethod(Order order) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.fragment_item_list_dialog_list_dialog);
        TextView tongTientxt,tvPhiVanChuyen,tvThue;
        EditText ten, sdt, diaChi;
        RecyclerView rcThanhToan;
        Button datHangBtn;
        RecyclerView.Adapter adapter;
        
        tongTientxt = bottomSheetDialog.findViewById(R.id.tongTientxt);
        tvThue = bottomSheetDialog.findViewById(R.id.tvTax);
        tvPhiVanChuyen = bottomSheetDialog.findViewById(R.id.tvdeliveryFee);
        ten = bottomSheetDialog.findViewById(R.id.tenEdt);
        sdt = bottomSheetDialog.findViewById(R.id.soDienThoaiEdt);
        diaChi = bottomSheetDialog.findViewById(R.id.diaChiGiaoHang);
        rcThanhToan = bottomSheetDialog.findViewById(R.id.rcThanhToan);
        datHangBtn = bottomSheetDialog.findViewById(R.id.datHangBtn);

        NumberFormat vietnameseCurrencyFormat = NumberFormat.getCurrencyInstance();
        vietnameseCurrencyFormat.setMaximumFractionDigits(0);
        vietnameseCurrencyFormat.setCurrency(Currency.getInstance("VND"));
        
        adapter = new CheckOutAdapter((ArrayList<Cart>) order.getOrderDetails());
        assert rcThanhToan != null;
        rcThanhToan.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        rcThanhToan.setAdapter(adapter);

        assert tvPhiVanChuyen != null;
        double deliveryFeeFormated = order.getDeliveryFee();
        String formattedDeliveryFee = vietnameseCurrencyFormat.format(deliveryFeeFormated);
        tvPhiVanChuyen.setText("Phí Vận Chuyển: " + formattedDeliveryFee);
        assert tvThue != null;
        double taxFormated = order.getTax();
        String formattedTax = vietnameseCurrencyFormat.format(taxFormated);
        tvThue.setText("Thuế: " + formattedTax);
        assert tongTientxt != null;
        double totalAmountFormated = order.getTotalAmount();
        String formattedTotalAmount = vietnameseCurrencyFormat.format(totalAmountFormated);
        tongTientxt.setText("Tổng tiền:  " + formattedTotalAmount);

        FirebaseDatabase.getInstance().getReference("users").child(MainActivity.id)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    User user = snapshot.getValue(User.class);
                                    if (user != null) {
                                        assert ten != null;
                                        ten.setText(user.getName());
                                        assert sdt != null;
                                        sdt.setText(user.getPhoneNumber());
                                        assert diaChi != null;
                                        diaChi.setText(user.getAddress());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(CartActivity.this, "fail"+error, Toast.LENGTH_SHORT).show();
                            }
                        });

        assert datHangBtn != null;
        datHangBtn.setOnClickListener(v-> {
            assert ten != null;
            String name = ten.getText().toString().trim();
            assert sdt != null;
            String phone = sdt.getText().toString().trim();
            assert diaChi != null;
            String address = diaChi.getText().toString().trim();
            if (name.isEmpty()) {
                ten.setError("Không được để trống tên khách hàng");
                return;
            }
            if (phone.isEmpty()) {
                sdt.setError("Số điện thoại không được để trống");
                return;
            }
            if (address.isEmpty()) {
                ten.setError("địa chỉ giao hàng không được bỏ trống");
                return;
            }
                order.setUser(name);
                order.setPhone(phone);
                order.setAddress(address);
                order.setOrderDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                FirebaseDatabase.getInstance()
                        .getReference("Orders")
                        .child(order.getUserID()+"")
                        .child(order.getOrderId())
                        .setValue(order)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseDatabase.getInstance()
                                        .getReference("Carts")
                                        .child(order.getUserID())
                                        .removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                bottomSheetDialog.dismiss();
                                                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                                                MainActivity.idFrament = R.id.myOrder;
                                                startActivity(intent);
                                                Toast.makeText(CartActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CartActivity.this, "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CartActivity.this, "Đã xảy ra lỗi khi đặt hàng", Toast.LENGTH_SHORT).show();
                            }
                        });
        });
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();
    }
}