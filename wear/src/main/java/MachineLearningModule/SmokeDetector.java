package MachineLearningModule;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.time.LocalDateTime;

import SensorReadoutModule.DataFileWriter;

public class SmokeDetector implements AsyncResponse {

    private static final int gestureProbability = 75; // min probability to recognize a smoking gesture
    private static final int gestureThreshold = 6; // consecutive times above sigmoid threshold to recognize smoking gesture
    private static final int smokingThreshold = 6; // consecutive gesture occurences to enter smoking phase
    private static final int smokingTimeout = 45; // max frames between two gestures to abort entering smoking phase
    private static final int stopProbability = 70; // max probability to count as stop frame
    private static final int stopThreshold = 30; // consecutive times below stop probability to leave smoking phase
    private static final int maxStopFrames = 300; // max number of frames for smoking phase since first gesture recognition

    enum State { Initialize, Idle, Start, Smoking, Stop; }

    private ModelHandler modelHandler;
    private State currentState;
    private Context myContext;
    private File currentFile = null;
    private DataFileWriter dataFile;
    private boolean reportSmoking = false;
    private LocalDateTime startTime = null;
    private LocalDateTime stopTime = null;
    private long currentTiming = 0;
    private int currentProbability = 0;
    private int smokingStartFrame = 0;
    private int gestureCounter = 0;
    private int timeoutCounter = 0;
    private int frameCounter;
    private int startFrames = 0;
    private int stopFrames = 0;

    public SmokeDetector(Context context) {
        frameCounter = 0;
        currentState = State.Initialize;
        modelHandler = new ModelHandler();
        modelHandler.loadModel(context.getAssets());
        myContext = context;
        dataFile = new DataFileWriter();
    }

    private void reset() {
        reportSmoking = false;
        smokingStartFrame = 0;
        gestureCounter = 0;
        timeoutCounter = 0;
        startFrames = 0;
        stopFrames = 0;
        if(currentFile != null) {
            currentFile = null;
        }
        currentFile = new File(myContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), dataFile.getCommonPrefix() + "_LiveData");
        dataFile.setCurrentFile(currentFile);
        //dataFile.startSession();
    }

    public int getCurrentProbability() {
        return currentProbability;
    }

    public int getCurrentStartStopFrames() {
        if(currentState == State.Smoking || currentState == State.Stop) {
            return stopFrames;
        }
        return startFrames;
    }

    public long getCurrentTiming() { return currentTiming; }

    public long getCurrentFrame() { return frameCounter; }

    public int getGestureCounter() { return gestureCounter; }

    public boolean isSmokingPhase() { return (currentState == State.Start || currentState == State.Smoking); }

    public LocalDateTime getStartTime() { return startTime; }

    public LocalDateTime getStopTime() { return stopTime; }

    public String getCurrentState() {
        switch(currentState) {
            case Initialize: return "Init";
            case Idle: return "Idle";
            case Start: return "Start";
            case Smoking: return "Smoke";
            case Stop: return "Stop";
            default: return "";
        }
    }

    public boolean isSmokingDetected() {
        if(reportSmoking) {
            reportSmoking = false;
            return true;
        }
        return false;
    }

    public void feedSensorData(final double[][] window) {
        frameCounter++;
        new ProcessingTask(this, modelHandler, window).execute();
        //dataFile.writeSession(window, window.length); // unlabeled -> TODO: store current state, too
    }

    @Override
    public void predictionResult(int probability, long timing) {
        currentProbability = probability;
        currentTiming = timing;

        if (currentState == State.Initialize) {
            reset();
            currentState = State.Idle;
        }

        if (currentState == State.Idle) {
            if (currentProbability >= gestureProbability) {
                startFrames++;
            } else {
                startFrames = 0;
            }
            if (startFrames >= gestureThreshold) {
                // smoking gesture first recognized
                startTime = LocalDateTime.now();
                startFrames = 0;
                gestureCounter++;
                smokingStartFrame = frameCounter;
                currentState = State.Start;
            }
        } else if (currentState == State.Start) {
            if (currentProbability >= gestureProbability) {
                startFrames++;
                timeoutCounter = 0;
            } else {
                startFrames = 0;
                timeoutCounter++;
            }
            if (startFrames >= gestureThreshold) {
                // smoking gesture recognized again
                startFrames = 0;
                timeoutCounter = 0;
                gestureCounter++;
                if (gestureCounter >= smokingThreshold) {
                    // ok, now it is smoking
                    currentState = State.Smoking;
                }
            } else if (timeoutCounter >= smokingTimeout) {
                // timeout for gesture repetition -> abort
                currentState = State.Initialize;
            }
        } else if (currentState == State.Smoking) {
            if (currentProbability < stopProbability) {
                stopFrames++;
            } else {
                stopFrames = 0;
            }
            if (stopFrames >= stopThreshold || frameCounter >= smokingStartFrame + maxStopFrames) {
                // smoking finished
                currentState = State.Stop;
            }
        }

        if (currentState == State.Stop) {
            stopTime = LocalDateTime.now();
            reportSmoking = true;
            currentState = State.Initialize;
            //dataFile.finishSession();
        }
    }
}
