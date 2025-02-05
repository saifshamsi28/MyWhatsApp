package com.saif.mywhatsapp.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saif.mywhatsapp.Adapters.UserAdapter;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.FragmentChatsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding fragmentChatsBinding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ArrayList<User> users;
    private UserAdapter userAdapter;
    private AppDatabase appDatabase;
    private TextView nameTextView;
    private TextView aboutTextView;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
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
//        appDatabase=DatabaseClient.getInstance().getAppDatabase();
        userAdapter = new UserAdapter(requireContext(), users,true);

        fragmentChatsBinding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        fragmentChatsBinding.chatRecyclerView.setAdapter(userAdapter);

        nameTextView = fragmentChatsBinding.getRoot().findViewById(R.id.contact_name);
        aboutTextView = fragmentChatsBinding.getRoot().findViewById(R.id.about_user);

        // Initialize database
        initializeDatabase();

        // Fetch user from Room database
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        executor.execute(() -> {
            if (appDatabase != null) {
                User user = appDatabase.userDao().getUserByUid(uid);
                if (user != null) {
                    mainHandler.post(() -> {
                        users.clear();
                        users.add(user);
                        userAdapter.notifyDataSetChanged();
                    });
                }
            }
        });

        // Fetch updated user data from Firebase and update Room database
        database.getReference().child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User firebaseUser = snapshot.getValue(User.class);
                if (firebaseUser != null && appDatabase != null) {
                    executor.execute(() -> {
                        if (appDatabase.userDao().userExists(firebaseUser.getUid()) == 0) {
                            appDatabase.userDao().insertUser(firebaseUser);
                            mainHandler.post(() -> {
                                users.clear();
                                users.add(firebaseUser);
                                userAdapter.notifyDataSetChanged();
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "chatFragment Error fetching user from Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        loadUsers();
    }

    private void initializeDatabase() {
        // Initialize the AppDatabase using DatabaseClient
        DatabaseClient databaseClient = DatabaseClient.getInstance(getContext());
        if (databaseClient != null) {
            appDatabase = databaseClient.getAppDatabase();
        } else {
            Log.e("ChatsFragment", "DatabaseClient instance is null");
            Toast.makeText(getContext(), "Database initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUsers() {
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        executor.execute(() -> {
            List<User> userList = appDatabase.userDao().getAllUsers(); // Fetch all users from local database
            mainHandler.post(() -> {
                if (userList.size() != users.size()) {
                    users.clear();
                    users.addAll(userList);
                    userAdapter.notifyDataSetChanged();
                }
            });
        });

        // Listen for Firebase data changes to update local database
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> firebaseUsers = new ArrayList<>();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    User user = userSnap.getValue(User.class);
                    if (user != null) {
                        if (!user.getUid().equals(currentUserId)) {
                            firebaseUsers.add(user);
                        } else {
                            firebaseUsers.add(0, user);
                        }
                    }
                }
                executor.execute(() -> {
                    appDatabase.userDao().insertAllUsers(firebaseUsers); // Bulk insert to local database
                    mainHandler.post(() -> {
                        users.clear();
                        users.addAll(firebaseUsers); // Update UI list
                        userAdapter.notifyDataSetChanged();
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("loadUsers chatFragment", "Firebase error: " + error.getMessage());
                Log.e("loadUsers chatFragment", "Firebase error details: " + error.getDetails());
                Log.e("loadUsers chatFragment", "Firebase error code: " + error.getCode());
                Toast.makeText(getContext(), "Error fetching users from Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void searchUsers(String query) {
        String userInput = query.toLowerCase();
        List<User> filteredUsers = new ArrayList<>();

        for (User user : users) {
            if (user.getName().toLowerCase().contains(userInput) || user.getPhoneNumber().toLowerCase().contains(userInput)) {
                filteredUsers.add(user);
            }
        }

        UserAdapter userAdapter1 = new UserAdapter(getContext(), (ArrayList<User>) filteredUsers,true);
        fragmentChatsBinding.chatRecyclerView.setAdapter(userAdapter1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentChatsBinding = null;
    }
}
