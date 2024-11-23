package com.fpoly.nhom2.mifoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityCreateProfileBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.User;

public class CreateProfileActivity extends AppCompatActivity {
    private ActivityCreateProfileBinding bind;
    Dialogs dialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityCreateProfileBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        bind.btnBack.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        bind.createBtn.setOnClickListener(v -> {
            dialogs = new Dialogs();
            dialogs.showProgressBar(this);
            dialogs.show();
            // Kiểm tra xem các trường nhập liệu có trống không
            if (!validateFirstName() || !validateLastName() ||
                    !validateEmailAddress() || !validateAddress()) {
                dialogs.dismiss();
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            createProfile();
        });

    }


    // Hàm kiểm tra xem số first name có trống không
    private Boolean validateFirstName() {
        String val = Objects.requireNonNull(bind.edtFirstName.getText()).toString();
        if (val.isEmpty()) {
            bind.edtFirstName.setError("First name is required");
            return false;
        } else {
            bind.edtFirstName.setError(null);
            return true;
        }
    }

    // Hàm kiểm tra xem số last name có trống không
    private Boolean validateLastName() {
        String val = Objects.requireNonNull(bind.edtLastName.getText()).toString();
        if (val.isEmpty()) {
            bind.edtLastName.setError("Last name is required");
            return false;
        } else {
            bind.edtLastName.setError(null);
            return true;
        }
    }

    // Hàm kiểm tra xem email address có trống không
    private Boolean validateEmailAddress() {
        String val = Objects.requireNonNull(bind.edtEmailAddress.getText()).toString();
        if (val.isEmpty()) {
            bind.edtEmailAddress.setError("Email address is required");
            return false;
        } else {
            bind.edtEmailAddress.setError(null);
            return true;
        }
    }

    // Hàm kiểm tra xem address có trống không
    private Boolean validateAddress() {
        String val = Objects.requireNonNull(bind.edtAddress.getText()).toString();
        if (val.isEmpty()) {
            bind.edtAddress.setError("Address is required");
            return false;
        } else {
            bind.edtAddress.setError(null);
            return true;
        }
    }

    private void createProfile() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users");
        String firstName = Objects.requireNonNull(bind.edtFirstName.getText()).toString();
        String lastName = Objects.requireNonNull(bind.edtLastName.getText()).toString();
        String emailAddress = Objects.requireNonNull(bind.edtEmailAddress.getText()).toString();
        String address = Objects.requireNonNull(bind.edtAddress.getText()).toString();
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        String password = getIntent().getStringExtra("password");
        String id = phoneNumber;
        User user = new User(id, phoneNumber, password, firstName + " " + lastName, emailAddress, address);
        assert phoneNumber != null;
        reference.child(phoneNumber).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                dialogs.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogs.dismiss();
            }
        });
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("phoneNumber",phoneNumber);
        startActivity(intent);
        Toast.makeText(this, "Tạo tài khoản thành công !", Toast.LENGTH_SHORT).show();
        dialogs.dismiss();
    }
}