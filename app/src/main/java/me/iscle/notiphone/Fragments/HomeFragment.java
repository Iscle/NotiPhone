package me.iscle.notiphone.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.iscle.notiphone.R;
import me.iscle.notiphone.Watch;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private ScrollView scrollView;
    private TextView watchName;
    private TextView watchDescription;
    private ImageView csPreview;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = view.findViewById(R.id.home_scroll);
        watchName = view.findViewById(R.id.watch_name);
        watchDescription = view.findViewById(R.id.watch_description);
        csPreview = view.findViewById(R.id.watch_image);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void updateWatchStatus(Watch watch) {
        // TODO: implement this method
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("HOME_SCROLL_POSITION", new int[]{scrollView.getScrollX(), scrollView.getScrollY()});
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updateWatchInfo(String name, String description) {
        watchName.setText(name);
        watchDescription.setText(description);
    }

    public void updateCSPreview(String previewUrl) {
        Picasso.get().load(previewUrl).into(csPreview);
    }
}
