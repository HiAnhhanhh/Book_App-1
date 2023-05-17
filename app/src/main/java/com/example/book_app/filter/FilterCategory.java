package com.example.book_app.filter;


import android.widget.Filter;

import com.example.book_app.adapter.CategoryAdapter;
import com.example.book_app.models.CategoryModel;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    // ArrayList in which we want to search

    ArrayList<CategoryModel> filterList;

    // adapter in which filter need to implement
    CategoryAdapter categoryAdapter;

    public FilterCategory(ArrayList<CategoryModel> filterList, CategoryAdapter categoryAdapter) {
        this.filterList = filterList;
        this.categoryAdapter = categoryAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();

            ArrayList<CategoryModel> filteredModels = new ArrayList<>();
            for ( int i=0; i<filterList.size(); i++){
                //
                if(filterList.get(i).getCategory().toUpperCase().contains(constraint)){
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
        categoryAdapter.categoryModelArrayList = (ArrayList<CategoryModel>)results.values;
        categoryAdapter.notifyDataSetChanged();
    }
}
