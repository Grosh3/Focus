package com.mikesuvade.focus.ui.detail.helper;

import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class KeyboardInsetsHelper {

    private KeyboardInsetsHelper() {}

    public static void setup(ScrollView scrollView, Window window) {
        if (scrollView == null || window == null) return;

        scrollView.setClipToPadding(false);

        ViewCompat.setOnApplyWindowInsetsListener(window.getDecorView(), (v, insets) -> {
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            float density = v.getResources().getDisplayMetrics().density;

            if (isKeyboardVisible && keyboardHeight > 0) {
                int paddingBottom = keyboardHeight + (int) (24 * density);
                scrollView.setPadding(
                        scrollView.getPaddingLeft(),
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        paddingBottom
                );

                View currentFocus = window.getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    final View focusedView = currentFocus;
                    scrollView.postDelayed(() -> {
                        if (focusedView.isFocused()) {
                            int scrollToY = focusedView.getBottom() + (int) (32 * density);
                            scrollView.smoothScrollTo(0, scrollToY);
                        }
                    }, 150);
                }
            } else {
                scrollView.setPadding(
                        scrollView.getPaddingLeft(),
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        navBarHeight + (int) (16 * density)
                );
            }

            return insets;
        });
    }
}