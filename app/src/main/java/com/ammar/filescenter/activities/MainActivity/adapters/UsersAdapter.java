package com.ammar.filescenter.activities.MainActivity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ammar.filescenter.R;
import com.ammar.filescenter.services.models.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        holder.setup( User.users.get(position) );
    }

    @Override
    public int getItemCount() {
        return User.users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView userIconIV;
        private final TextView usernameTV;
        private final TextView userAddressTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userIconIV = itemView.findViewById(R.id.IV_UserIcon);
            usernameTV = itemView.findViewById(R.id.TV_Username);
            userAddressTV = itemView.findViewById(R.id.TV_UserIpAddress);
        }

        public void setup( User user ) {
            // Temporary for now
            userIconIV.setImageResource(R.drawable.icon_apps);
            usernameTV.setText("User-" + user.getId());

            userAddressTV.setText(user.getIp());
        }
    }
}
