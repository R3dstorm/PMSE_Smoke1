package SensorReadoutModule;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.opencsv.ICSVWriter.NO_ESCAPE_CHARACTER;
import static com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER;

public class DataFileWriter {

    private File DataFile;
    private CSVWriter CurrentCSV;

    public void setCurrentFile(File file) {
        DataFile = file;
    }

    public String getCommonPrefix() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_");
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }

    public boolean startSession() {
        if(CurrentCSV != null) {
            CurrentCSV = null;
        }
        try {
            CurrentCSV = new CSVWriter(new java.io.FileWriter(DataFile), ' ', NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER, "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean finishSession() {
        if(CurrentCSV != null) {
            try {
                CurrentCSV.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean writeSession(float[][] dataStorage, int numberOfSamples) {
        if(CurrentCSV != null) {
            for (int i = 0; i < numberOfSamples; i++) {
                String[] data = {
                        String.format("%.0f",dataStorage[i][0]),
                        String.format("%.3f",dataStorage[i][1]),
                        String.format("%.6f",dataStorage[i][2]),
                        String.format("%.6f",dataStorage[i][3]),
                        String.format("%.6f",dataStorage[i][4]),
                        String.format("%.6f",dataStorage[i][5]),
                        String.format("%.6f",dataStorage[i][6])};
                CurrentCSV.writeNext(data);
            }
            return true;
        }
        return false;
    }

    public boolean storeDataToFile(float[][] dataStorage, int numberOfSamples) {
        /* Checks if external storage is available for read and write */
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return startSession() &&
                   writeSession(dataStorage, numberOfSamples) &&
                   finishSession();
        }
        return false;
    }
}
