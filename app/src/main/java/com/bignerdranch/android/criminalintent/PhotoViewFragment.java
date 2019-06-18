package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class PhotoViewFragment extends DialogFragment {
    private static final String ARG_PHOTO_PATH = "photo_path";

    private ImageView mPhotoView;

    public static PhotoViewFragment newInstance(String photoPath) {
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_PATH, photoPath);

        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String photoPath = getArguments().getString(ARG_PHOTO_PATH);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);
        mPhotoView = view.findViewById(R.id.detail_photo_view);
        Bitmap bitmap = PictureUtils.getScaledBitmap(photoPath, getActivity());
        mPhotoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity(), R.style.ThemeOverlay_AppCompat_Dialog)
                .setView(view)
                .create();
    }
}
