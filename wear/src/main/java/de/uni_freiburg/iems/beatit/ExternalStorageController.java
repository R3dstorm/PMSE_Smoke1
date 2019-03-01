package de.uni_freiburg.iems.beatit;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.opencsv.ICSVWriter.NO_ESCAPE_CHARACTER;
import static com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER;

public class ExternalStorageController {

    private static final String LOG_TAG = "ExternalStorageController";

    ExternalStorageController(){

    }

    List<byte[]> getHashList (){
        int errorCode = 0;
        File file;
        File fileList[];
        List<byte[]> hashList = new ArrayList<byte[]>();

        /* check if readable */
        if (isExternalStorageReadable()){
            /* find files */
            //file = new File(getPublicAppStorageDir(),"blub4.csv");
            //writeTest(file);
            File fileFolder = getPublicAppStorageDir();
            fileList = fileFolder.listFiles();
            if (fileList != null) {
                int numFiles = fileList.length;
                for (int i=0; i<numFiles; i++){
                    hashList.add(calcHash(fileList[i]));
                }
            }
            else{
                /* no files available -> send empty hashList */
                //writeTest(file);
            }
        }
        else {
            /* External memory not readable */
            errorCode = 1;
        }

        return hashList;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicAppStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "BeatSmoking");
        if(!file.exists()) {
            file.mkdirs();
        }
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private void writeTest (File file){
        CSVWriter writer;
        try {
            writer = new CSVWriter(new FileWriter(file),' ',NO_QUOTE_CHARACTER, NO_ESCAPE_CHARACTER,"\n");//filePath));
            String data[] = {"test2.csv"};
                writer.writeNext(data);

            writer.close();
            //Toast.makeText(this, "Storing finished", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     public byte[] calcHash(File fileName){
        byte[] buffer= new byte[8192];
        byte[] hash = null;
        int count;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();

            hash = digest.digest();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }
}
