package com.example.ronan.heading_app.file;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

public class FileOperation {


    private static String getCurrentTimeStamp(){
        return new SimpleDateFormat("HH-mm-ss", Locale.UK).format(new Date());
    }



    public static File createLogFile(String title)throws Exception{
        String name = title + " " +getCurrentTimeStamp();
        File directory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        File folder = new File(directory, "Head Ball Logs");
        folder.mkdir();
        if(folder.exists()){
            System.out.println("Folder Exists");
            return File.createTempFile(name, ".csv", folder );
        }
        else{
            throw new Exception("Cant create folder");
        }
    }
    public static File FYP()throws Exception{
        String name = "HeadData" ;
        File directory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        File folder = new File(directory, "FYPData");
        folder.mkdir();
        if(folder.exists()){
            System.out.println("Folder Exists");
            File[] list = folder.listFiles();
            int count = 0;
            for (File f: list){
                String filename = f.getName();
                if (filename.endsWith(".csv"))
                    count++;
            }
            File output = new File(folder.getPath() + "/" + name + (count + 1) + ".csv");
            output.createNewFile();
            return output;
        }
        else{
            throw new Exception("Cant create folder");
        }
    }
}
