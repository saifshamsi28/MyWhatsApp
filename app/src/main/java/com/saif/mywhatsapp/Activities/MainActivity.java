package com.saif.mywhatsapp.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.saif.mywhatsapp.Adapters.ViewPagerAdapter;
import com.saif.mywhatsapp.Database.AppDatabase;
import com.saif.mywhatsapp.Database.DatabaseClient;
import com.saif.mywhatsapp.Fragments.ChatsFragment;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.UserStatusObserver;
import com.saif.mywhatsapp.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Objects;
import androidx.lifecycle.ProcessLifecycleOwner;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    FirebaseDatabase database;
    public static String currentUserName, currentUserProfile, aboutCurrentUser;
    ViewPager2 viewPager;
    AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

            if (setThemeForHomeScreen() == 2)
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
            else
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));


//            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            appDatabase = DatabaseClient.getInstance(this).getAppDatabase();

            BottomNavigationView bottomNavigationView;
            viewPager = findViewById(R.id.viewPager);
            bottomNavigationView = findViewById(R.id.bottomNavigationView);

            ViewPagerAdapter adapter = new ViewPagerAdapter(this);
            viewPager.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        //to check online/offline status of user
            UserStatusObserver appLifecycleObserver = new UserStatusObserver(this);
            ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);

//            executor.execute(() -> {
//                if (appDatabase != null) {
//                    currentUser = appDatabase.userDao().getUserByUid(auth.getUid());
//                    if (currentUser != null) {
//                        mainHandler.post(() -> {
//                            currentUserName = currentUser.getName();
//                            currentUserProfile = currentUser.getProfileImage();
//                            aboutCurrentUser = currentUser.getAbout();
//                        });
//                    }
//                }
//            });


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
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        if (auth.getCurrentUser() != null) {
//            setFCMTokenOfUser();
//        } else {
//            // Handle user not authenticated
//            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);

            }
        }
    }


    private void setFCMTokenOfUser() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                if (token != null && !token.isEmpty()) {
                    HashMap<String, Object> tokenMap = new HashMap<>();
                    tokenMap.put("fcmToken", token);
//                    FirebaseDatabase.getInstance().getReference().child("Users")
//                            .child(FirebaseAuth.getInstance().getUid())
//                            .updateChildren(tokenMap);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to retrieve FCM token: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_top, menu);
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
            Toast toast= Toast.makeText(this, "Groups clicked", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        } else if (R.id.logout == item.getItemId()) {

            Dialog dialog=new Dialog(this);
            dialog.setContentView(R.layout.logout_confirmation_dialog);
            TextView accountName=dialog.findViewById(R.id.account_name);
            CircleImageView accountProfile=dialog.findViewById(R.id.account_profile);
            accountProfile.setVisibility(View.VISIBLE);
            Glide.with(this).load(currentUserProfile)
                    .placeholder(R.drawable.avatar)
                            .into(accountProfile);
            accountName.setText(currentUserName);

            dialog.setCanceledOnTouchOutside(true);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);

            dialog.show();

            dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accountProfile.setVisibility(View.GONE);
//                    Toast.makeText(MainActivity.this, "Logout cancelled ", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Logging out from "+currentUserName, Toast.LENGTH_SHORT).show();
                    deleteFCMTokenAndSignOut();
                    accountProfile.setVisibility(View.GONE);
                    dialog.dismiss();
                }
            });

//            deleteFCMTokenAndSignOut(auth.getUid());
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

    private void deleteFCMTokenAndSignOut() {
//        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                auth.signOut();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MainActivity.this, SignUpLoginActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//            }
//        });
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
