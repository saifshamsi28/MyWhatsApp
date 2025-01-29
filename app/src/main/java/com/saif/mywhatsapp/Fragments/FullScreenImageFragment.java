package com.saif.mywhatsapp.Fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

import com.bumptech.glide.Glide;
import com.saif.mywhatsapp.ClickablePhotoView;
import com.saif.mywhatsapp.R;

public class FullScreenImageFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_CONTACT_NAME = "contact_name";

    private String imageUrl;
    private String contactName;
    private GestureDetector gestureDetector;
    private static Context activityContext;
    private View view;

    public static FullScreenImageFragment newInstance(Context context, String imageUrl, String contactName) {
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        ((AppCompatActivity) context).getWindow().setStatusBarColor(ContextCompat.getColor(context, android.R.color.black));
        Bundle args = new Bundle();
        activityContext = context;
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_CONTACT_NAME, contactName);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
            contactName = getArguments().getString(ARG_CONTACT_NAME);
        }

        setEnterTransition(TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.fade));
        setExitTransition(TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.fade));

        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > 100 && Math.abs(velocityY) > 100) {
                    requireActivity().onBackPressed(); // Close fragment on swipe up/down
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_full_profile, container, false);

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ClickablePhotoView photoView = view.findViewById(R.id.photo_view);
        TextView contactNameView = view.findViewById(R.id.contact_name);
        ImageView backButton = view.findViewById(R.id.back_button);

        Glide.with(this)
                .load(imageUrl)
                .into(photoView);

        contactNameView.setText(contactName);

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

//        photoView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (gestureDetector.onTouchEvent(event)) {
//                    return true; // Gesture detected, consume touch event
//                }
//                return v.performClick(); // Delegate touch event to PhotoView for zoom functionality
//            }
//        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out);
        view.startAnimation(animation);
        ((AppCompatActivity) activityContext).getWindow().setStatusBarColor(ContextCompat.getColor(activityContext, R.color.GreenishBlue));
        // Show action bar when fragment is destroyed
        if (setThemeForHomeScreen() == 2)
            ((AppCompatActivity) activityContext).getWindow().setStatusBarColor(ContextCompat.getColor(activityContext, R.color.GreenishBlue));
        else
            ((AppCompatActivity) activityContext).getWindow().setStatusBarColor(ContextCompat.getColor(activityContext, R.color.dark));

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }
    private int setThemeForHomeScreen() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
//                mainBinding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.night_color_background));
                return 1;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                return 2;
        }
    }
}
