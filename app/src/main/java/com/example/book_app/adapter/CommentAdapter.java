package com.example.book_app.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.book_app.MyApplication;
import com.example.book_app.R;
import com.example.book_app.databinding.RowCommentBinding;
import com.example.book_app.models.CommentModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    @NonNull

    RowCommentBinding binding;
    FirebaseAuth auth;

    Context context;
    ArrayList<CommentModel> commentModelArrayList;

    public CommentAdapter(android.content.Context context, ArrayList<CommentModel> commentModelArrayList) {
        this.context = context;
        this.commentModelArrayList = commentModelArrayList;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        CommentModel model = commentModelArrayList.get(position);

        String id = model.getId();
        String comment = model.getComment();
        String uid = model.getUid();
        String timestamp = model.getTimestamp();
        String bookId = model.getBookId();

        String dateFormat =MyApplication.formatTimestamp(Long.parseLong(timestamp));

        holder.dateTv.setText(dateFormat);
        holder.commentTv.setText(comment);

        loadUserDetail(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteComment(model);
            }
        });
    }

    private void deleteComment( CommentModel model) {
        String bookId = model.getBookId();
        String id = model.getId();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete comment")
                .setMessage("Are you sure Are you want to delete comment ?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(""+bookId)
                                .child("comment")
                                .child(""+id)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }



    private void loadUserDetail(CommentModel model, ViewHolder holder) {

        auth = FirebaseAuth.getInstance();

        String uid = auth.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nameProfile = ""+ snapshot.child("name").getValue();
                String imageProfile = ""+snapshot.child("profileImage");

                holder.profileNameTv.setText(nameProfile);

                try{
                    Glide.with(context)
                            .load(imageProfile)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileImBtn);
                }catch(Exception e){
                    holder.profileImBtn.setImageResource(R.drawable.ic_person_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView profileImBtn;
        TextView dateTv, profileNameTv, commentTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImBtn = binding.imageProfile;
            dateTv = binding.dateComment;
            profileNameTv = binding.nameProfile;
            commentTv = binding.commentTv;

        }
    }
}
