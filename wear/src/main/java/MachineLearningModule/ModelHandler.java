package MachineLearningModule;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.Exception;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;

public class ModelHandler {

    private final double SigmoidThreshold = 0.85;
    private final int featureCount = 24;
    private final int windowLength = 1000;
    private final int windowColumns = 6;
    private int currentProbability;
    private final String inputTensor = "dense_1_input:0";
    private final String outputTensor = "dense_3/Sigmoid:0";
    private final String[] outputNodes = { outputTensor };
    private AssetManager Assets;

    TensorFlowInferenceInterface inferenceInterface;

    public void loadModel(AssetManager assets) {
        try {
            inferenceInterface = new TensorFlowInferenceInterface(assets, "file:///android_asset/model.pb");
            Log.i("ML","model successfully loaded");
        }
        catch(Exception e) {
            Log.i("ML","failed to load model: " + e.getMessage());
        }
        Assets = assets;
//        testConvertToFeatures();
//        testClassify();
    }

    public int getCurrentProbability() {
        return currentProbability;
    }

    public boolean predict(double[][] window) {
       // in: 2D-array with 6 columns x 1000 samples
       // out: true (smoking) or false (non-smoking)
       float[] features = new float[featureCount];
       assert(window[0].length == windowLength);
       assert(window.length == windowColumns);
       convertToFeatures(window, features);
       return (classify(features) >= SigmoidThreshold);
    }

    private float classify(float[] features) {
        try {
            float[] result = new float[1];
            inferenceInterface.feed(inputTensor, features, 1, featureCount);
            inferenceInterface.run(outputNodes);
            inferenceInterface.fetch(outputTensor, result);
            currentProbability = (int)(result[0] * 100);
//            Log.i("ML", "" + (int)(result[0] * 100));
            return result[0];
        }
        catch(Exception e) {
            Log.i("ML","classify failed: " + e.getMessage());
        }
        return 0;
    }

    private void convertToFeatures(double[][] input, float[] output) {
        // in: windowColumns x windowLength (6 x 1000)
        // out: featureCount (12)
        //long start = System.nanoTime();
        for(int column = 0; column < windowColumns; column++) {
            DescriptiveStatistics ds = new DescriptiveStatistics(input[column]);
            for(int row = 0; row < windowLength; row++) {
                output[column] = (float)(ds.getMean()); // 1500: max 586 ms, avg 430 ms
                output[column + windowColumns] = (float)(ds.getStandardDeviation()); // 1500: max 1061 ms, avg 810 ms
                output[column + windowColumns * 2] = (float)(ds.getMin());  // 1500: max 522 ms, avg 355 ms
                output[column + windowColumns * 3] = (float)(ds.getMax());  // 1500: max 502 ms, avg 355 ms
            }
        }
        //Log.i("ML", "" + (System.nanoTime() - start) / 1000000 + " ms");
    }

    private void testClassify() {
        String featureFile = "testfeatures.csv";
        int lineCount = countLines(featureFile);
        Log.i("ML", lineCount + " lines");
        float[][] features = new float[lineCount][featureCount];
        float[] labels = new float[lineCount];
        readData(featureFile, features, labels);
        Log.i("ML", "features [" + labels[0] + "] [" + features[0][0] + ", " + features[0][1] + ", ...]");
        int tp = 0, tn = 0, fp = 0, fn = 0;
        for(int i = 0; i < lineCount; i++) {
            if(classify(features[i]) > 0.5)
                if(labels[i] == 0) fp++; else tp++;
            else
                if(labels[i] == 0) tn++; else fn++;
        }
        evaluateClassification(tp, tn, fp, fn);
    }

    private void evaluateClassification(int tp, int tn, int fp, int fn) {
        Log.i("ML", "tp: " + tp + "  tn: " + tn + "  fp: " + fp + "  fn: " + fn);
        float precision = (float) tp / (tp + fp);
        float recall = (float) tp / (tp + fn);
        float accuracy = ((float) tp + tn) / (tp + fn + tn + fp);
        Log.i("ML", "precision: " + precision);
        Log.i("ML", "recall   : " + recall);
        Log.i("ML", "accuracy : " + accuracy);
        Log.i("ML", "f1-score : " + 2 * ((precision * recall) / (precision + recall)));
    }

    private void testConvertToFeatures() {
        String dataFile = "testdata.csv";
        int lineCount = countLines(dataFile);
        Log.i("ML", lineCount + " lines");
        double[][] sensorData = new double[lineCount][6];
        double[] labels = new double[lineCount];
        readData(dataFile, sensorData, labels);
        Log.i("ML", "sensorData [" + labels[0] + "] [" + sensorData[0][0] + "]");

        double[][] window = new double[windowColumns][windowLength];
        createWindowData(sensorData, window);

        float[] result = new float[lineCount];
        convertToFeatures(window, result);
        Log.i("ML", "testConvertToFeatures [" + result[0] + ", " + result[1] + ", " +
              result[2] + " ... " + result[9] + ", " + result[10] + ", " + result[11] + "]");
    }

    private void createWindowData(double[][] sensorData, double[][] windowData)
    {
        // make transposed subset from input
        Log.i("ML", "createWindowData " + windowData.length + " x " + windowData[0].length);
        for(int i = 0; i < windowData[0].length; i++)
        {
            for(int n = 0; n < windowData.length; n++)
            {
                windowData[n][i] = sensorData[i][n];
            }
        }
        Log.i("ML", "createWindowData col 0 [" + windowData[0][0] + ", " + windowData[0][1] + ", ...]");
        Log.i("ML", "createWindowData col 1 [" + windowData[1][0] + ", " + windowData[1][1] + ", ...]");
    }

    private int countLines(String file) {
        try {
            LineNumberReader count = new LineNumberReader(new InputStreamReader(Assets.open(file)));
            while(count.skip(Long.MAX_VALUE) > 0);
            return count.getLineNumber();
        }
        catch(Exception e) {
            Log.i("ML", "failed to count lines of file: " + e.getMessage());
            return 0;
        }
    }

    private void readData(String file, double[][] values, double[] labels) {
        try {
            String fieldDelimiter = " ";
            BufferedReader reader = new BufferedReader(new InputStreamReader(Assets.open(file)));
            String line;
            int row = 0;
            while(row < values.length && (line = reader.readLine()) != null) {
                String[] fields = line.split(fieldDelimiter);
                labels[row] = parseDouble(fields[0]);
                for(int i = 0; i < values[0].length; i++) {
                    values[row][i] = parseDouble(fields[i + 1]);
                }
                row++;
            }
        }
        catch(Exception e) { Log.i("ML", "failed to read data: " + e.getMessage()); }
    }

    private void readData(String file, float[][] values, float[] labels) {
        try {
            String fieldDelimiter = " ";
            BufferedReader reader = new BufferedReader(new InputStreamReader(Assets.open(file)));
            String line;
            int row = 0;
            while(row < values.length && (line = reader.readLine()) != null) {
                String[] fields = line.split(fieldDelimiter);
                labels[row] = parseFloat(fields[0]);
                for(int i = 0; i < values[0].length; i++) {
                    values[row][i] = parseFloat(fields[i + 1]);
                }
                row++;
            }
        }
        catch(Exception e) { Log.i("ML", "failed to read data: " + e.getMessage()); }
    }
}
