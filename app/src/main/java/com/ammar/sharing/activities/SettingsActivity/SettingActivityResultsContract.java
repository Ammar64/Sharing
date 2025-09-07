package com.ammar.sharing.activities.SettingsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SettingActivityResultsContract extends ActivityResultContract<Void, Boolean> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    public Boolean parseResult(int resultCode, @Nullable Intent result) {
        if (resultCode != Activity.RESULT_OK || result == null) {
            Log.d("MYLOG", "Settings activity result is null");
            return false;
        }
        boolean shouldRecreate = result.getBooleanExtra(SettingsActivity.EXTRA_SHOULD_MAIN_ACTIVITY_RECREATE, false);
        Log.d("MYLOG", "Settings activity result is " + shouldRecreate);
        return shouldRecreate;
    }

}
