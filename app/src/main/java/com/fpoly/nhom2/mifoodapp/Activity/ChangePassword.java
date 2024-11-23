package com.fpoly.nhom2.mifoodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import FPT.PRO1122.Nhom3.DuAn1.R;

public class ChangePassword extends AppCompatActivity {
    ImageView btn_back;
    Button btnChangePass;
    EditText edtNewPass, edtReNewPass, edtOldPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        anhXa();
        //1
        btn_back.setOnClickListener(v -> startActivity(new Intent(ChangePassword.this, Profile.class)));
        btnChangePass.setOnClickListener(e -> {
            changePassword();
        });
    }

    private void anhXa() {
        btn_back = findViewById(R.id.btn_back_changepass);
        edtNewPass = findViewById(R.id.edtNewPass);
        edtReNewPass = findViewById(R.id.edtReNewPass);
        edtOldPass = findViewById(R.id.edtOldPass);
        btnChangePass = findViewById(R.id.btnChangePass);
    }

    private void changePassword() {
        String userId = MainActivity.id;
        if (userId == null || userId.isEmpty()) {
            // Handle the error when userId is null or empty
            Log.e("ProfileActivity", "User ID is null or empty");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pass = snapshot.child("password").getValue(String.class);
                    if (pass != null && pass.equalsIgnoreCase(edtOldPass.getText().toString())) {
                        String newPass = edtNewPass.getText().toString();
                        String reNewPass = edtReNewPass.getText().toString();
                        if (checkPass(newPass, reNewPass)) {
                            userRef.child("password").setValue(newPass)
                                    .addOnSuccessListener(aVoid -> {
                                        // Password update successful
                                        startActivity(new Intent(ChangePassword.this, LoginActivity.class));
                                        // Notify that the password has been changed successfully
                                        Toast.makeText(ChangePassword.this, "Thay đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle error when password update fails
                                        Log.e("ProfileActivity", "Error updating password", e);
                                        Toast.makeText(ChangePassword.this, "Thay đổi mật khẩu thất bại vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Old password is incorrect
                        Toast.makeText(ChangePassword.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User does not exist
                    Toast.makeText(ChangePassword.this, "User does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error when Firebase query is cancelled
                Log.e("ProfileActivity", "Error querying user", error.toException());
            }
        });
    }

    private boolean checkPass(String newPass, String reNewPass) {
        boolean isValid = true;
        if (newPass.isEmpty()) {
            Toast.makeText(this, "Mật khẩu mới không được để trống", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (reNewPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập lại mật khẩu mới", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (!newPass.equals(reNewPass)) {
            Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }
}