package com.saif.mywhatsapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Objects;

public class UserStatusObserver implements DefaultLifecycleObserver {

    private final Context context;
    private final FirebaseDatabase database;

    public UserStatusObserver(Context context) {
        this.context = context;
        this.database = FirebaseDatabase.getInstance();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        setUserCurrentStatus("online");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        setUserCurrentStatus("offline");
        setLastSeen(System.currentTimeMillis());
    }

    private void setUserCurrentStatus(String status) {
        HashMap<String, Object> statusHashMap = new HashMap<>();
        statusHashMap.put("status", status);

        if ("offline".equals(status)) {
            statusHashMap.put("lastSeen", System.currentTimeMillis());
        }

        database.getReference().child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .updateChildren(statusHashMap);
    }

    private void setLastSeen(long lastSeen) {
        database.getReference().child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .child("lastSeen")
                .setValue(lastSeen);
    }

    public void setUserTypingStatus(String status) {
        database.getReference().child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .child("status")
                .setValue(status);
    }
}
