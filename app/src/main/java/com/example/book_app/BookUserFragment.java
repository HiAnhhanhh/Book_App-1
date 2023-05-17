package com.example.book_app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.book_app.adapter.PdfUserAdapter;
import com.example.book_app.databinding.FragmentBookUserBinding;
import com.example.book_app.models.PdfModel;
import com.google.api.LogDescriptor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.signature.qual.FieldDescriptorWithoutPackage;
import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class BookUserFragment extends Fragment {


    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<PdfModel> pdfModelArrayList;
    private PdfUserAdapter pdfUserAdapter;

    public static final String TAG = "BOOK_USER_TAG";
    FragmentBookUserBinding binding;

    public BookUserFragment() {
        // Required empty public constructor
    }


    public static BookUserFragment newInstance(String categoryId, String uid, String category) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("uid", uid);
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category");
            categoryId = getArguments().getString("categoryId");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBookUserBinding.inflate(getLayoutInflater(), container, false);
        Log.d(TAG, "onCreateView: category" + category);

        if (category.equals("All")){
            loafAllBook();
        }else if (category.equals("Most View")){
            loadMostViewBook("viewCount");
        }else if(category.equals("Most Download")){
            loadMostDownloadBook("downloadsCount");
        }else{
            loadCategoriesBook();
        }
        
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    pdfUserAdapter.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+ e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        
        return binding.getRoot();
    }

    private void loadCategoriesBook() {
        pdfModelArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfModelArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            PdfModel model = ds.getValue(PdfModel.class);
                            pdfModelArrayList.add(model);
                        }
                        pdfUserAdapter = new PdfUserAdapter(getContext(), pdfModelArrayList);
                        binding.bookRec.setAdapter(pdfUserAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMostDownloadBook(String orderBy) {
        pdfModelArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfModelArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PdfModel model = ds.getValue(PdfModel.class);
                    pdfModelArrayList.add(model);
                }

                pdfUserAdapter = new PdfUserAdapter(getContext(), pdfModelArrayList);
                binding.bookRec.setAdapter(pdfUserAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewBook(String orderBy) {
        pdfModelArrayList = new ArrayList<>();
         DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
         ref.orderByChild(orderBy).limitToLast(10).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 pdfModelArrayList.clear();
                 for (DataSnapshot ds : snapshot.getChildren()){
                     PdfModel model = ds.getValue(PdfModel.class);
                     pdfModelArrayList.add(model);
                 }

                 pdfUserAdapter = new PdfUserAdapter(getContext(), pdfModelArrayList);
                 binding.bookRec.setAdapter(pdfUserAdapter);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }

    private void loafAllBook() {
        pdfModelArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfModelArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PdfModel model = ds.getValue(PdfModel.class);
                    pdfModelArrayList.add(model);
                }

                pdfUserAdapter = new PdfUserAdapter(getContext(), pdfModelArrayList);
                binding.bookRec.setAdapter(pdfUserAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    
}