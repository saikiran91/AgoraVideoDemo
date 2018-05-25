package io.agora.agoravideodemo.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import io.agora.agoravideodemo.R;
import io.agora.agoravideodemo.ui.AgSurfaceView;
import io.agora.agoravideodemo.utils.DoubleClickListener;

public class VideoViewAdapter extends RecyclerView.Adapter<VideoViewAdapter.ViewHolder> {
    private static final String TAG = "VideoViewAdapter";
    private VideoSelectedListener videoSelectedListener;
    private ArrayList<AgSurfaceView> mVideoList = new ArrayList<>();

    public VideoViewAdapter(VideoSelectedListener videoSelectedListener) {
        this.videoSelectedListener = videoSelectedListener;
    }

    @NonNull
    @Override
    public VideoViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video_container, parent, false);

        view.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                super.onDoubleClick(v);
                videoSelectedListener.onVideoSelected(view);
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AgSurfaceView agView = mVideoList.get(position);

        if (agView.isSelected()) {
            holder.frameLayout.setBackgroundColor(Color.LTGRAY);
        } else {

            FrameLayout surfaceParent = (FrameLayout) agView.getSurfaceView().getParent();
            if (surfaceParent != null) surfaceParent.removeAllViews();

            //TODO do this Visibility thing in large_view too
            agView.getSurfaceView().setVisibility(agView.getVisible() ? View.VISIBLE : View.GONE);
            holder.frameLayout.setBackgroundColor(Color.TRANSPARENT);
            holder.frameLayout.addView(agView.getSurfaceView());
        }

    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }


    public boolean addView(SurfaceView view, int uid) {
        Log.d(TAG, "addView udi =" + uid + " and view =" + view);
        removeView(uid);
        view.setZOrderMediaOverlay(true);
        boolean result = mVideoList.add(new AgSurfaceView(view, true, false));
        if (result) notifyItemInserted(mVideoList.size());
        return result;

    }

    public boolean removeView(int uid) {
        Log.d(TAG, "removeView uid=" + uid);
        for (int i = 0; i < mVideoList.size(); i++) {
            if ((int) (mVideoList.get(i).getSurfaceView().getTag()) == uid) {
                mVideoList.remove(i);
                notifyItemRemoved(i);
                return true;
            }
        }
        return false;
    }

    public void onUserVideoMuted(int uid, boolean isMuted) {
        Log.d(TAG, "onUserVideoMuted uid=" + uid + " and isMuted=" + isMuted);
        for (int i = 0; i < mVideoList.size(); i++) {
            if ((int) mVideoList.get(i).getSurfaceView().getTag() == uid) {
                mVideoList.get(i).setVisible(!isMuted);
                notifyItemChanged(i);
            }
        }
    }

    public AgSurfaceView getItem(int position) {
        return mVideoList.get(position);
    }

    public void putViewBack(View childViewOfLargeContainer) {
        for (int i = 0; i < mVideoList.size(); i++) {
            if (mVideoList.get(i).getUid() == (int) childViewOfLargeContainer.getTag()) {
                mVideoList.get(i).setSelected(false);
                notifyItemChanged(i);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameLayout;

        ViewHolder(View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.video_container);
        }
    }

    public interface VideoSelectedListener {
        void onVideoSelected(View view);
    }
}