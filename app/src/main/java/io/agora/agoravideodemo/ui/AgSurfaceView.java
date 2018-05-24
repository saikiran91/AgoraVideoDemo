package io.agora.agoravideodemo.ui;

import android.view.SurfaceView;

/**
 * Created by saiki on 24-05-2018.
 **/
public class AgSurfaceView {
    private SurfaceView surfaceView;
    private Boolean isVisible;

    public AgSurfaceView(SurfaceView surfaceView, boolean visibility) {
        this.surfaceView = surfaceView;
        this.isVisible = visibility;
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
