package com.fpoly.nhom2.mifoodapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityLoginBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.User;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding bind;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    Dialogs dialogs;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data).getResult());
            }
            else dialogs.dismiss();
        }
    });

    private void handleSignInResult(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        dialogs.showProgressBar(this);
                        Intent intent = new Intent(this, MainActivity.class);
                        // user exit to homephone
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        FirebaseUser user = auth.getCurrentUser();
                        updateUI(user);
                        assert user != null;
                        saveDataToPreferences(user.getUid());
                        dialogs.dismiss();
                    } else {
                        dialogs.dismiss();
                        Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        String name = user.getDisplayName();
                        String mail = user.getEmail();
                        String avatar = user.getPhotoUrl() + "";
                        String uid = user.getUid();
                        User user1 = new User(uid, "", "123", name, mail, "");
                        user1.setImageAvatar(avatar);
                        saveDataToPreferences(uid);
                        database.getReference("users").child(user.getUid()).setValue(user1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d("SignIn", "User not signed in");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        if (getIntent() != null) {
            String phoneNumber = getIntent().getStringExtra("phoneNumber");
            bind.edtPhoneNumber.setText(phoneNumber);
        }
        FirebaseApp.initializeApp(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        dialogs = new Dialogs();
        dialogs.showProgressBar(this);

        bind.googleBtn.setOnClickListener(v -> {
            // Đăng nhập bằng Google
            dialogs.show();
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });

        bind.signInBtn.setOnClickListener(v -> {
            dialogs.show();
            // Kiểm tra xem các trường nhập liệu có trống không
            if (!validatePhoneNumber() || !validatePassword()) {
                // Nếu có trường nào trống, thông báo và dừng hàm
                dialogs.dismiss();
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Nếu các trường nhập liệu không trống, kiểm tra người dùng có tồn tại trong Firebase hay không
                checkUser();
            }
        });

        bind.tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // Hàm này dùng để kiểm tra xem phone number có trống không
    private Boolean validatePhoneNumber() {
        String val = Objects.requireNonNull(bind.edtPhoneNumber.getText()).toString();
        if (val.isEmpty()) {
            bind.edtPhoneNumber.setError("Phone number is required");
            return false;
        } else {
            bind.edtPhoneNumber.setError(null);
            return true;
        }
    }

    // Hàm này dùng để kiểm tra xem mật khẩu có trống không
    private Boolean validatePassword() {
        String val = Objects.requireNonNull(bind.edtPassword.getText()).toString();
        if (val.isEmpty()) {
            bind.edtPassword.setError("Password is required");
            return false;
        } else {
            bind.edtPassword.setError(null);
            return true;
        }
    }

    //  Hàm này dùng để kiểm tra xem người dùng có tồn tại trong Firebase hay không
    private void checkUser() {
        String phoneNumber = Objects.requireNonNull(bind.edtPhoneNumber.getText()).toString().trim();
        String password = Objects.requireNonNull(bind.edtPassword.getText()).toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        // Kiểm tra xem người dùng có tồn tại trong Firebase hay không
        Query checkUserDB = reference.orderByChild("userId").equalTo(phoneNumber);

        checkUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Nếu người dùng tồn tại, kiểm tra mật khẩu
                if (snapshot.exists()) {
                    bind.edtPhoneNumber.setError(null);
                    String passwordDB = snapshot.child(phoneNumber).child("password").getValue(String.class);

                    assert passwordDB != null;
                    // Nếu mật khẩu chính xác, chuyển sang màn hình chính
                    if (passwordDB.equals(password)) {
                        dialogs.dismiss();
                        bind.edtPhoneNumber.setError(null);
                        saveDataToPreferences(phoneNumber);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Nếu mật khẩu không chính xác, thông báo và focus vào mật khẩu
                        dialogs.dismiss();
                        bind.edtPassword.setError("Invalid Credentials");
                        bind.edtPassword.requestFocus();
                    }
                } else {
                    // Nếu người dùng không tồn tại, thông báo và focus vào người dùng
                    dialogs.dismiss();
                    bind.edtPhoneNumber.setError("User does not exist");
                    bind.edtPhoneNumber.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý trường hợp có lỗi xảy ra
                dialogs.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDataToPreferences(String id) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserID", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);
        editor.apply();
    }
}