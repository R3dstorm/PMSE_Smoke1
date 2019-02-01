package MachineLearningModule;

import android.os.AsyncTask;
import android.util.Log;

public class ProcessingTask extends AsyncTask<Void, Void, Void> {
    AsyncResponse parent;
    ModelHandler model;
    int probability;
    long requiredTime;
    double[][] window;

    public ProcessingTask(AsyncResponse parent, ModelHandler model, double[][] window) {
        this.parent = parent;
        this.window = window;
        this.model = model;
    }

    @Override
    protected Void doInBackground(Void... params) {
        long start = System.nanoTime();
        // 6x1500/24: max 2637 ms, avg 2050 ms
        // 6x1000/24: max 1569 ms, avg 900 ms
        probability = model.predict(window);
        requiredTime = (System.nanoTime() - start) / 1000000;
        //Log.i("ML", "" + (System.nanoTime() - start) / 1000000 + " ms (" + hasSmokingLabel + ")");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        parent.predictionResult(probability, requiredTime);
    }
}
