package com.example.book_app.filter;


import android.widget.Filter;

import com.example.book_app.activities.PdfAdminListActivity;
import com.example.book_app.adapter.CategoryAdapter;
import com.example.book_app.adapter.PdfAdminAdapter;
import com.example.book_app.models.CategoryModel;
import com.example.book_app.models.PdfModel;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    // ArrayList in which we want to search

    ArrayList<PdfModel> filterList;

    // adapter in which filter need to implement
    PdfAdminAdapter pdfAdminAdapter;

    public FilterPdfAdmin(ArrayList<PdfModel> filterList, PdfAdminAdapter pdfAdminAdapter) {
        this.filterList = filterList;
        this.pdfAdminAdapter = pdfAdminAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();

            ArrayList<PdfModel> filteredModels = new ArrayList<>();
            for ( int i=0; i<filterList.size(); i++){
                //
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    // add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }else{
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        pdfAdminAdapter.pdfModelArrayList = (ArrayList<PdfModel>)results.values;
        pdfAdminAdapter.notifyDataSetChanged();
    }
}
