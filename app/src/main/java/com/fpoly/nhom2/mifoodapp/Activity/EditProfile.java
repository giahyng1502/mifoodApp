package com.fpoly.nhom2.mifoodapp.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.databinding.ActivityEditProfileBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.User;

public class EditProfile extends AppCompatActivity {
    // Sử dụng View Binding để truy cập các view
    ActivityEditProfileBinding binding;
    User user;
    StorageReference storageReference;
    Uri userImageUri;
    Dialogs dialogs;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    // Sử dụng ActivityResultLauncher để xử lý kết quả từ trình chọn ảnh
    private final ActivityResultLauncher<Intent> activityResultLauncher
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    userImageUri = result.getData().getData();
                    if (userImageUri != null) {
                        binding.ivCurrentUsr.setImageURI(userImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dialogs = new Dialogs();
        dialogs.showProgressBar(this);

        // Nút quay lại màn hình hồ sơ
        binding.btnBackEditprofile.setOnClickListener(v -> startActivity(new Intent(EditProfile.this, Profile.class)));

        // Thiết lập Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("Image User");

        // Lấy dữ liệu người dùng từ Firebase Realtime Database
        getData();

        // Khi nhấn vào ảnh người dùng, kiểm tra quyền và mở trình chọn ảnh
        binding.ivCurrentUsr.setOnClickListener(v -> checkPermissionAndOpenImagePicker());

        // Khi nhấn vào nút xác nhận, tải hình ảnh lên Firebase
        binding.btnComfirm.setOnClickListener(v -> {
            dialogs.show();
            upLoadImageToFireBase();
        });
    }

    // Kiểm tra quyền và mở trình chọn ảnh
    private void checkPermissionAndOpenImagePicker() {
        // Nếu phiên bản Android nhỏ hơn M, không cần kiểm tra quyền
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openImage();
            return;
        }

        // Nếu phiên bản Android là Android 11 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                openImage();
            } else {
                // Yêu cầu quyền truy cập tất cả các tệp
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This permission is needed to access your gallery to select images.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            } catch (Exception e) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        } else {
            // Kiểm tra quyền truy cập bộ nhớ ngoài đối với các phiên bản Android khác
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openImage();
            } else {
                // Nếu quyền chưa được cấp, yêu cầu quyền từ người dùng
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Needed")
                            .setMessage("This permission is needed to access your gallery to select images.")
                            .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION))
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImage();
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Mở trình chọn ảnh để người dùng chọn hình ảnh từ thư viện
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }

    // Tải hình ảnh lên Firebase Storage
    private void upLoadImageToFireBase() {
        String imageId = user.getName() + user.getUserId();
        if (userImageUri == null) {
            // Nếu không có hình ảnh mới, sử dụng hình ảnh cũ
            dialogs.dismiss();
            updateProfile(user.getImageAvatar());
        } else {
            // Tải lên hình ảnh mới và cập nhật đường dẫn hình ảnh trên Firebase
            storageReference.child(imageId).putFile(userImageUri).addOnSuccessListener(taskSnapshot -> storageReference.child(imageId).getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageLink = uri.toString();
                        updateProfile(imageLink);
                        dialogs.dismiss();
                    }).addOnFailureListener(e -> {
                        dialogs.dismiss();
                        Toast.makeText(EditProfile.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        dialogs.dismiss();
                        Toast.makeText(EditProfile.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Cập nhật thông tin hồ sơ người dùng
    private void updateProfile(String imageLink) {
        String name = binding.edtFullName.getText().toString();
        String mail = binding.edtMail.getText().toString();
        String phone = binding.edtPhoneNumber.getText().toString();
        String hometown = binding.edtHomeTown.getText().toString();
        String pass = user.getPassword();
        if (name.isEmpty()) {
            dialogs.dismiss();
            Toast.makeText(EditProfile.this, "Không được để trống tên người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mail.isEmpty()) {
            Toast.makeText(EditProfile.this, "Không được để trống emai người dùng", Toast.LENGTH_SHORT).show();
            dialogs.dismiss();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(EditProfile.this, "Không được để trống số điện thoại người dùng", Toast.LENGTH_SHORT).show();
            dialogs.dismiss();
            return;
        }
        if (hometown.isEmpty()) {
            Toast.makeText(EditProfile.this, "Không được để trống địa chỉ người dùng", Toast.LENGTH_SHORT).show();
            dialogs.dismiss();
            return;
        }
        if (pass.isEmpty()) {
            Toast.makeText(EditProfile.this, "Không được để trống mật khẩu", Toast.LENGTH_SHORT).show();
            dialogs.dismiss();
            return;
        }
        User user1 = new User(MainActivity.id, phone, pass, name, mail, hometown);
        user1.setImageAvatar(imageLink);
        user1.setRole(user.getRole());
        // Lưu thông tin cập nhật vào Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("users")
                .child(MainActivity.id)
                .setValue(user1)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditProfile.this, "Thay đổi thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfile.this, Profile.class));
                    dialogs.dismiss();
                }).addOnFailureListener(e -> {
                    Toast.makeText(EditProfile.this, "Update Fail", Toast.LENGTH_SHORT).show();
                    dialogs.dismiss();
                });
    }

    // Lấy dữ liệu người dùng từ Firebase Realtime Database
    private void getData() {
        dialogs.show();
        FirebaseDatabase.getInstance().getReference("users")
                .child(MainActivity.id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            user = snapshot.getValue(User.class);
                            // Hiển thị dữ liệu người dùng trên giao diện
                            assert user != null;
                            binding.edtFullName.setText(user.getName());
                            binding.edtMail.setText(user.getEmail());
                            Glide.with(EditProfile.this).load(user.getImageAvatar()).error(R.drawable.none_avatar).into(binding.ivCurrentUsr);
                            binding.edtPhoneNumber.setText(user.getPhoneNumber());
                            binding.edtHomeTown.setText(user.getAddress());
                            dialogs.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialogs.dismiss();
                        Toast.makeText(EditProfile.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
