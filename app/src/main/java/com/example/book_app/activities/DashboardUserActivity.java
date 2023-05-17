package com.example.book_app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.view.View;

import com.example.book_app.BookUserFragment;
import com.example.book_app.databinding.ActivityDashboardUserBinding;
import com.example.book_app.models.CategoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class DashboardUserActivity extends AppCompatActivity {

    public ViewPagerAdapter viewPagerAdapter;
    // to show tabs
    public ArrayList<CategoryModel> categoryModelArrayList;

    private ActivityDashboardUserBinding binding;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);


        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardUserActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupViewPagerAdapter (ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

        categoryModelArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                categoryModelArrayList.clear();
                CategoryModel modelAll = new CategoryModel("All","01","",1);
                CategoryModel modelMostView = new CategoryModel("Most View","02","",1);
                CategoryModel modelMostDownload = new CategoryModel("Most Download","03","",1);

                categoryModelArrayList.add(modelAll);
                categoryModelArrayList.add(modelMostView);
                categoryModelArrayList.add(modelMostDownload);

                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+ modelAll.getId(),
                        ""+modelAll.getUid(),
                        ""+ modelAll.getCategory()
                ), modelAll.getCategory());

                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostView.getId(),
                        ""+ modelMostView.getUid(),
                        ""+modelMostView.getCategory()
                ), modelMostView.getCategory());

                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+ modelMostDownload.getId(),
                        ""+ modelMostDownload.getUid(),
                        ""+ modelMostDownload.getCategory()
                ), modelMostDownload.getCategory());

               viewPagerAdapter.notifyDataSetChanged();

                for(DataSnapshot ds : snapshot.getChildren()){
                    CategoryModel model = ds.getValue(CategoryModel.class);
                    categoryModelArrayList.add(model);

                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            ""+ model.getId(),
                            ""+ model.getUid(),
                            ""+model.getCategory()
                    ), model.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<BookUserFragment> bookUserFragmentArrayList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context= context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return bookUserFragmentArrayList.get(position);
        }

        @Override
        public int getCount() {
            return bookUserFragmentArrayList.size();
        }

        private void addFragment (BookUserFragment fragment, String Title){
            bookUserFragmentArrayList.add(fragment);
            fragmentTitleList.add(Title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser == null){
            startActivity(new Intent (this, MainActivity.class));
        }else{
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);
        }
    }
}