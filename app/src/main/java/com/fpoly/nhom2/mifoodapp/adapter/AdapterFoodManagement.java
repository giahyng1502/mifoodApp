package com.fpoly.nhom2.mifoodapp.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import FPT.PRO1122.Nhom3.DuAn1.Activity.ChiTietMonAn;
import FPT.PRO1122.Nhom3.DuAn1.Activity.MainActivity;
import FPT.PRO1122.Nhom3.DuAn1.Dialogs.Dialogs;
import FPT.PRO1122.Nhom3.DuAn1.R;
import FPT.PRO1122.Nhom3.DuAn1.databinding.DialogAddFoodBinding;
import FPT.PRO1122.Nhom3.DuAn1.model.Catagory;
import FPT.PRO1122.Nhom3.DuAn1.model.Foods;

public class AdapterFoodManagement extends RecyclerView.Adapter<AdapterFoodManagement.ViewHolder> {
    Context context;
    List<Foods> list;
    private Dialog dialog;
    private DialogAddFoodBinding dialogAddFoodBinding;
    public Uri foodUri;
    ActivityResultLauncher<Intent> activityResultLauncherUpdate;
    Dialogs dialogs;

    public AdapterFoodManagement(Context context, List<Foods> list, ActivityResultLauncher<Intent> activityResultLauncherUpdate) {
        this.context = context;
        this.list = list;
        this.activityResultLauncherUpdate = activityResultLauncherUpdate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_admin, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Foods food = list.get(position);
        holder.tvTitle.setText(food.getTitle());
        holder.tvDecr.setText(food.getDescription());
        holder.tvRate.setText(food.getStar() + "");

        holder.tvPrice.setText(food.getPrice()+"");

        FirebaseDatabase.getInstance()
                .getReference("Category")
                .child(food.getCategoryId() + "")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String categoryName = snapshot.child("Name").getValue(String.class);
                            holder.tvCatagory.setText(categoryName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.tvCatagory.setText("");
                    }
                });

        Glide.with(context)
                .load(food.getImagePath())
                .thumbnail(Glide.with(context).load(R.drawable.loading_image))
                .fitCenter()
                .into(holder.ivFoodAvatar);

        if (MainActivity.role == 0) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showdialogAdd(food);
                    return true;
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChiTietMonAn.class);
            intent.putExtra("object", list.get(position));
            context.startActivity(intent);
        });

    }

    @SuppressLint("SetTextI18n")
    private void showdialogAdd(Foods food) {
        List<Catagory> catagories = new ArrayList<>();
        dialog = new Dialog(context);
        dialogAddFoodBinding = DialogAddFoodBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(dialogAddFoodBinding.getRoot());
        Objects.requireNonNull(dialog.getWindow()).setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setStatusBarColor(Color.BLACK);
        dialogAddFoodBinding.ivFood.setOnClickListener(v -> openMedia());

        dialogAddFoodBinding.edtDescribe.setText(food.getDescription());
        double priceFormated = food.getPrice();
        dialogAddFoodBinding.edtPrice.setText(priceFormated+"");
        dialogAddFoodBinding.tvFoodName.setText(food.getTitle());
        dialogAddFoodBinding.tvFoodID.setText(food.getId() + "");

        Glide.with(context)
                .load(food.getImagePath())
                .thumbnail(Glide.with(context).load(R.drawable.loading_image))
                .fitCenter()
                .into(dialogAddFoodBinding.ivFood);

        FirebaseDatabase.getInstance()
                .getReference("Category")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                catagories.add(dataSnapshot.getValue(Catagory.class));
                            }
                        }
                        if (!catagories.isEmpty()) {
                            AdapterSpinner adapter = new AdapterSpinner(context, catagories);
                            dialogAddFoodBinding.spnCategory.setAdapter(adapter);
                            for (int i = 0; i < catagories.size(); i++) {
                                if (catagories.get(i).getId() == food.getCategoryId()) {
                                    dialogAddFoodBinding.spnCategory.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        dialogAddFoodBinding.btnCancelUserManagement.setOnClickListener(v -> dialog.dismiss());
        dialogAddFoodBinding.btnSubMitUserManagement.setOnClickListener(v -> {
            dialogs = new Dialogs();
            dialogs.showProgressBar(context);
            dialogs.show();
            String name = dialogAddFoodBinding.tvFoodName.getText().toString();
            String price = dialogAddFoodBinding.edtPrice.getText().toString();
            String describe = dialogAddFoodBinding.edtDescribe.getText().toString();
            if (name.isEmpty()) {
                dialogs.dismiss();
                Toast.makeText(context, "Không được bỏ trống tên món ăn", Toast.LENGTH_SHORT).show();
                return;
            }
            if (price.isEmpty()) {
                Toast.makeText(context, "Không được bỏ trống giá món ăn", Toast.LENGTH_SHORT).show();
                dialogs.dismiss();
                return;
            }
            if (describe.isEmpty()) {
                Toast.makeText(context, "Hãy mô tả chi tiết món ăn", Toast.LENGTH_SHORT).show();
                dialogs.dismiss();
                return;
            }
            int categoryId = catagories.get(dialogAddFoodBinding.spnCategory.getSelectedItemPosition()).getId();
            Foods foods = new Foods();
            foods.setStar(food.getStar());
            foods.setId(food.getId());
            foods.setPrice(Double.parseDouble(price));
            foods.setCategoryId(categoryId);
            foods.setTitle(name);
            foods.setDescription(describe);
            if (foodUri == null) {
                foods.setImagePath(food.getImagePath());
                uploadFood(foods);
            } else {
                putFoodAvatar(foods);
            }
        });

        dialog.show();
    }

    private void putFoodAvatar(Foods foods) {
        FirebaseStorage.getInstance().getReference("Image Food").child(foods.getId() + "")
                .putFile(foodUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        FirebaseStorage.getInstance().getReference("Image Food")
                                .child(foods.getId() + "")
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageFood = uri.toString();
                                        foods.setImagePath(imageFood);
                                        uploadFood(foods);
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void uploadFood(Foods foods) {
        FirebaseDatabase.getInstance().getReference("Foods").child(foods.getId() + "")
                .setValue(foods).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        dialogs.dismiss();
                        notifyDataSetChanged();
                        Toast.makeText(context, "Thay đổi thành công", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Fail" + e, Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        dialogs.dismiss();
                    }
                });
    }

    private void openMedia() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activityResultLauncherUpdate.launch(intent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void listFillter(ArrayList<Foods> listSearch) {
        this.list = listSearch;
        notifyDataSetChanged();
    }

    public void setImageUri(Uri imageUri) {
        this.foodUri = imageUri;
        if (imageUri != null) {
            dialogAddFoodBinding.ivFood.setImageURI(imageUri);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvDecr, tvRate, tvCatagory;
        ImageView ivFoodAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitleFood);
            tvPrice = itemView.findViewById(R.id.tvPriceFood);
            tvRate = itemView.findViewById(R.id.tvRate);
            tvDecr = itemView.findViewById(R.id.tvMota);
            tvCatagory = itemView.findViewById(R.id.tvCategoryTitle);
            ivFoodAvatar = itemView.findViewById(R.id.ivAvatarFood);
        }
    }
}
