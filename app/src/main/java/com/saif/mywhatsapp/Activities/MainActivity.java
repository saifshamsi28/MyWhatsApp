package com.saif.mywhatsapp.Activities;

//import static com.google.firebase.database.core.operation.OperationSource.Source.User;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.saif.mywhatsapp.Adapters.UserAdapter;
import com.saif.mywhatsapp.Adapters.ViewPagerAdapter;
import com.saif.mywhatsapp.AppDatabase;
import com.saif.mywhatsapp.DatabaseClient;
import com.saif.mywhatsapp.Fragments.ChatsFragment;
import com.saif.mywhatsapp.Models.User;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    public static String currentUserName,currentUserProfile,aboutCurrentUser;
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

        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        appDatabase= DatabaseClient.getInstance(this).getAppDatabase();
        users=new ArrayList<>();
        userAdapter=new UserAdapter(this,users);
        recyclerView=findViewById(R.id.main_recyclerview);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            Fragment selectedFragment = null;
//            if(item.getItemId()==R.id.status) {
//                selectedFragment = new StatusFragment();
//            } else if (item.getItemId()==R.id.calls) {
//                selectedFragment = new CallsFragment();
//            }else{
//                selectedFragment = new ChatsFragment();
//            }
//            if (selectedFragment != null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, selectedFragment)
//                        .commit();
//            }
//            return true;
//        });
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

//        currentUser=appDatabase.userDao().getUserByUid(auth.getUid());

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
                if (item.getItemId()==R.id.navigation_chat) {
                    viewPager.setCurrentItem(0);
                    return true;
                }else if( item.getItemId()==R.id.navigation_status) {
                    viewPager.setCurrentItem(1);
                    return true;
                }else if(item.getItemId()== R.id.navigation_calls){
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


//        // Set initial fragment
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, new ChatsFragment())
//                    .commit();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                recyclerView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void searchUsers(String query) {
        database.getReference().child("Users").orderByChild("name")
                .startAt(query).endAt(query + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<User> searchResults = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user != null && !user.getUid().equals(auth.getCurrentUser().getUid())) {
                                searchResults.add(user);
                            }
                        }
                        users.addAll(searchResults);
                        showSearchResults(searchResults);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Error searching users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSearchResults(List<User> searchResults) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("Search Results");

//        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setVisibility(View.VISIBLE);
        UserAdapter searchAdapter = new UserAdapter(MainActivity.this, users);
        recyclerView.setAdapter(searchAdapter);

//        builder.setView(recyclerView);
//        AlertDialog dialog = builder.create();
//        dialog.show();

        searchAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                addUserToLocalDatabase(user);
//                Toast.makeText(MainActivity.this,"new user is clicked",Toast.LENGTH_SHORT);
//                Intent intent=new Intent(MainActivity.this, ChatsFragment.class);
//                intent.putExtra("Contact_name",user.getName());
//                intent.putExtra("chat_profile", user.getProfileImage());
//                intent.putExtra("number",user.getPhoneNumber().toString());
//                intent.putExtra("uid",user.getUid());
//                startActivity(intent);
                recyclerView.setVisibility(View.GONE);
//                dialog.dismiss();
            }
        });
    }

    private void addUserToLocalDatabase(User user) {
        executor.execute(() -> {
            appDatabase.userDao().insertUser(user);
            mainHandler.post(() -> {
                users.add(user);
                userAdapter.notifyDataSetChanged();
            });
        });
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.setting==item.getItemId()) {
            Toast.makeText(this, "setting clicked", Toast.LENGTH_SHORT).show();
        } else if (R.id.groups== item.getItemId()) {
            Toast.makeText(this, "Groups clicked", Toast.LENGTH_SHORT).show();
        } else if (R.id.logout==item.getItemId()) {
            auth.signOut();
            Intent intent=new Intent(MainActivity.this, SignUpLoginActivity.class);
            startActivity(intent);
//            Toast.makeText(this,"Logged out from "+currentUserName,Toast.LENGTH_SHORT).show();
        } else if (R.id.profile==item.getItemId()) {
            Intent intent=new Intent(MainActivity.this, SetUpProfileActivity.class);
            intent.putExtra("source","MainActivity");
            intent.putExtra("name",currentUserName);
            intent.putExtra("about",aboutCurrentUser);
            intent.putExtra("profileUri",currentUserProfile);
            startActivity(intent);
        } else if(item.getItemId()==R.id.search) {
            Toast.makeText(this, "search clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
