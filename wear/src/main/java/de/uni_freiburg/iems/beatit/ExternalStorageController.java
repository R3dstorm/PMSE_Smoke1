/* Credits: https://stackoverflow.com/questions/29790578/android-copy-file-from-internal-storage-to-external */

package de.uni_freiburg.iems.beatit;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

    public List<String> getHashList (){
        int errorCode = 0;
        File fileList[];
        List<String> hashList = new ArrayList<String>();

        /* check if readable */
        if (isExternalStorageReadable()){
            /* find files */
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
            }
        }
        else {
            /* External memory not readable */
            errorCode = 1;
        }

        return hashList;
    }

    public void writeFileToStorage (File file, String title){
        if (isExternalStorageWritable()){
            String target = getPublicAppStorageDir().getAbsolutePath() + "/" +title;
            copyFile(file.getAbsolutePath(), target);
        }
    }

    public static boolean copyFile(String from, String to) {
        try {
            int bytesum = 0;
            int byteRead = 0;
            File oldFile = new File(from);
            if (oldFile.exists()) {
                InputStream inStream = new FileInputStream(from);
                FileOutputStream fs = new FileOutputStream(to);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    bytesum += byteRead;
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
                fs.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String calcHash(File fileName){
        byte[] buffer= new byte[8192];
        String hash = null;
        int count;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();

            hash = Base64.encodeToString(digest.digest(), Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }
}

