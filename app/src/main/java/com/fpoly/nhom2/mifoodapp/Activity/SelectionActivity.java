package com.fpoly.nhom2.mifoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivitySelectionBinding;

public class SelectionActivity extends AppCompatActivity {
    private ActivitySelectionBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivitySelectionBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        bind.getStartedBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        bind.tvRegisterHere.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // load the animation
        Animation animUpAndDown = AnimationUtils.loadAnimation(this, R.anim.anim_up_down);
        // start the animation
        bind.icUp.startAnimation(animUpAndDown);
    }
}