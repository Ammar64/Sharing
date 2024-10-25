package com.ammar.sharing.activities.MainActivity.adaptersR.LanguagesAdapter.viewHolders

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.ammar.sharing.R
import com.ammar.sharing.common.utils.Utils
import com.ammar.sharing.custom.ui.AdaptiveTextView

class LanguageViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val langOptionRB: RadioButton
    private val originalLangNameTV: AdaptiveTextView
    private val translatedLanguageNameTV: AdaptiveTextView
    init {
        langOptionRB = itemView.findViewById(R.id.RB_LanguageOption)
        originalLangNameTV = itemView.findViewById(R.id.TV_LanguageOptionOriginal)
        translatedLanguageNameTV = itemView.findViewById(R.id.TV_LanguageOptionNotTranslated)
    }

    fun setOriginalLangText(text: String) {
        originalLangNameTV.text = text
    }

    fun setTranslatedLangText(text: String) {
        translatedLanguageNameTV.text = text
    }

    fun setChecked(selected: Boolean) {
        langOptionRB.isChecked = selected
    }

    companion object {
        fun makeLanguageViewHolder(parent: ViewGroup): LanguageViewHolder {
            val languageOptionView = LayoutInflater.from(parent.context).inflate(R.layout.row_language, parent, false)
            languageOptionView.updateLayoutParams<RecyclerView.LayoutParams> {
                topMargin = Utils.dpToPx(12f).toInt()
            }
            return LanguageViewHolder(languageOptionView)
        }

        fun makeSystemDefaultLanguageViewHolder(parent: ViewGroup): LanguageViewHolder {
            val languageOptionView = LayoutInflater.from(parent.context).inflate(R.layout.row_language, parent, false)

            // hide bottom text
            val languageOptionTranslated = languageOptionView.findViewById<AdaptiveTextView>(R.id.TV_LanguageOptionNotTranslated)
            languageOptionTranslated.visibility = View.GONE

            val languageOptionOriginal = languageOptionView.findViewById<AdaptiveTextView>(R.id.TV_LanguageOptionOriginal)
            languageOptionOriginal.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomToBottom = R.id.RB_LanguageOption
            }
            languageOptionOriginal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            languageOptionOriginal.setText( R.string.system_default_lang )
            return LanguageViewHolder(languageOptionView)
        }
    }
}