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

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final String STORAGE_PERMISSION_NEEDED = "This permission is needed to access your gallery to select images.";

    private ActivityEditProfileBinding binding;
    private User user;
    private StorageReference storageReference;
    private Dialogs dialogs;
    private Uri userImageUri;

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

        init();
        setupListeners();
        getDataFromFirebase();
    }

    private void init() {
        dialogs = new Dialogs();
        dialogs.showProgressBar(this);
        storageReference = FirebaseStorage.getInstance().getReference("Image User");
    }

    private void setupListeners() {
        binding.btnBackEditprofile.setOnClickListener(v -> navigateToProfile());
        binding.ivCurrentUsr.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
        binding.btnComfirm.setOnClickListener(v -> uploadImageToFirebase());
    }

    private void navigateToProfile() {
        startActivity(new Intent(EditProfile.this, Profile.class));
    }

    private void checkPermissionAndOpenImagePicker() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager())) {
            openImagePicker();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestAllFilesAccessPermission();
        } else {
            requestStoragePermission();
        }
    }

    private void requestAllFilesAccessPermission() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage(STORAGE_PERMISSION_NEEDED)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                            ? new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()))
                            : new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage(STORAGE_PERMISSION_NEEDED)
                .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }

    private void uploadImageToFirebase() {
        dialogs.show();
        if (userImageUri == null) {
            updateUserProfile(user.getImageAvatar());
        } else {
            uploadNewImageToFirebase();
        }
    }

    private void uploadNewImageToFirebase() {
        String imageId = user.getName() + user.getUserId();
        storageReference.child(imageId).putFile(userImageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.child(imageId).getDownloadUrl()
                        .addOnSuccessListener(uri -> updateUserProfile(uri.toString()))
                        .addOnFailureListener(e -> showErrorMessage("Failed to get download URL")))
                .addOnFailureListener(e -> showErrorMessage("Failed to upload image"));
    }

    private void updateUserProfile(String imageLink) {
        if (!isInputValid()) return;

        User updatedUser = new User(MainActivity.id, binding.edtPhoneNumber.getText().toString(),
                user.getPassword(), binding.edtFullName.getText().toString(),
                binding.edtMail.getText().toString(), binding.edtHomeTown.getText().toString());
        updatedUser.setImageAvatar(imageLink);
        updatedUser.setRole(user.getRole());

        FirebaseDatabase.getInstance().getReference("users")
                .child(MainActivity.id)
                .setValue(updatedUser)
                .addOnSuccessListener(unused -> {
                    showSuccessMessage("Thay đổi thành công");
                    navigateToProfile();
                })
                .addOnFailureListener(e -> showErrorMessage("Update Fail"));
    }

    private boolean isInputValid() {
        if (binding.edtFullName.getText().toString().isEmpty() || binding.edtMail.getText().toString().isEmpty() ||
                binding.edtPhoneNumber.getText().toString().isEmpty() || binding.edtHomeTown.getText().toString().isEmpty()) {
            dialogs.dismiss();
            showErrorMessage("Không được để trống các trường thông tin");
            return false;
        }
        return true;
    }

    private void getDataFromFirebase() {
        dialogs.show();
        FirebaseDatabase.getInstance().getReference("users")
                .child(MainActivity.id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            user = snapshot.getValue(User.class);
                            updateUI();
                        }
                        dialogs.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showErrorMessage("Failed to load user data");
                        dialogs.dismiss();
                    }
                });
    }

    private void updateUI() {
        binding.edtFullName.setText(user.getName());
        binding.edtMail.setText(user.getEmail());
        binding.edtPhoneNumber.setText(user.getPhoneNumber());
        binding.edtHomeTown.setText(user.getAddress());
        Glide.with(this).load(user.getImageAvatar()).error(R.drawable.none_avatar).into(binding.ivCurrentUsr);
    }

    private void showErrorMessage(String message) {
        dialogs.dismiss();
        Toast.makeText(EditProfile.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccessMessage(String message) {
        dialogs.dismiss();
        Toast.makeText(EditProfile.this, message, Toast.LENGTH_SHORT).show();
    }
}
