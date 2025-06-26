package com.example.bdapps;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileViewTabsAdapter extends FragmentStateAdapter {
    public ProfileViewTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0){
            return new AboutFragmentProfileView();
        }
        else if(position == 1){
            return new PostsFragmentProfileView();
        }
        else {
            return  new ActivityFragmentProfileView();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
