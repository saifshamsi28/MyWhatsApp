package com.saif.mywhatsapp.Adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.saif.mywhatsapp.Fragments.CallsFragment;
import com.saif.mywhatsapp.Fragments.ChatsFragment;
import com.saif.mywhatsapp.Fragments.StatusFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new StatusFragment();
            case 2:
                return new CallsFragment();
            default:
                return new ChatsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

