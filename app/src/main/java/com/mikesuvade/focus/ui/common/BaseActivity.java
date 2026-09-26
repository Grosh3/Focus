package com.mikesuvade.focus.ui.common;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyStatusBarAppearance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyStatusBarAppearance();
    }

    protected void applyStatusBarAppearance() {
        Window window = getWindow();
        if (window == null) return;

        boolean isNight = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());
        // Иконки статус-бара
        controller.setAppearanceLightStatusBars(!isNight);

        // Иконки navigation bar (внизу)
        controller.setAppearanceLightNavigationBars(!isNight);

    }
}