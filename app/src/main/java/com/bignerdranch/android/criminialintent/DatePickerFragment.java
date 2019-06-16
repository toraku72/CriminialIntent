package com.bignerdranch.android.criminialintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment {
    public static final String EXTRA_DATE = "com.bignerdranch.android.crimicalintent.date";

    private static final String ARG_DATE = "date";

    private DatePicker mDatePicker;
    private Button mOkButton;

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        View v = inflater.inflate(R.layout.dialog_date, container, false);
        mDatePicker = v.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month, day, null);

        mOkButton = v.findViewById(R.id.button_date_picker_ok);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.set(Calendar.YEAR, mDatePicker.getYear());
                calendar.set(Calendar.MONTH, mDatePicker.getMonth());
                calendar.set(Calendar.DAY_OF_MONTH, mDatePicker.getDayOfMonth());
                Date date = calendar.getTime();
                sendResult(Activity.RESULT_OK, date);
            }
        });

        return v;
    }

    private void sendResult(int resultCode, Date date) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        if (getTargetFragment() == null) {
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else {
            getTargetFragment()
                    .onActivityResult(getTargetRequestCode(), resultCode, intent);
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}
