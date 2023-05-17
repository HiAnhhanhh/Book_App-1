package com.example.book_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.book_app.databinding.ActivityPdfEditBinding;
import com.example.book_app.databinding.ActivityProfileBinding;
import com.example.book_app.databinding.ActivityProfileEditBinding;

public class ProfileEditActivity extends AppCompatActivity {

    ActivityProfileEditBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}