package com.example.book_app.activities;

import static java.lang.Long.parseLong;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.IconMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.book_app.MyApplication;
import com.example.book_app.R;
import com.example.book_app.adapter.CommentAdapter;
import com.example.book_app.databinding.ActivityPdfDetailBinding;
import com.example.book_app.databinding.DialogAddCommentBinding;
import com.example.book_app.models.CommentModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    ActivityPdfDetailBinding binding;

    ProgressDialog progressDialog;

    FirebaseAuth auth;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    String bookId, bookTitle, bookUrl;

    private ArrayList<CommentModel> commentModelArrayList;
    private CommentAdapter commentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait ...");

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
        loadComment();
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


        binding.addCommentIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment = binding.commentEt.getText().toString().trim();
                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this, "Enter Comment ...", Toast.LENGTH_SHORT).show();
                }else{
                    addComment();

                }
            }
        });

//        binding.addCommentIb.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addCommentDialog();
//            }
//        });

    }

    String comment = "";
//    private void addCommentDialog() {
//        DialogAddCommentBinding addCommentBinding = DialogAddCommentBinding.inflate(LayoutInflater.from(this));
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
//
//        builder.setView(addCommentBinding.getRoot());
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//
//        addCommentBinding.backBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
//            }
//        });
//
//        addCommentBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                comment = addCommentBinding.CommentEt.getText().toString().trim();
//                if(TextUtils.isEmpty(comment)){
//                    Toast.makeText(PdfDetailActivity.this, "Enter Comment ...", Toast.LENGTH_SHORT).show();
//                }else{
//                    alertDialog.dismiss();
//                    addComment();
//                }
//            }
//        });
//
//    }

    private void loadComment() {
        binding.commentRec.setLayoutManager(new LinearLayoutManager(this));
        commentModelArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books").child(bookId).child("comment");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentModelArrayList.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){

                            CommentModel model = ds.getValue(CommentModel.class);
                            commentModelArrayList.add(model);

                        }
                        commentAdapter = new CommentAdapter(PdfDetailActivity.this,commentModelArrayList);

                        binding.commentRec.setAdapter(commentAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    String uid;
    String timestamp;
//    String commentEt;


    private void addComment() {
        progressDialog.setMessage("Upload comment");
        progressDialog.show();
        timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment",""+comment);
        hashMap.put("UserId",""+uid);
        hashMap.put("BookId", ""+bookId);
        hashMap.put("Id",""+timestamp);
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("comment").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Added", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        binding.commentEt.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment due to"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
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

//                        MyApplication.loadPdfFromUrl(
//                                ""+ bookUrl,
//                                ""+ bookTitle,
//                                binding.pdfView,
//                                binding.progressbar
//                        );


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