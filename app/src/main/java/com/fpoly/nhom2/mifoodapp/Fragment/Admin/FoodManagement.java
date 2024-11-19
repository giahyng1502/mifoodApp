package com.fpoly.nhom2.mifoodapp.Fragment.Admin;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import FPT.PRO1122.Nhom3.DuAn1.Activity.MainActivity;
import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.Home;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.adapter.AdapterFoodManagement;
import FPT.PRO1122.Nhom3.DuAn1.adapter.AdapterSpinner;
import FPT.PRO1122.Nhom3.DuAn1.databinding.DialogAddFoodBinding;
import FPT.PRO1122.Nhom3.DuAn1.databinding.FragmentFoodManagerBinding;
import FPT.PRO1122.Nhom3.DuAn1.implement.SwipeToDeleteCallback;
import FPT.PRO1122.Nhom3.DuAn1.model.Catagory;
import FPT.PRO1122.Nhom3.DuAn1.model.Foods;

public class FoodManagement extends Fragment {
    FragmentFoodManagerBinding binding;
    List<Foods> list;
    DatabaseReference databaseReference;
    AdapterFoodManagement adapterFoodManagement;
    DialogAddFoodBinding dialogAddFoodBinding;
    Uri foodUri;
    Dialog dialog;
    Dialogs dialogs;

