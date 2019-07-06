package com.reyzeny.twist;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.security.Key;

public class MainActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment inboxFragment;
    Fragment sentFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Your username: " + LocalData.getUserName(this));
        if (!LocalData.isUserSetupDone(this)){
            showLogin();
            finish();
            return;
        };
        initComponents();
    }

    private void initComponents() {
        inboxFragment = new InboxFragment();
        sentFragment = new SentFragment();

        fragmentManager = getSupportFragmentManager();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewMessage.class);
                startActivity(intent);
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_inbox:
                        showInboxFragment();
                        return true;
                    case R.id.navigation_sent:
                        showSentFragment();
                        return true;
                }
                return false;
            }
        });
        showInboxFragment();
    }

    private void showInboxFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_container, inboxFragment);
        fragmentTransaction.commit();
    }

    private void showSentFragment() {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_container, sentFragment);
        fragmentTransaction.commit();
    }

    private void LoadMessages() {

    }
    private void showLogin() {
        startActivity(new Intent(this, Auth.class));
    }


}
