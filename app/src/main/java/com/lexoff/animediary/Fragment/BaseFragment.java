package com.lexoff.animediary.Fragment;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment implements OnBackPressed {

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
