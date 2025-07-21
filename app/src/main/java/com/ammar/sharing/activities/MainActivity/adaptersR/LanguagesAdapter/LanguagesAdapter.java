package com.ammar.sharing.activities.MainActivity.adaptersR.LanguagesAdapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.activities.MainActivity.adaptersR.LanguagesAdapter.viewHolders.LanguageViewHolder;
import com.ammar.sharing.common.Consts;

import java.util.Locale;

public class LanguagesAdapter extends RecyclerView.Adapter<LanguageViewHolder> {

    private static final int TYPE_SYSTEM_DEFAULT = 0;
    private static final int TYPE_LANG_OPTION = 1;

    private final String[] originalLanguagesNames;
    private final String[] translatedLanguagesNames;
    private final Context context;

    private String languageCode = null;
    private int selectedIndex;

    public LanguagesAdapter(Context context) {
        this.context = context;
        Resources res = this.context.getResources();
        originalLanguagesNames = res.getStringArray(R.array.lang_names_original);
        translatedLanguagesNames = res.getStringArray(R.array.lang_names_translated);
        resetSelectedIndex();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return TYPE_SYSTEM_DEFAULT;
        } else {
            return TYPE_LANG_OPTION;
        }
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return switch(viewType) {
            case TYPE_SYSTEM_DEFAULT -> LanguageViewHolder.Companion.makeSystemDefaultLanguageViewHolder(parent);
            case TYPE_LANG_OPTION -> LanguageViewHolder.Companion.makeLanguageViewHolder(parent);
            default -> throw new IllegalStateException("Unexpected value: " + viewType);
        };
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        position--;
        int positionCopy = position;
        holder.itemView.setOnClickListener((v) -> {
            assert Consts.langCodes != null;
            notifyItemChanged(selectedIndex+1);
            selectedIndex = positionCopy;
            if( selectedIndex == -1 ) {
                // if languageCode is an empty String language will be set to system default
                languageCode = "";

            } else {
                languageCode = Consts.langCodes[selectedIndex];
            }
            notifyItemChanged(positionCopy+1);
        });
        holder.setChecked(position == selectedIndex);
        if(position == -1) return;

        holder.setOriginalLangText(originalLanguagesNames[position]);
        holder.setTranslatedLangText(translatedLanguagesNames[position]);
    }

    @Override
    public int getItemCount() {
        assert Consts.langCodes != null;
        assert Consts.langCodes.length == originalLanguagesNames.length;
        assert Consts.langCodes.length == translatedLanguagesNames.length;

        return Consts.langCodes.length + 1;
    }

    public String getSelectedLanguageCode() {
        return languageCode;
    }

    public void resetSelectedIndex() {
        String langCode = Locale.getDefault().getLanguage();
        assert Consts.langCodes != null;
        for(int i = 0; i < Consts.langCodes.length ; i++) {
            if( Consts.langCodes[i].equals(langCode) ) {
                selectedIndex = i;
                return;
            }
        }
        selectedIndex = -1;
    }
}
