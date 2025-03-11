package com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.viewHolders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.AddAppsAndFilesActivity.adaptersR.SearchableRecyclerAdapter;

public class SearchBarViewHolder extends RecyclerView.ViewHolder {
    public SearchBarViewHolder(@NonNull View itemView, SearchableRecyclerAdapter adapter) {
        super(itemView);
        AppCompatEditText searchInput = itemView.findViewById(R.id.ET_SearchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.searchItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

}
