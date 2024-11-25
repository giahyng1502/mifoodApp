package com.fpoly.nhom2.mifoodapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import FPT.PRO1122.Nhom3.DuAn1.Fragment.ChoXacNhan;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.DangGiao;
import FPT.PRO1122.Nhom3.DuAn1.Fragment.HoanThanh;

public class MyOrderPagerAdapter extends FragmentStateAdapter {
    public MyOrderPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChoXacNhan();
            case 1:
                return new DangGiao();
            case 2:
                return new HoanThanh();
            default:
                return new ChoXacNhan();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}
