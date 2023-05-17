package com.example.book_app.adapter;

import static com.example.book_app.Constants.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_app.MyApplication;
//import com.example.book_app.activities.PdfDetailActivity;
import com.example.book_app.activities.PdfDetailActivity;
import com.example.book_app.activities.PdfEditActivity;
import com.example.book_app.databinding.RowPdfAdminBinding;
import com.example.book_app.filter.FilterPdfAdmin;
import com.example.book_app.models.PdfModel;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PdfAdminAdapter extends RecyclerView.Adapter<PdfAdminAdapter.ViewHolder> implements Filterable {

    Context context;
    RowPdfAdminBinding binding;
    private static final String TAG ="PDF_ADAPTER_TAG";
    public ArrayList<PdfModel> pdfModelArrayList, filterList;

    private FilterPdfAdmin filter;
    ProgressDialog progressDialog;

    public PdfAdminAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
        this.filterList = pdfModelArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait ...");
    }

    @NonNull
    @Override
    public PdfAdminAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfAdminAdapter.ViewHolder holder, int position) {
        PdfModel model = pdfModelArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String bookId = model.getId();
        String categoryId = model.getCategoryID();
        long timestamp = model.getTimestamp();
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        // setData
        holder.titleTv.setText(title);
        holder.desTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //we will need these many time, so insteady of writing again and again move them to My aplication class
        //load futher details like category, pdf from url, pdf size in seprate functions


        MyApplication.loadPdfFromUrl(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar);

        MyApplication.loadPdfSize(
                ""+ pdfUrl,
                ""+ title,
                holder.sizeTv);

        MyApplication.loadCategory(
                ""+ categoryId,
                holder.categoryTv);


        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, PdfDetailActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });


    }

    private void moreOptionsDialog(PdfModel model, ViewHolder holder) {
        String bookTile = model.getTitle();
        String bookId = model.getId();
        String bookUrl = model.getUrl();

        String options [] = {"Edit" , "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            // Edit
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",model.getId());
                            context.startActivity(intent);

                        } else if (which == 1){
                            MyApplication.deleteBook(context,""+ bookTile,""+bookId, ""+bookUrl);
                        }
                    }
                }).show();
    }

    @Override
    public int getItemCount() {
        return pdfModelArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv, desTv, sizeTv, categoryTv, dateTv;
        PDFView pdfView;
        ImageButton imageButton;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = binding.bookTitleTv;
            desTv = binding.bookDesTv;
            sizeTv = binding.sizeTv;
            categoryTv = binding.categoryTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            imageButton = binding.moreBtn;
            progressBar = binding.progressBar;
        }
    }
}
