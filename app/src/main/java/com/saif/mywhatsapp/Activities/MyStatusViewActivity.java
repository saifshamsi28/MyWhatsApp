package com.saif.mywhatsapp.Activities;

import android.content.ClipData;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.saif.mywhatsapp.Adapters.MyStatusAdapter;
import com.saif.mywhatsapp.Models.UserStatus;
import com.saif.mywhatsapp.R;
import com.saif.mywhatsapp.StatusUpdateCallback;
import com.saif.mywhatsapp.databinding.ActivityMyStatusViewBinding;

public class MyStatusViewActivity extends AppCompatActivity {
    ActivityMyStatusViewBinding myStatusViewBinding;
    MyStatusAdapter myStatusAdapter;
    private static StatusUpdateCallback statusUpdateCallback;

    public static void setStatusUpdateCallback(StatusUpdateCallback callback) {
        statusUpdateCallback = callback;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        myStatusViewBinding=ActivityMyStatusViewBinding.inflate(getLayoutInflater());
        setContentView(myStatusViewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_status_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        this.setTitle("My status");
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.GreenishBlue));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        UserStatus userStatus= (UserStatus) getIntent().getSerializableExtra("myStatuses");

        if(userStatus!=null) {
            myStatusAdapter = new MyStatusAdapter(this, userStatus,statusUpdateCallback);
            myStatusViewBinding.myStatusRecyclerView.setAdapter(myStatusAdapter);
        }
        setThemeForHomeScreen();
    }
    private  void setThemeForHomeScreen() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.GreenishBlue));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}