package MachineLearningModule;

import android.content.res.AssetManager;
import android.util.Log;

public class SmokeDetector implements AsyncResponse {

    private static final int startFrameThreshold = 10;
    private static final int stopFrameThreshold = 8;

    private ModelHandler modelHandler;
    private boolean isSmokingPhase;
    private int frameCounter;
    private int startFrames;
    private int stopFrames;

    public SmokeDetector(AssetManager assets) {
        isSmokingPhase = false;
        frameCounter = 0;
        startFrames = 0;
        stopFrames = 0;
        modelHandler = new ModelHandler();
        modelHandler.loadModel(assets);
    }

    public void feedSensorData(final double[][] window) {
        frameCounter++;
        new ProcessingTask(this, modelHandler, window).execute();
    }

    @Override
    public void predictionResult(boolean isSmoking) {
        if(isSmoking) {
            stopFrames = 0;
            if(!isSmokingPhase) {
                startFrames++;
                if(startFrames >= startFrameThreshold) {
                    isSmokingPhase = true;
                }
            }
        } else {
            startFrames = 0;
            if(isSmokingPhase) {
                stopFrames++;
                if(stopFrames >= stopFrameThreshold) {
                    isSmokingPhase = false;
                }
            }
        }

        Log.i("ML","<" + frameCounter + ", " + isSmoking + "> smoking phase: " + isSmokingPhase +
                " (start: " + startFrames + ", stop: " + stopFrames + ") ");
    }
}
