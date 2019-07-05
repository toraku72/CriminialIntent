package com.bignerdranch.android.criminalintent;

import java.text.DateFormat;
import java.util.Date;

public class Utils {
    public static String getLocaleFormattedDate(Date date) {
        return DateFormat.getDateInstance(DateFormat.FULL).format(date);
    }
}
