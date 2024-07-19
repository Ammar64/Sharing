package com.ammar.filescenter.activities.TutorialActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.ammar.filescenter.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class TutorialActivity extends AppCompatActivity {
    private ViewPager2 viewPager;


    private ArrayList<FragmentHolder> holders = new ArrayList<>(2);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        viewPager = findViewById(R.id.VP_Tutorial);

        // init fragment
        holders.add(
                new FragmentHolder(
                        R.drawable.icon_linux,
                        R.drawable.icon_android,
                        R.string.completed,
                        R.string.disable_browser_upload_desc
                )
        );
        holders.add(
                new FragmentHolder(
                        R.drawable.icon_check,
                        R.drawable.icon_app,
                        R.string.completed,
                        R.string.disable_browser_upload_desc
                )
        );
        holders.add(
                new FragmentHolder(
                        R.drawable.icon_link,
                        R.drawable.icon_file_red,
                        R.string.completed,
                        R.string.receiving_from_user_stopped
                )
        );


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), holders);
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.TL_Tutorial);
        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {

        }).attach();

    }


    public ArrayList<FragmentHolder> getHolders() {
        return holders;
    }

    public void endTutorial() {
        finish();
        overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
    }
}
