package com.example.fitlife_sumyatnoe.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitlife_sumyatnoe.fragments.HealthFragment;
import com.example.fitlife_sumyatnoe.fragments.TodayFragment;

public class HomePagerAdapter extends FragmentStateAdapter {

    private TodayFragment todayFragment;
    private HealthFragment healthFragment;

    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        todayFragment = new TodayFragment();
        healthFragment = new HealthFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return todayFragment;
        } else {
            return healthFragment;
        }
    }

    public Fragment getFragmentAtPosition(int position) {
        if (position == 0) {
            return todayFragment;
        } else {
            return healthFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}