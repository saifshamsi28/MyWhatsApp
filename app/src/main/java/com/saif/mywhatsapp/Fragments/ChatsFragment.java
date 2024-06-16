package com.saif.mywhatsapp.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saif.mywhatsapp.Activities.MainActivity;
import com.saif.mywhatsapp.Adapters.UserAdapter;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.databinding.FragmentChatsBinding;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding fragmentChatsBinding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ArrayList<User> users;
    private UserAdapter userAdapter;

    public void onResume() {
        super.onResume();

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("MyWhatsApp");
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentChatsBinding = FragmentChatsBinding.inflate(inflater, container, false);
        return fragmentChatsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userAdapter = new UserAdapter(requireContext(), users);

        fragmentChatsBinding.shimmer.startShimmer();
        fragmentChatsBinding.shimmer.showShimmer(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        fragmentChatsBinding.chatRecyclerView.setLayoutManager(layoutManager);
        fragmentChatsBinding.chatRecyclerView.setAdapter(userAdapter);

        loadUsers();
    }

    private void loadUsers() {

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                fragmentChatsBinding.shimmer.stopShimmer();
                fragmentChatsBinding.shimmer.setVisibility(View.GONE);
                fragmentChatsBinding.chatRecyclerView.setVisibility(View.VISIBLE);
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                User currentUser=null;
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    User user = userSnap.getValue(User.class);
                    if (user != null) {
                        if (user.getUid().equals(currentUserId)) {
                            MainActivity.currentUserName=user.getName();
                            MainActivity.currentUserProfile=user.getProfileImage();
                            MainActivity.aboutCurrentUser=user.getAbout();
                            user.setName(user.getName() + " (you)");
                            users.add(0, user);
                        }else {
                            users.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentChatsBinding = null;
    }
}
