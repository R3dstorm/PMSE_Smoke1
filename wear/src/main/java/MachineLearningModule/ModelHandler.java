package MachineLearningModule;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.lang.Exception;

public class ModelHandler {

    public void loadModel(AssetManager assets) {
        try {
            TensorFlowInferenceInterface inferenceInterface =
                    new TensorFlowInferenceInterface(assets, "file:///android_asset/model.pb");
            Log.i("ML","model successfully loaded");
        }
        catch(Exception e) {
            Log.i("ML","failed to load model: " + e.getMessage());
        }
    }
}
