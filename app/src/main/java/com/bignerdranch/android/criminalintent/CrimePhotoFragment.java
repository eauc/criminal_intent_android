package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class CrimePhotoFragment extends DialogFragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_PHOTO_PATH = "photo_path";

    private ImageView mPhotoImageView;

    public static CrimePhotoFragment newInstance(String title, String photoPath) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TITLE, title);
        args.putSerializable(ARG_PHOTO_PATH, photoPath);

        CrimePhotoFragment fragment = new CrimePhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_photo, null);

        String photoPath = (String)getArguments().getSerializable(ARG_PHOTO_PATH);
        mPhotoImageView = (ImageView)v.findViewById(R.id.photo_imageview);
        Bitmap bitmap = PictureUtils.getScaledBitmap(photoPath, getActivity());
        mPhotoImageView.setImageBitmap(bitmap);

        String title = (String)getArguments().getSerializable(ARG_TITLE);
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
