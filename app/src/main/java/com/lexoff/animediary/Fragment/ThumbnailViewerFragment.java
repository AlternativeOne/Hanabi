package com.lexoff.animediary.Fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.lexoff.animediary.Api;
import com.lexoff.animediary.Util.ImageLoaderWrapper;
import com.lexoff.animediary.Info.AnimeAdditionalInfo;
import com.lexoff.animediary.R;
import com.lexoff.animediary.Util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ThumbnailViewerFragment extends BaseFragment {

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private long malid;

    private ImageView downloadBtn;

    private SubsamplingScaleImageView mainImageView;
    private ProgressBar progressBar;
    private TextView noImageView;

    private AnimeAdditionalInfo currentInfo;
    private Bitmap currentImage;

    public ThumbnailViewerFragment() {
        //empty
    }

    public static ThumbnailViewerFragment newInstance(long malid) {
        ThumbnailViewerFragment fragment = new ThumbnailViewerFragment();
        fragment.setMalid(malid);
        return fragment;
    }

    private void setMalid(long malid){
        this.malid=malid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (currentWorker!=null){
            currentWorker.dispose();
            currentWorker=null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thumbnailviewer, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        //set margins of statusbar
        //post because if not then padding will not be set to rootview of fragments opened from AnimeFragment
        rootView.post(()->{
            int statusbarHeight = Utils.getStatusBarHeight(requireContext());
            rootView.setPadding(0, statusbarHeight, 0, 0);
        });

        ImageView backBtn=rootView.findViewById(R.id.ab_back_btn);
        backBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                requireActivity().onBackPressed();
            });
        });

        downloadBtn=rootView.findViewById(R.id.download_btn);
        downloadBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                try {
                    if (currentInfo==null || currentImage==null) return;

                    String[] parts=currentInfo.getThumbnailHiRes().split("/");
                    String fullName=parts[parts.length-1];
                    String name=fullName.substring(0, fullName.lastIndexOf(".")),
                            ext=fullName.substring(fullName.lastIndexOf(".")+1);

                    //assuming that content resolver used to write file
                    //I don't know a way to check if file exists
                    //also I am not sure about default behaviour in case if file with such name exists
                    //so use 2 different random values to make sure filename will be unique
                    name = String.format("%s_%d_%d.%s", name, System.currentTimeMillis(), new Random().nextInt(), ext);

                    Utils.saveImageToDownloads(requireContext(), currentImage, name);

                    //TODO: implement in-style toast messages
                    Toast.makeText(requireContext(), String.format(getString(R.string.file_saved_to_toast_message), name), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    String msg = Utils.getStringOrEmpty(e.getMessage());

                    //TODO: implement in-style toast messages
                    Toast.makeText(requireContext(), String.format(getString(R.string.error_happened_with_message), msg), Toast.LENGTH_SHORT).show();
                }
            });
        });

        mainImageView=rootView.findViewById(R.id.main_image_viewer);
        mainImageView.setMaxScale(7.5f); //5 is too low, 10 is too much; I suppose

        progressBar=rootView.findViewById(R.id.progress_bar);
        noImageView=rootView.findViewById(R.id.no_image_textview);

        loadInfo();
    }

    private void loadInfo(){
        if (isLoading.get()) return;

        isLoading.set(true);

        showOrHideProgressBar(true);

        if (currentWorker != null) currentWorker.dispose();

        currentWorker = Single.fromCallable(() -> (new Api()).getAnimeAdditionalInfo(malid))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final AnimeAdditionalInfo result) -> {
                    isLoading.set(false);

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult(AnimeAdditionalInfo info){
        currentInfo=info;

        if (info.getThumbnailHiRes().isEmpty()){
            mainImageView.setVisibility(View.GONE);
            noImageView.setVisibility(View.VISIBLE);
        } else {
            ImageLoaderWrapper.loadImage(info.getThumbnailHiRes(), new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    currentImage=bitmap;

                    mainImageView.setImage(ImageSource.bitmap(bitmap));
                    mainImageView.invalidate();

                    downloadBtn.setClickable(true);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            }, () -> {
                showOrHideProgressBar(false);
                showOrHideImageView(false);
            });
        }

        showOrHideProgressBar(false);
        showOrHideImageView(true);
    }

    private void handleError(Throwable e){
        showOrHideProgressBar(false);

        Toast.makeText(requireContext(), String.format(getString(R.string.error_happened_with_message), e.getMessage()), Toast.LENGTH_SHORT).show();
    }

    private void showOrHideProgressBar(boolean show){
        progressBar.setPadding(progressBar.getPaddingLeft(), progressBar.getPaddingTop(), progressBar.getPaddingRight(), Utils.getNavBarHeight(requireContext())+150);

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showOrHideImageView(boolean show){
        mainImageView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}
