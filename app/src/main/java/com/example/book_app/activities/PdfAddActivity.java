package com.example.book_app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.book_app.databinding.ActivityCategoryBinding;
import com.example.book_app.databinding.ActivityPdfAddBinding;
import com.example.book_app.models.CategoryModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;

    private FirebaseAuth firebaseAuth;
    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;
    private ProgressDialog progressDialog;
    private ArrayList<CategoryModel> categoryModelArrayList;

    private Uri pdfUri = null;
    private static final String TAG = "ADD_PDF_TAG";
    private static final int PDF_PICK_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        loadCategories();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    String bookTitle ="", bookDes ="";

    private void validateData() {
        bookTitle = binding.bookTitleEt.getText().toString();
        bookDes = binding.bookDesEt.getText().toString();

        if(TextUtils.isEmpty(bookTitle)){
            Toast.makeText(this, "Enter Book Title", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(bookDes)) {
            Toast.makeText(this, "Enter Book Description", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Pick category", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick Pdf", Toast.LENGTH_SHORT).show();
        }else{
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "UploadPdfToStorage : Loading Pdf categories");

        progressDialog.setMessage("Uploading Pdf ...");
        progressDialog.show();

        long timeStamp = System.currentTimeMillis();
        String filePathAndName = "Book/" + timeStamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Log.d(TAG, "onSuccess : PDF uploaded to Storage");
                Log.d(TAG, "onSuccess : getting PDF url");
                Task<Uri>  uriTask =  taskSnapshot.getStorage().getDownloadUrl();

                while(!uriTask.isComplete());
                String uploadedPdfUrl = ""+uriTask.getResult();


                uploadPdfInfoToDb(uploadedPdfUrl,timeStamp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure : PDF upload failed due to" + e.getMessage());
                Toast.makeText(PdfAddActivity.this, "PDF upload failed due to"+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timeStamp) {
        Log.d(TAG,"uploadPdfToStorage : uploading Pdf info to firebase db");
        progressDialog.setMessage("Loading pdf info ...");
        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+ uid);
        hashMap.put("id",""+timeStamp);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("title",""+ selectedCategoryTitle);
        hashMap.put("description",""+ bookDes);
        hashMap.put("categoryID",""+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("viewCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess : Successfully uploaded ...");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure : Failed upload to db due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed upload to db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategories() {
        Log.d(TAG, "loadPdfCategories : Loading Pdf categories ....");

        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    String categoryTitle = ""+ds.child("category").getValue();
                    String categoryId = ""+ds.child("id").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);

                    Log.d(TAG, "onDataChange : "+categoryTitle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");
        String categoriesArray[] = new String[categoryTitleArrayList.size()];
        for (int i=0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        binding.categoryTv.setText(selectedCategoryTitle);
                        Log.d(TAG, "onClick : Selected Category" + selectedCategoryId + " " + selectedCategoryTitle);
                    }
                })
                .show();

    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent ");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"),PDF_PICK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK && requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult :PDF Picked");
                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult : URI " + pdfUri);
            }
            else{
                Log.d(TAG, "onActivityResult : cancelled picking pdf");
                Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
            }
    }
}