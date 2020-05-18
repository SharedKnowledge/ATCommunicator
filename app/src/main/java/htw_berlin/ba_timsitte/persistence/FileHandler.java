package htw_berlin.ba_timsitte.persistence;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileHandler {

    private static final String TAG = "FileHandler";
    private static final String FILE_NAME = "terminal";

    public void save(Context ctx, String text){
        FileOutputStream fos = null;
        try {
            fos = ctx.openFileOutput(FILE_NAME, ctx.MODE_PRIVATE);
            fos.write(text.getBytes());
            Log.d(TAG, "saved to: " + ctx.getFilesDir() + "/" + FILE_NAME);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "save: " + e);
        } catch (IOException e) {
            Log.e(TAG, "save: " + e);
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "save: " + e);
                }
            }
        }
    }

    public String load(Context ctx){
        FileInputStream fis = null;
        String text = "";

        try {
            fis = ctx.openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            while ((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "load: " + e);
        } catch (IOException e) {
            Log.e(TAG, "load: " + e);
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "load: " + e);
                }
            }
        }

        return text;
    }

    public void delete(Context ctx){
//        FileInputStream fis = null;
//        InputStreamReader isr = new InputStreamReader(fis);
//        BufferedReader br = new BufferedReader(isr);
//
//        String currentLine;
//
//
//
//        try {
//            fis = ctx.openFileInput(FILE_NAME);
//            while ((currentLine = br.readLine()) != null){
//
//            }
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, "delete: " + e);
//        } catch (IOException e) {
//            Log.e(TAG, "delete: " + e);
//        } finally {
//            if (fis != null){
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "delete: " + e);
//                }
//            }
//        }

        try {
            new FileOutputStream(ctx.getFilesDir() + "/" + FILE_NAME).close();
        } catch (IOException e) {
            Log.e(TAG, "delete: " + e);
        }
    }
}
