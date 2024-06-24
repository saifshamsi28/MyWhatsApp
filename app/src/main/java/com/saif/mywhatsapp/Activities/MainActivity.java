package com.saif.mywhatsapp.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.saif.mywhatsapp.Adapters.UserAdapter;
import com.saif.mywhatsapp.Adapters.ViewPagerAdapter;
import com.saif.mywhatsapp.AppDatabase;
import com.saif.mywhatsapp.DatabaseClient;
import com.saif.mywhatsapp.Fragments.ChatsFragment;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.UserStatusObserver;
import com.saif.mywhatsapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import androidx.lifecycle.ProcessLifecycleOwner;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    public static String currentUserName, currentUserProfile, aboutCurrentUser;
    ViewPager2 viewPager;
    AppDatabase appDatabase;
    User currentUser;
    ArrayList<User> users;
    UserAdapter userAdapter;
    RecyclerView recyclerView;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        //to check online/offline status of user
//        UserStatusObserver appLifecycleObserver = new UserStatusObserver(this);
//        ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);

         // Set status bar color based on Day or Night Mode
//            Toast.makeText(this, "user id is null", Toast.LENGTH_SHORT).show();
            if (setThemeForHomeScreen() == 2)
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
            else
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));


            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            appDatabase = DatabaseClient.getInstance(this).getAppDatabase();
            users = new ArrayList<>();
            userAdapter = new UserAdapter(this, users, false);
            recyclerView = findViewById(R.id.main_recyclerview);

            BottomNavigationView bottomNavigationView;
            viewPager = findViewById(R.id.viewPager);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);

            ViewPagerAdapter adapter = new ViewPagerAdapter(this);
            viewPager.setAdapter(adapter);

            //to check online/offline status of user
            UserStatusObserver appLifecycleObserver = new UserStatusObserver(this);
            ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);

            executor.execute(() -> {
                if (appDatabase != null) {
                    currentUser = appDatabase.userDao().getUserByUid(auth.getUid());
                    if (currentUser != null) {
                        mainHandler.post(() -> {
                            currentUserName = currentUser.getName();
                            currentUserProfile = currentUser.getProfileImage();
                            aboutCurrentUser = currentUser.getAbout();
                        });
                    }
                }
            });


            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.navigation_chat) {
                        viewPager.setCurrentItem(0);
                        return true;
                    } else if (item.getItemId() == R.id.navigation_status) {
                        viewPager.setCurrentItem(1);
                        return true;
                    } else if (item.getItemId() == R.id.navigation_calls) {
                        viewPager.setCurrentItem(2);
                        return true;
                    }
                    return false;
                }
            });


            BottomNavigationView finalBottomNavigationView = bottomNavigationView;
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    switch (position) {
                        case 0:
                            finalBottomNavigationView.setSelectedItemId(R.id.navigation_chat);
                            break;
                        case 1:
                            finalBottomNavigationView.setSelectedItemId(R.id.navigation_status);
                            break;
                        case 2:
                            finalBottomNavigationView.setSelectedItemId(R.id.navigation_calls);
                            break;
                    }
                }
            });
            setFCMTokenOfUser();
    }

    private void setFCMTokenOfUser() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
                HashMap<String,Object> tokenMap = new HashMap<>();
                tokenMap.put("fcmToken",token);
                database.getReference().child("Users")
                        .child(auth.getUid())
                        .updateChildren(tokenMap);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Pass search query to ChatsFragment
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                if (currentFragment instanceof ChatsFragment) {
                    ((ChatsFragment) currentFragment).searchUsers(newText);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.setting == item.getItemId()) {
            Toast.makeText(this, "setting clicked", Toast.LENGTH_SHORT).show();
        } else if (R.id.groups == item.getItemId()) {
            Toast.makeText(this, "Groups clicked", Toast.LENGTH_SHORT).show();
        } else if (R.id.logout == item.getItemId()) {
            deleteFCMTokenAndSignOut(auth.getUid());
//            Toast.makeText(this,"Logged out from "+currentUserName,Toast.LENGTH_SHORT).show();
        } else if (R.id.profile == item.getItemId()) {
            Intent intent = new Intent(MainActivity.this, SetUpProfileActivity.class);
            intent.putExtra("source", "MainActivity");
            intent.putExtra("name", currentUserName);
            intent.putExtra("about", aboutCurrentUser);
            intent.putExtra("profileUri", currentUserProfile);
            startActivity(intent);
        } else if (item.getItemId() == R.id.search) {
            Toast.makeText(this, "search clicked", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void deleteFCMTokenAndSignOut(String uid) {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, SignUpLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private int setThemeForHomeScreen() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return 1;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                return 2;
        }
    }
}
