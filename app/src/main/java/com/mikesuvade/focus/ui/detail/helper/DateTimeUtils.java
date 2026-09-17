package com.mikesuvade.focus.ui.detail.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtils {

    private DateTimeUtils() {}

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}