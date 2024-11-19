package com.fpoly.nhom2.mifoodapp.Fragment.Admin;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.databinding.FragmentBannerManagerBinding;

public class BannerManagement extends Fragment {
    FragmentBannerManagerBinding binding;
    Uri uriBanner1,uriBanner2,uriBanner3;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private ActivityResultLauncher<Intent>activityResultLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            uriBanner1 = result.getData().getData();
            binding.ivbanner1.setImageURI(uriBanner1);
        }
    });
    private ActivityResultLauncher<Intent>activityResultLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            uriBanner2 = result.getData().getData();
            binding.ivbanner2.setImageURI(uriBanner2);
        }
    });
    private ActivityResultLauncher<Intent> activityResultLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            uriBanner3 = result.getData().getData();
            binding.ivbanner3.setImageURI(uriBanner3);
        }
    });
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBannerManagerBinding.inflate(inflater,container,false);
        // Inflate the layout for this fragment
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        openMedia1();
        openMedia2();
        openMedia3();
        getData("banner1",binding.ivbanner1);
        getData("banner2",binding.ivbanner2);
        getData("banner3",binding.ivbanner3);
        binding.btnComfirm.setOnClickListener(v-> {
            Dialogs dialogs = new Dialogs();
            dialogs.showProgressBar(requireContext());
            dialogs.show();

        if (uriBanner1!=null) {
            FirebaseStorage.getInstance()
                    .getReference("Image Banner")
                    .child("1")
                    .putFile(uriBanner1)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            FirebaseStorage.getInstance()
                                    .getReference("Image Banner")
                                    .child("1")
                                    .getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            uploadBanner(uri, "banner1");
                                            dialogs.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {

                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialogs.dismiss();
                                        }
                                    });
                        }
                    });
        }
            if (uriBanner2!=null) {
                FirebaseStorage.getInstance()
                        .getReference("Image Banner")
                        .child("2")
                        .putFile(uriBanner2)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                FirebaseStorage.getInstance()
                                        .getReference("Image Banner")
                                        .child("2")
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                uploadBanner(uri,"banner2");
                                                dialogs.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                dialogs.dismiss();

                                            }
                                        });
                            }
                        });
            }
            if (uriBanner3!=null) {
                FirebaseStorage.getInstance()
                        .getReference("Image Banner")
                        .child("3")
                        .putFile(uriBanner3)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                FirebaseStorage.getInstance()
                                        .getReference("Image Banner")
                                        .child("3")
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                uploadBanner(uri,"banner3");
                                                dialogs.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                           dialogs.dismiss();
                                            }
                                        });
                            }
                        });
            }
            if (uriBanner1 == null && uriBanner2 == null && uriBanner3==null) {
                Toast.makeText(requireContext(), "Bạn cần cập nhập mới ít nhất 1 banner", Toast.LENGTH_SHORT).show();
                dialogs.dismiss();
            }
        });
    }

    private void getData(String bannerID, ImageView banner) {
        FirebaseDatabase.getInstance()
                .getReference("Banner")
                .child(bannerID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Glide.with(requireContext()).load(snapshot.getValue(String.class)).into(banner);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void uploadBanner(Uri uri,String id) {
        if (uri != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Banner").child(id)
                    .setValue(uri.toString())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(requireContext(), "Upload Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), "Error"+e, Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void openMedia1() {
        binding.ivCameraIcon1.setOnClickListener(v-> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");

            activityResultLauncher1.launch(intent);
        });
    }
    private void openMedia2() {
        binding.ivCameraIcon2.setOnClickListener(v-> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher2.launch(intent);
        });
    }
    private void openMedia3() {
        binding.ivCameraIcon3.setOnClickListener(v-> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher3.launch(intent);
        });
    }
}