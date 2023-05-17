package com.example.book_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_app.MyApplication;
import com.example.book_app.activities.PdfDetailActivity;
import com.example.book_app.databinding.RowPdfUserBinding;
import com.example.book_app.filter.FilterPdfUser;
import com.example.book_app.models.PdfModel;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;



public class PdfUserAdapter extends RecyclerView.Adapter<PdfUserAdapter.ViewHolder> implements Filterable {

    private RowPdfUserBinding binding;

    Context context;
    public ArrayList<PdfModel> pdfModelArrayList, filterList;
    private FilterPdfUser filter;

    public PdfUserAdapter(Context context, ArrayList<PdfModel> pdfModelArrayList) {
        this.context = context;
        this.pdfModelArrayList = pdfModelArrayList;
        this.filterList = pdfModelArrayList;
    }

    @NonNull
    @Override
    public PdfUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfUserAdapter.ViewHolder holder, int position) {
        PdfModel model = pdfModelArrayList.get(position);

        String bookId = model.getId();
        String categoryId = model.getCategoryID();
        String des = model.getDescription();
        String title = model.getTitle();
        String url = model.getUrl();
        long timestamp = model.getTimestamp();

        String date = MyApplication.formatTimestamp(timestamp);

        holder.dateTv.setText(date);
        holder.desTv.setText(des);
        holder.bookTitleTv.setText(title);

        MyApplication.loadCategory(""+categoryId,
                 holder.categoryTv);

        MyApplication.loadPdfSize(""+ url,
                ""+ title,
                holder.pdfSizeTv);

        MyApplication.loadPdfFromUrl(""+ url,
                ""+ title,
                holder.pdfView,
                holder.progressBar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (context, PdfDetailActivity.class);
                intent.putExtra("bookId",bookId );
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return pdfModelArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView bookTitleTv, desTv, pdfSizeTv, dateTv, categoryTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitleTv = binding.bookTitleTv;
            desTv = binding.bookDesTv;
            pdfSizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            categoryTv = binding.categoryTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
