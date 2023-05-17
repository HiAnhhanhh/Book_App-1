package com.example.book_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.book_app.adapter.PdfAdminAdapter;
import com.example.book_app.databinding.ActivityPdfAddBinding;
import com.example.book_app.databinding.ActivityPdfAdminListBinding;
import com.example.book_app.models.PdfModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfAdminListActivity extends AppCompatActivity {

    ActivityPdfAdminListBinding binding;
    ArrayList<PdfModel> pdfModelArrayList;
    PdfAdminAdapter pdfAdminAdapter;
    String categoryID, categoryTitle;

    private static final String TAG = "TAG_LIST_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAdminListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        categoryID = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        loadPdfList();

        binding.titleSubTv.setText(categoryTitle);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               try{
                   pdfAdminAdapter.getFilter().filter(s);
               } catch (Exception e){
                   Log.d(TAG, "onTextChanged :"+e.getMessage());
               }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadPdfList() {
        pdfModelArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryID").equalTo(categoryID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfModelArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            PdfModel model = ds.getValue(PdfModel.class);
                            pdfModelArrayList.add(model);
                            Log.d(TAG, "onDataChange: "+model.getId() +""+model.getTitle());
                        }
                        pdfAdminAdapter = new PdfAdminAdapter(PdfAdminListActivity.this,pdfModelArrayList);
                        binding.bookRec.setAdapter(pdfAdminAdapter);
                        binding.bookRec.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}