package com.fpoly.nhom2.mifoodapp.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityPaymentBinding;

public class Payment extends AppCompatActivity {
    private ActivityPaymentBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(Payment.this, MainActivity.class));
                finish();
            }
        });
    }
}