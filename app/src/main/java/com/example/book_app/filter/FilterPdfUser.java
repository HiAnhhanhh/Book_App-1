package com.example.book_app.filter;

import android.widget.Filter;

import com.example.book_app.adapter.PdfUserAdapter;
import com.example.book_app.models.PdfModel;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {

    ArrayList<PdfModel> filterList;
    PdfUserAdapter pdfUserAdapter;

    public FilterPdfUser(ArrayList<PdfModel> filterList, PdfUserAdapter pdfUserAdapter) {
        this.filterList = filterList;
        this.pdfUserAdapter = pdfUserAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        if(constraint!=null || constraint.length() >0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<PdfModel> filteredModels = new ArrayList<>();
            for(int i=0; i < filterList.size(); i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            filterResults.count = filteredModels.size();
            filterResults.values = filteredModels;
        }else{
            filterResults.count= filterList.size();
            filterResults.values= filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        pdfUserAdapter.pdfModelArrayList = (ArrayList<PdfModel>)results.values;
        pdfUserAdapter.notifyDataSetChanged();
    }
}
