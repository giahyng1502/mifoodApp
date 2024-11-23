package com.fpoly.nhom2.mifoodapp.Fragment.Admin;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import FPT.PRO1122.Nhom3.DuAn1.Activity.MainActivity;
import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.adapter.AdapterUserManager;
import FPT.PRO1122.Nhom3.DuAn1.databinding.DialogAddUserManagementBinding;
import FPT.PRO1122.Nhom3.DuAn1.databinding.FragmentUserManagementBinding;
import FPT.PRO1122.Nhom3.DuAn1.implement.MyButtonClickListener;
import FPT.PRO1122.Nhom3.DuAn1.implement.SwipeToDeleteCallback;
import FPT.PRO1122.Nhom3.DuAn1.model.User;

@SuppressLint("NotifyDataSetChanged")
public class UserManagement extends Fragment {
    FragmentUserManagementBinding binding;
    AdapterUserManager adapterUserManager;
    ArrayList<User> list;
    Dialogs dialogs ;
    SwipeToDeleteCallback swipeToDeleteCallback;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri userImageUri = result.getData().getData();
                adapterUserManager.setImageUri(userImageUri);
            }
        });
        dialogs = new Dialogs();
        dialogs.showProgressBar(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // create binding
        binding = FragmentUserManagementBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDataFireBase();
        searchUser();
        addUser();
        swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext(), binding.recyclerView, 200) {

            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buffer) {
                MyButton myButton = new MyButton(requireContext(),
                        "delete",
                        13, R.drawable.delete,
                        Color.parseColor("#FF3C30"),
                        new MyButtonClickListener() {
                    @Override
                    public void onclick(int pos) {
                        showBottomSheetDialog(pos);

                    }
                });
                buffer.add(myButton);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    private void addUser() {
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaLogAdd();
            }
        });
    }
    public void diaLogAdd() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        DialogAddUserManagementBinding bindingDialog = DialogAddUserManagementBinding.inflate(LayoutInflater.from(requireContext()));
        bottomSheetDialog.setContentView(bindingDialog.getRoot());

        bindingDialog.btnCancelUserManagement.setOnClickListener(v->bottomSheetDialog.dismiss());
        bindingDialog.btnSubMitUserManagement.setOnClickListener(v-> {
            dialogs.showProgressBar(requireContext());
            dialogs.show();
            String userid = bindingDialog.edtIDNumberUserManagement.getText().toString();
            String pass = bindingDialog.edtPassUserManagement.getText().toString();
            if (userid.isEmpty() ) {
                dialogs.dismiss();
                Toast.makeText(requireContext(), "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show();
            }
            else if (pass.isEmpty()) {
                dialogs.dismiss();
                Toast.makeText(requireContext(), "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
            }
            else {
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userid).orderByChild("userId")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    User user = new User(userid, "", pass, "", "", "");
                                    FirebaseDatabase.getInstance()
                                            .getReference("users")
                                            .child(userid).setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    bottomSheetDialog.dismiss();
                                                    dialogs.dismiss();
                                                    Toast.makeText(requireContext(), "Tài khoản này đã được thêm vào hệ thống thành công", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    dialogs.dismiss();
                                                    Toast.makeText(requireContext(), "Thất bại" + e, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(requireContext(), "Tài khoản này đã tồn tại", Toast.LENGTH_SHORT).show();
                                    dialogs.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                dialogs.dismiss();
                                Toast.makeText(requireContext(), "Error" + error, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        });
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        bottomSheetDialog.show();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void showBottomSheetDialog(int pos) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout, null);

        Button btnCancel = bottomSheetView.findViewById(R.id.btnCancel);
        TextView btnConfirm = bottomSheetView.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> {
            adapterUserManager.notifyDataSetChanged();
            bottomSheetDialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            dialogs.showProgressBar(requireContext());
            dialogs.show();
            bottomSheetDialog.dismiss();

            User user = list.get(pos);
            // Xóa user khỏi Firebase Database
            FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUserId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialogs.dismiss();
                            Toast.makeText(requireContext(), "Xoá thành công", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialogs.dismiss();
                            Toast.makeText(requireContext(), "Error" + e, Toast.LENGTH_SHORT).show();
                        }
                    });
            // Xóa user khỏi danh sách và cập nhật giao diện
            adapterUserManager.notifyItemRemoved(pos);
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setStatusBarColor(Color.BLACK);
        bottomSheetDialog.show();
    }

    private void searchUser() {
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SearchData(newText);
                return true;
            }
        });
    }

    private void SearchData(String query) {
        ArrayList<User> listSearch = new ArrayList<>();
        for (User user : list) {
            if (user.getName().toLowerCase().contains(query)
                    || user.getEmail().toLowerCase().contains(query)
                    || user.getPhoneNumber().toLowerCase().contains(query.toLowerCase())) {
                listSearch.add(user);
            }
        }
        adapterUserManager.listFillter(listSearch);
        adapterUserManager.notifyDataSetChanged();
    }

    private void getDataFireBase() {
        FirebaseDatabase.getInstance().getReference("users").orderByChild("role").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list = new ArrayList<>();
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        User user;
                        user = data.getValue(User.class);
                        assert user != null;
                        if (!user.getUserId().equals(MainActivity.id)) {
                            list.add(user);
                        }
                    }
                    if (!list.isEmpty()) {
                        initLayout();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initLayout() {
        adapterUserManager = new AdapterUserManager(requireActivity(), list, activityResultLauncher);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(adapterUserManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}