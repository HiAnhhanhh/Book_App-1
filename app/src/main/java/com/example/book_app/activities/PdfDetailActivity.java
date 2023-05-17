package com.example.book_app.activities;

import static java.lang.Long.parseLong;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.book_app.MyApplication;
import com.example.book_app.databinding.ActivityPdfDetailBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    ActivityPdfDetailBinding binding;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    String bookId, bookTitle, bookUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        binding.downloadBookBtn.setVisibility(View.GONE);
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                if(ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    MyApplication.downloadBook(PdfDetailActivity.this, bookId, bookUrl,bookTitle );
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted , can download book ");
                }else{
                    Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request permission");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        loadBookDetail ();
        MyApplication.incrementBookViewCount(bookId);
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent (PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookID", bookId);
                startActivity(intent1);

            }
        });

    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                        if(isGranted){
                            Log.d(TAG_DOWNLOAD,"Permission Granted");
                            MyApplication.downloadBook(this, ""+bookId,""+ bookUrl,""+bookTitle );
                        }else{
                            Log.d(TAG_DOWNLOAD, "Permission was denied ...");
                            Toast.makeText(this, "Permission was denied ...", Toast.LENGTH_SHORT).show();
                        }
                    });

    private void loadBookDetail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookUrl = ""+snapshot.child("url").getValue();
                        bookTitle = ""+ snapshot.child("title").getValue();
                        String categoryId = ""+snapshot.child("categoryID").getValue();
                        String timestamp = ""+ snapshot.child("timestamp").getValue();
                        String des = ""+ snapshot.child("description").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadsCount = ""+ snapshot.child("downloadsCount").getValue();
                        String date = MyApplication.formatTimestamp(parseLong(timestamp));

                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        MyApplication.loadPdfSize(
                                ""+ bookUrl,
                                ""+ bookTitle,
                                binding.sizeTv
                        );

                        MyApplication.loadPdfFromUrl(
                                ""+ bookUrl,
                                ""+ bookTitle,
                                binding.pdfView,
                                binding.progressbar
                        );


                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );

                        binding.bookTitleTv.setText(bookTitle);
                        binding.descriptionTv.setText(des);
                        binding.dateTv.setText(date);
                        binding.viewTv.setText(viewCount.replace("null","N/A"));
                        binding.downloadTv.setText(downloadsCount.replace("null","N/A"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}