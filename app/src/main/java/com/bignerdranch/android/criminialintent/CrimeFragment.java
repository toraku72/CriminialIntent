package com.bignerdranch.android.criminialintent;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    private static final int PERMISSION_REQUEST_READ_CONTACT_FOR_CHOOSING = 101;
    private static final int PERMISSION_REQUEST_READ_CONTACT_FOR_UPDATING = 102;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;

    private static final Intent mPickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // This space is intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                mCrime.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This one too
            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());
                builder.setType("text/plain");
                builder.setText(getCrimeReport());
                builder.setSubject(getString(R.string.crime_report_subject));
                builder.setChooserTitle(R.string.send_report);

                builder.startChooser();
            }
        });

        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_READ_CONTACT_FOR_CHOOSING);
                } else {
                    pickContact();
                }
            }
        });

        mCallButton = v.findViewById(R.id.crime_call_suspect);
        mCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver cr = getActivity().getContentResolver();
                Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{mCrime.getSuspect()}, null);
                c.moveToFirst();
                String phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Uri number = Uri.parse("tel:" + phone);
                Intent intent = new Intent(Intent.ACTION_DIAL, number);
                c.close();
                startActivity(intent);
            }
        });

        if (mCrime.getSuspect() ==  null) {
            mCallButton.setEnabled(false);
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(mPickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSuspect();
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null || ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, mSuspectButton.getText());
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want to query to return values for
            String[] queryFields = new String[] {
                    ContactsContract.Contacts._ID
            };

            // Perform your query - the contactUri is like a "where" clause here
            try (Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);) {
                // Double-check that you actually got the results
                if (c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();
                String suspect_id = c.getString(0);
                mCrime.setSuspect(suspect_id);
                mCallButton.setEnabled(true);
                updateSuspect();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (PERMISSION_REQUEST_READ_CONTACT_FOR_CHOOSING == requestCode) {
                pickContact();
            } else if (PERMISSION_REQUEST_READ_CONTACT_FOR_UPDATING == requestCode) {
                updateSuspect();
            }
        } else {
            mCallButton.setEnabled(false);
        }
    }

    private void pickContact() {
        startActivityForResult(mPickContact, REQUEST_CONTACT);
    }


    private void updateSuspect() {
        if (mCrime.getSuspect() == null)
            return;
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Cursor c = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                    null, ContactsContract.Contacts._ID + " = ?", new String[]{mCrime.getSuspect()}, null);
            c.moveToFirst();
            String suspect_name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            c.close();
            mSuspectButton.setText(suspect_name);
            mCallButton.setEnabled(true);
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACT_FOR_UPDATING);
        } else {
            mCallButton.setEnabled(false);
        }
    }

}
