package com.ammar.filescenter.activities.MainActivity.adaptersR;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
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
        private final Button userBlockToggleB;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userIconIV = itemView.findViewById(R.id.IV_UserIcon);
            usernameTV = itemView.findViewById(R.id.TV_Username);
            userAddressTV = itemView.findViewById(R.id.TV_UserIpAddress);
            userBlockToggleB = itemView.findViewById(R.id.B_ToggleUserBlock);
        }

        public void setup( User user ) {
            // Temporary for now
            int os_icon_res;
            switch (user.getOS()) {
                case WINDOWS:
                    os_icon_res = R.drawable.icon_windows_10;
                    break;
                case ANDROID:
                    os_icon_res = R.drawable.icon_android;
                    break;
                case LINUX:
                    os_icon_res = R.drawable.icon_linux;
                    break;
                default:
                    os_icon_res = R.drawable.icon_question_mark;
                    break;
            }

            userIconIV.setImageResource(os_icon_res);
            usernameTV.setText(itemView.getContext().getString(R.string.user_default_name, user.getId()));
            userAddressTV.setText(user.getIp());

            setBlockButton(user.isBlocked());
            userBlockToggleB.setOnClickListener( button -> {
                user.block(!user.isBlocked());
                setBlockButton(user.isBlocked());
            });
        }

        private void setBlockButton( boolean b ) {
            if(b) {
                ViewCompat.setBackgroundTintList(userBlockToggleB, ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.green)));
                userBlockToggleB.setText(R.string.unblock);
            } else {
                ViewCompat.setBackgroundTintList(userBlockToggleB, ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.red)));
                userBlockToggleB.setText(R.string.block);
            }
        }
    }
}
