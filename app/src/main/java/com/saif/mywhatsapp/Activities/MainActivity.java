package com.saif.mywhatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.saif.mywhatsapp.Fragments.CallsFragment;
import com.saif.mywhatsapp.Fragments.ChatsFragment;
import com.saif.mywhatsapp.Fragments.StatusFragment;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    FirebaseAuth auth;
    public static String currentUserName,currentUserProfile,aboutCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));

        auth = FirebaseAuth.getInstance();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if(item.getItemId()==R.id.status) {
                selectedFragment = new StatusFragment();
            } else if (item.getItemId()==R.id.calls) {
                selectedFragment = new CallsFragment();
            }else{
                selectedFragment = new ChatsFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top, menu);
        return super.onCreateOptionsMenu(menu);
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
        } else {
            Toast.makeText(this, "Invite clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
