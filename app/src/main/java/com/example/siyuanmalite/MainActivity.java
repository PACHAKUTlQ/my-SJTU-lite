package com.example.siyuanmalite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.siyuanmalite.databinding.ActivityMainBinding;
import com.example.siyuanmalite.helpers.SettingsHelper;

public class MainActivity extends FragmentActivity {

    private TextView mTextView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart()
    {
        super.onStart();
//        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
//        startActivity(intent);
//        LayoutInflater inflater = LayoutInflater.from(this);
//        inflater.inflate(R.layout.login_view, binding.container,true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        UnicodeFragment f = new UnicodeFragment();
        fragmentTransaction.add(R.id.container, f);

        fragmentTransaction.show(f);
        fragmentTransaction.commit();
    }
}