package com.ammar.filescenter.activities.MainActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.ammar.filescenter.R;
import com.ammar.filescenter.activities.MainActivity.fragments.ReceiveFragment;
import com.ammar.filescenter.activities.MainActivity.fragments.SendFragment;
import com.ammar.filescenter.activities.SharingActivity;
import com.ammar.filescenter.services.NetworkService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton serverButton;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initItems();
        setItemsListener();
        initStates();
        observeStates();
    }

    private void initItems() {
        bottomNavigationView = findViewById(R.id.BottomNavView);
        serverButton = findViewById(R.id.FAB_ServerButton);
        bottomNavigationView.setSelectedItemId(R.id.B_Share);
    }


    private int currentFragmentIndex = 1;
    private final ArrayList<Class> fragments = new ArrayList<>(Arrays.asList(new Class[]{
            ReceiveFragment.class,
            SendFragment.class
    }));


    private void setItemsListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();

            int nextFragmentIndex = -1;
            if (id == R.id.B_Receive) {
                nextFragmentIndex = fragments.indexOf(ReceiveFragment.class);
                if (currentFragmentIndex > nextFragmentIndex) {
                    ft.setCustomAnimations(R.anim.fragment_enter_right, R.anim.fragment_exit_right);
                }
                ft.replace(R.id.MainActivityFragmentContainer, ReceiveFragment.class, null);
            } else if (id == R.id.B_Share) {
                nextFragmentIndex = fragments.indexOf(SendFragment.class);
                if (currentFragmentIndex < nextFragmentIndex) {
                    ft.setCustomAnimations(R.anim.fragment_enter_left, R.anim.fragment_exit_left);
                }
                ft.replace(R.id.MainActivityFragmentContainer, SendFragment.class, null);
            } else if (id == R.id.B_Home) {
                startActivity(new Intent(this, SharingActivity.class));
            } else if (id == R.id.B_Settings) {
                return false;
            }
            currentFragmentIndex = nextFragmentIndex;
            ft.commit();
            return true;
        });
        serverButton.setOnClickListener((button) -> {
            Intent serviceIntent = new Intent(this, NetworkService.class);
            serviceIntent.setAction(NetworkService.ACTION_TOGGLE_SERVER);
            startService(serviceIntent);
        });

    }

    private void initStates() {
        Intent serviceIntent = new Intent(this, NetworkService.class);
        serviceIntent.setAction(NetworkService.ACTION_GET_SERVER_STATUS);
        startService(serviceIntent);
    }
    private void observeStates() {
        NetworkService.serverStatusObserver.observe(this, running -> {
            if(running) {
                serverButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.status_on)));
            } else {
                serverButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.status_off)));
            }
        });
    }

}