    ActivityResultLauncher<Intent>activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            foodUri = result.getData().getData();
            dialogAddFoodBinding.ivFood.setImageURI(foodUri);
        }
    });
    public ActivityResultLauncher<Intent> activityResultLauncherUpdate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri uriFoodUpdate = result.getData().getData();
            adapterFoodManagement.setImageUri(uriFoodUpdate);
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFoodManagerBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialogs = new Dialogs();
        dialogs.showProgressBar(requireContext());
        dialogs.show();
        databaseReference = FirebaseDatabase.getInstance().getReference("Foods");

        if (MainActivity.role == 0) {
            binding.backtoHome.setVisibility(View.GONE);
        } else {
            binding.tvTitleFoodManagement.setText("Tìm Kiếm");
        }

        binding.backtoHome.setOnClickListener(v -> {
            Home targetFragment = new Home();

            // Sử dụng FragmentManager và FragmentTransaction để chuyển đổi
            FragmentManager fragmentManager = getParentFragmentManager(); // Hoặc getActivity().getSupportFragmentManager() nếu dùng trong Activity
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Thay thế Fragment hiện tại bằng Fragment đích
            fragmentTransaction.replace(R.id.frameLayout, targetFragment);

            // Thêm vào back stack để có thể quay lại Fragment trước đó
            fragmentTransaction.addToBackStack(null);

            // Hoàn thành giao dịch
            fragmentTransaction.commit();
        });
        initRecyclerView();
        getFoodFromFireBase();
        if(MainActivity.role == 0) {
            // Admin = 0
            deleteFood();
            AddFood();
        } else {
            binding.btnAdd.setVisibility(View.GONE);
        }

        searchView();
        binding.searchPrice.setOnClickListener(v -> showTextViewDialog());
    }

    private void searchView() {
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
        ArrayList<Foods> listSearch = new ArrayList<>();
        for (Foods foods : list) {
            if (foods.getTitle().toLowerCase().contains(query)
            ) {
                listSearch.add(foods);
            }
        }
        adapterFoodManagement.listFillter(listSearch);
    }
    private void AddFood() {
        binding.btnAdd.setOnClickListener(v -> {
            showDialogAdd();

        });
    }
    private void showTextViewDialog() {
        final String[] textViewOptions = {"0 - 50k", "50k - 100k", "100k ->"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tìm kiếm theo giá tiền")
                .setItems(textViewOptions, (dialog, which) -> {
                    String selectedOption = textViewOptions[which];
                    double minPrice = 0, maxPrice = Double.MAX_VALUE;

                    switch (selectedOption) {
                        case "0 - 50k":
                            maxPrice = 50000;
                            break;
                        case "50k - 100k":
                            minPrice = 50001;
                            maxPrice = 100000;
                            break;
                        case "100k ->":
                            minPrice = 100001;
                            break;
                    }

                    searchByPrice(minPrice, maxPrice);
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void searchByPrice(double minPrice, double maxPrice) {
        ArrayList<Foods> foods = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Foods");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Foods monAn = snapshot.getValue(Foods.class);

                        // Kiểm tra nếu monAn không phải null và có giá nằm trong khoảng
                        if (monAn != null ) {
                            double price = monAn.getPrice();
                            if (price >= minPrice && price <= maxPrice) {
                                foods.add(monAn);
                            }
                        }
                    }
                    // Chỉ cập nhật adapter nếu danh sách không rỗng
                    if (!foods.isEmpty()) {
                        adapterFoodManagement.listFillter(foods);
                    } else {
                        // Xử lý trường hợp không tìm thấy món ăn nào phù hợp
                        Toast.makeText(requireContext(), "Không tìm thấy món ăn nào trong khoảng giá này.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi
                Toast.makeText(requireContext(), "Lỗi khi truy vấn dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogAdd() {
        List<Catagory> catagories = new ArrayList<>();
        dialog = new Dialog(requireContext());
        dialogAddFoodBinding = DialogAddFoodBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(dialogAddFoodBinding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setStatusBarColor(Color.BLACK);
        dialogAddFoodBinding.ivFood.setOnClickListener(v-> openMedia());

        FirebaseDatabase.getInstance()
                        .getReference("Category")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                           for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                               catagories.add(dataSnapshot.getValue(Catagory.class));
                                           }
                                        }
                                        if (!catagories.isEmpty()) {
                                            AdapterSpinner adapter = new AdapterSpinner(requireContext(), catagories);
                                            dialogAddFoodBinding.spnCategory.setAdapter(adapter);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(requireContext(), "Fail"+error, Toast.LENGTH_SHORT).show();
                                    }
                                });

        dialogAddFoodBinding.btnCancelUserManagement.setOnClickListener(v->dialog.dismiss());
        dialogAddFoodBinding.btnSubMitUserManagement.setOnClickListener(v-> {
            dialogs.showProgressBar(requireContext());
            dialogs.show();
            String name = dialogAddFoodBinding.tvFoodName.getText().toString();
            String price = dialogAddFoodBinding.edtPrice.getText().toString();
            String describe = dialogAddFoodBinding.edtDescribe.getText().toString();

            int categoryId = catagories.get(dialogAddFoodBinding.spnCategory.getSelectedItemPosition()).getId();
            Foods foods = new Foods();
            int timestamp = (int) System.currentTimeMillis();
            if (name.isEmpty()) {
                dialogs.dismiss();
                Toast.makeText(requireContext(), "Không được để trống tên món ăn", Toast.LENGTH_SHORT).show();
                return;
            }
            if (price.isEmpty()) {
                dialogs.dismiss();
                Toast.makeText(requireContext(), "Không được để trống giá món ăn", Toast.LENGTH_SHORT).show();
                return;
            }
            if (describe.isEmpty()) {
                Toast.makeText(requireContext(), "Không được để trống mô tả món ăn", Toast.LENGTH_SHORT).show();
                dialogs.dismiss();
                return;
            }
            if (categoryId == -1) {
                Toast.makeText(requireContext(), "Không được để trống loại món ăn", Toast.LENGTH_SHORT).show();
                dialogs.dismiss();
                return;
            }
            foods.setId(timestamp);
            foods.setPrice(Double.parseDouble(price));
            foods.setCategoryId(categoryId);
            foods.setTitle(name);
            foods.setDescription(describe);
            
            putFoodAvatar(foods);
        });
        dialog.show();
    }

    private void putFoodAvatar(Foods foods) {
        if (foodUri == null) {
            dialogs.dismiss();
            Toast.makeText(requireContext(), "Bạn cần tải lên hình ảnh của món ăn này", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseStorage.getInstance().getReference("Image Food").child(foods.getId()+"")
                    .putFile(foodUri).addOnSuccessListener(taskSnapshot ->
                            FirebaseStorage.getInstance().getReference("Image Food")
                            .child(foods.getId()+"")
                            .getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageFood = uri.toString();
                                foods.setImagePath(imageFood);
                                uploadFood(foods);
                            })).addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(requireContext(), "Fail"+e, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadFood(Foods foods) {
        FirebaseDatabase.getInstance().getReference("Foods").child(foods.getId()+"")
                .setValue(foods).addOnSuccessListener(unused -> {
                    dialogs.dismiss();
                    dialog.dismiss();
                    adapterFoodManagement.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    dialogs.dismiss();
                    adapterFoodManagement.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "thất bại"+e, Toast.LENGTH_SHORT).show();
                });
    }

    private void openMedia() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    private void deleteFood() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext(),binding.recyclerView,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buffer) {
                buffer.add(new MyButton(requireContext(),"",13,R.drawable.delete, Color.parseColor("#FF3C30"), pos-> {
                    showBottomSheetDialog(pos);
                }));
            }
        };
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void showBottomSheetDialog(int pos) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout, null);

        Button btnCancel = bottomSheetView.findViewById(R.id.btnCancel);
        TextView btnConfirm = bottomSheetView.findViewById(R.id.btnConfirm);
        TextView tvMessage = bottomSheetView.findViewById(R.id.tvMessage);
        tvMessage.setText("Món ăn này sẽ bị xoá vĩnh viễn khỏi ứng dụng");
        btnCancel.setOnClickListener(v -> {
            adapterFoodManagement.notifyDataSetChanged();
            bottomSheetDialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            dialogs.showProgressBar(requireContext());
            dialogs.show();
            Foods foods = list.get(pos);
            // Xóa user khỏi Firebase Database
            FirebaseDatabase.getInstance().getReference("Foods")
                    .child(foods.getId()+"").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialogs.dismiss();
                            bottomSheetDialog.dismiss();
                            adapterFoodManagement.notifyDataSetChanged();
                            Toast.makeText(requireContext(), "Xoá Thành Công", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialogs.dismiss();
                            bottomSheetDialog.dismiss();
                            adapterFoodManagement.notifyDataSetChanged();
                            Toast.makeText(requireContext(), "Error" + e, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setStatusBarColor(Color.BLACK);
        bottomSheetDialog.show();
    }

    private void getFoodFromFireBase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        list.add(dataSnapshot.getValue(Foods.class));
                    }
                    adapterFoodManagement.notifyDataSetChanged();
                }

                dialogs.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Fail"+error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView() {
        list = new ArrayList<>();
        adapterFoodManagement = new AdapterFoodManagement(requireContext(),list,activityResultLauncherUpdate);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.setAdapter(adapterFoodManagement);
        adapterFoodManagement.notifyDataSetChanged();
    }
}