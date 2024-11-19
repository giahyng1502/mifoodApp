package com.fpoly.nhom2.mifoodapp.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import FPT.PRO1122.Nhom3.DuAn1.R;

public class BaseActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    public String TAG="thien19dev";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
    }
}
