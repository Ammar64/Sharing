package com.ammar.sharing.custom.ui;

import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.custom.lambda.MyConsumer;


public abstract class SelectionRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    private int mSelectedIndex = -1;
    private MyConsumer<Integer> mOnSelectedListener;
    public void setSelectedIndex(int index) {
        if(index == mSelectedIndex) return;
        int oldIndex = mSelectedIndex;
        mSelectedIndex = index;
        notifyItemChanged(oldIndex);
        notifyItemChanged(mSelectedIndex);

        if(mOnSelectedListener != null) {
            mOnSelectedListener.accept(index);
        }
    }

    public boolean isSelected(int position) {
        return position == mSelectedIndex;
    }

    public void setOnSelectedListener(MyConsumer<Integer> l) {
        mOnSelectedListener = l;
    }
}