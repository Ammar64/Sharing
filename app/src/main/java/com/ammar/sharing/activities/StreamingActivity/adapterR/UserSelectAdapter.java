package com.ammar.sharing.activities.StreamingActivity.adapterR;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.sharing.R;
import com.ammar.sharing.common.enums.OS;
import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.custom.ui.SelectionRecyclerViewAdapter;
import com.ammar.sharing.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class UserSelectAdapter extends SelectionRecyclerViewAdapter<UserSelectAdapter.ViewHolder> {
    @NonNull
    @Override
    public UserSelectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_userselect, parent, false);
        return new ViewHolder(this, view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSelectAdapter.ViewHolder holder, int position) {
        holder.populate(position);
    }

    @Override
    public int getItemCount() {
        return User.users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final UserSelectAdapter mAdapter;
        private final ImageView mUserIcon;
        private final TextView mUserName;
        private final TextView mUserIP;
        private final RadioButton mUserRadioButton;

        public ViewHolder(@NonNull UserSelectAdapter adapter, @NonNull View itemView) {
            super(itemView);
            mAdapter = adapter;
            mUserIcon = itemView.findViewById(R.id.IV_UserIcon);
            mUserName = itemView.findViewById(R.id.TV_Username);
            mUserIP = itemView.findViewById(R.id.TV_UserIpAddress);
            mUserRadioButton = itemView.findViewById(R.id.RB_UserSelect);
        }

        public void populate(int position) {
            User user = User.users.get(position);
            int iconSize = (int) Utils.dpToPx(40);

            Glide.with(itemView)
                    .load(OS.getOSResourceDrawable(user.getOS()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(iconSize, iconSize)
                    .into(mUserIcon);

            mUserName.setText(user.getName());
            mUserIP.setText(user.getIp());
            mUserRadioButton.setChecked(mAdapter.isSelected(position));

            itemView.setOnClickListener((v) -> {
                mAdapter.setSelectedIndex(position);
            });
        }
    }
}
