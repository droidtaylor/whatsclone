package com.verbosetech.whatsclone.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.verbosetech.whatsclone.R;
import com.verbosetech.whatsclone.adapters.ViewPagerAdapter;
import com.verbosetech.whatsclone.interfaces.OnUserDetailFragmentInteraction;

public class UserMediaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    private OnUserDetailFragmentInteraction mListener;

    public UserMediaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_media, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        for (int i = 0; i < 3; i++) {
            String title = null;
            switch (i) {
                case 0:
                    title = getString(R.string.media);
                    break;
                case 1:
                    title = getString(R.string.audio);
                    break;
                case 2:
                    title = getString(R.string.docs);
                    break;
            }

            UserSubMediaFragment fragment = UserSubMediaFragment.newInstance(i);
            fragment.setAttachment(mListener.getAttachments(i));
            viewPagerAdapter.addFrag(fragment, title);
        }
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserDetailFragmentInteraction) {
            mListener = (OnUserDetailFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnUserDetailFragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
