package io.agora.agoravideodemo.ui;

import android.view.SurfaceView;

/**
 * Created by saiki on 24-05-2018.
 **/
public class AgSurfaceView {
    private SurfaceView surfaceView;
    private boolean isVisible;
    private boolean isSelected;
    private int uid;

    public AgSurfaceView(SurfaceView surfaceView, boolean visibility, boolean isSelected) {
        this.surfaceView = surfaceView;
        this.isVisible = visibility;
        this.isSelected = isSelected;
        uid = (int) surfaceView.getTag();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getUid() {
        return uid;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}
