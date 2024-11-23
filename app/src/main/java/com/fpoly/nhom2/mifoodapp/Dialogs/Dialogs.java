package com.fpoly.nhom2.mifoodapp.Dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import java.util.Objects;

import FPT.PRO1122.Nhom3.DuAn1.Activity.Profile;
import FPT.PRO1122.Nhom3.DuAn1.R;

@SuppressLint("InflateParams")
public class Dialogs {
    public AlertDialog dialog;
    public AlertDialog.Builder builder;

    public void showProgressBar(Context context) {
        builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.layout_progress);
        dialog = builder.create();
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void showSignOutDialog(Profile profile) {
        View view = LayoutInflater.from(profile.getApplicationContext()).inflate(R.layout.dialog_logout, null, false);
        builder = new AlertDialog.Builder(profile);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        // Xử lý sự kiện khi nhấn nút "Yes"
        AppCompatButton yes = view.findViewById(R.id.yesBtn);
        yes.setOnClickListener(v -> profile.signOut());
        // Xử lý sự kiện khi nhấn nút "No"
        AppCompatButton no = view.findViewById(R.id.noBtn);
        no.setOnClickListener(v -> dialog.dismiss());
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
