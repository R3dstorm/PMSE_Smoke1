package MachineLearningModule;

import android.content.res.AssetManager;
import android.util.Log;

public class SmokeDetector implements AsyncResponse {

    private static final int startFrameThreshold = 10; // consecutive occurrences to enter smoking phase
    private static final int stopFrameThreshold = 30; // consecutive occurrences to leave smoking phase
    private static final int stopFrameProbability = 70; // max probability to count as stop frame
    private static final int maxStopFrames = 300; // max number of frames for smoking phase since start

    private ModelHandler modelHandler = null;
    private boolean wasSmokingReported;
    private boolean isSmokingPhase;
    private long currentTiming;
    private int smokingStartFrame;
    private int frameCounter;
    private int startFrames;
    private int stopFrames;

    public SmokeDetector(AssetManager assets) {
        wasSmokingReported = false;
        isSmokingPhase = false;
        smokingStartFrame = 0;
        frameCounter = 0;
        startFrames = 0;
        stopFrames = 0;
        modelHandler = new ModelHandler();
        modelHandler.loadModel(assets);
    }

    public int getCurrentProbability() {
        return modelHandler.getCurrentProbability();
    }

    public int getCurrentStartFrames() {
        return startFrames;
    }

    public long getCurrentTiming() {
        return currentTiming;
    }

    public long getCurrentFrame() {
        return frameCounter;
    }

    public boolean isSmokingPhase() {
        return isSmokingPhase;
    }

    public boolean isSmokingDetected() {
        if(isSmokingPhase && !wasSmokingReported) {
            wasSmokingReported = true;
            return true;
        }
        return false;
    }

    public void feedSensorData(final double[][] window) {
        frameCounter++;
        new ProcessingTask(this, modelHandler, window).execute();
    }

    @Override
    public void predictionResult(boolean isSmoking, long requiredTime) {
        currentTiming = requiredTime;

        if(isSmoking) {
            stopFrames = 0;
            if(!isSmokingPhase) {
                startFrames++;
                if(startFrames >= startFrameThreshold) {
                    isSmokingPhase = true;
                    smokingStartFrame = frameCounter;
                }
            }
        } else {
            startFrames = 0;
            if(isSmokingPhase) {
                if(getCurrentProbability() < stopFrameProbability) {
                    stopFrames++;
                }
//                Log.i("ML", "<" + frameCounter + "> started: " + smokingStartFrame + " stops: " + stopFrames);
            }
        }

        if(isSmokingPhase && (stopFrames >= stopFrameThreshold || frameCounter >= smokingStartFrame + maxStopFrames)) {
            wasSmokingReported = false;
            isSmokingPhase = false;
        }

//        Log.i("ML", "<" + frameCounter + ", " + isSmoking + "> smoking phase: " + isSmokingPhase +
//                " (start: " + startFrames + ", stop: " + stopFrames + ") ");
    }
}
