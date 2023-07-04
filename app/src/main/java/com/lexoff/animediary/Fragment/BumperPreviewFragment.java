package com.lexoff.animediary.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.lexoff.animediary.BumperCallback;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BumperPreviewFragment extends Fragment {

    private Uri uri;
    private BumperCallback callback;

    private ImageView mainImageView;

    public BumperPreviewFragment() {
        //empty
    }

    public static BumperPreviewFragment newInstance(Uri uri, BumperCallback callback) {
        BumperPreviewFragment fragment = new BumperPreviewFragment();
        fragment.setUri(uri);
        fragment.setCallback(callback);
        return fragment;
    }

    private void setUri(Uri uri){
        this.uri=uri;
    }

    private void setCallback(BumperCallback callback){
        this.callback=callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bumper_preview, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        int statusbarHeight=Utils.getStatusBarHeight(requireContext());
        View pab=rootView.findViewById(R.id.pab);
        ConstraintLayout.LayoutParams params=(ConstraintLayout.LayoutParams) pab.getLayoutParams();
        params.topMargin=statusbarHeight;
        pab.setLayoutParams(params);

        ImageView backBtn = rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        mainImageView = rootView.findViewById(R.id.main_image_view);

        TextView setBumperButton = rootView.findViewById(R.id.set_bumper_btn);
        int navbarHeight = Utils.getNavBarHeight(requireContext());
        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) setBumperButton.getLayoutParams();
        params2.bottomMargin = navbarHeight + Utils.dpToPx(requireContext(), 15);
        setBumperButton.setLayoutParams(params2);

        setBumperButton.setOnClickListener(v -> {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);

                File file = Utils.getBumperFile(requireContext());
                try (OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4 * 1024];
                    int read;

                    while ((read = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }

                    output.flush();
                }

                inputStream.close();

                callback.onSet();

                requireActivity().onBackPressed();
            } catch (Exception e) {
                callback.onError(e);

                requireActivity().onBackPressed();
            }
        });

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            inputStream.close();

            mainImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            callback.onError(e);

            requireActivity().onBackPressed();
        }
    }

}
