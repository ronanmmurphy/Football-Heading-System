package com.example.ronan.heading_app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.ronan.heading_app.R;
import com.example.ronan.heading_app.file.FileOperation;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.MAGENTA;
import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;

public class GraphActivity extends Activity{
    private LineChart HEADChart;
    private LineChart NECKChart;
    private LineChart BALLChart;
    public static final String EXTRA_FILE = "Head file";
    public static final String EXTRA_FILE_1 = "Ball file";
    private File previousLog1;
    private File previousLog2;


    private String titlesComplete = "Timestamp"+ "," +"fsrLEFT" + "," + "fsrMIDDLE" + "," + "fsrRIGHT" + "," + "xpin" + "," +
            "ypin" + "," + "zpin" + "," + "ax" + "," + "ay" + "," + "az" + "," + "bx" + "," + "by" + "," + "bz" + "," +"\n";

    private float threshold = 0.5F;
    int range = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        final Intent intent = getIntent();
        String pathhead = intent.getStringExtra(EXTRA_FILE);
        previousLog1 = new File(pathhead);
        String pathball = intent.getStringExtra(EXTRA_FILE_1);
        previousLog2 = new File(pathball);

        HEADChart = (LineChart)this.findViewById(R.id.Head);
        HEADChart.getDescription().setEnabled(false);
        HEADChart.setBackgroundColor(Color.WHITE);
        HEADChart.setTouchEnabled(true);

        NECKChart = (LineChart)this.findViewById(R.id.Neck);
        NECKChart.getDescription().setEnabled(false);
        NECKChart.setBackgroundColor(Color.WHITE);
        NECKChart.setTouchEnabled(true);

        BALLChart = (LineChart)this.findViewById(R.id.Ball);
        BALLChart.getDescription().setEnabled(false);
        BALLChart.setBackgroundColor(Color.WHITE);
        BALLChart.setTouchEnabled(true);

        ArrayList<Entry> fsrLeft = new ArrayList<>();
        ArrayList<Entry> fsrMiddle = new ArrayList<>();
        ArrayList<Entry> fsrRight = new ArrayList<>();
        ArrayList<Entry> xpin = new ArrayList<>();
        ArrayList<Entry> ypin = new ArrayList<>();
        ArrayList<Entry> zpin = new ArrayList<>();
        ArrayList<Entry> ax = new ArrayList<>();
        ArrayList<Entry> ay = new ArrayList<>();
        ArrayList<Entry> az = new ArrayList<>();
        ArrayList<Entry> bx = new ArrayList<>();
        ArrayList<Entry> by = new ArrayList<>();
        ArrayList<Entry> bz = new ArrayList<>();

        if(previousLog1.exists() && previousLog2.exists()){

            ArrayList<ArrayList<Float>> data_2 = new ArrayList<ArrayList<Float>>();
            ArrayList<ArrayList<Float>> data_1 = new ArrayList<ArrayList<Float>>();
            try {
                FileInputStream is2 = new FileInputStream(previousLog2);
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(is2));
                String line2 = reader2.readLine();

                while (line2 != null) {
                    line2 = reader2.readLine();
                    if(line2 != null) {


                        String[] values = line2.split(",");
                        ArrayList<Float> lineData = new ArrayList<>();

                        Float timestamp = Float.parseFloat(values[0]);

                        lineData.add(Float.parseFloat(values[1]));
                        lineData.add(Float.parseFloat(values[2]));
                        lineData.add(Float.parseFloat(values[3]));

                        bx.add(new Entry(timestamp, lineData.get(0)));
                        by.add(new Entry(timestamp, lineData.get(1)));
                        bz.add(new Entry(timestamp, lineData.get(2)));

                        data_2.add(lineData);
                    }

                }
            }
            catch(Exception e){
                e.printStackTrace();
            }


            try {
                FileInputStream is = new FileInputStream(previousLog1);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();


                float max = 0;
                int index = 0;
                int i = 0;

                while (line != null) {
                    line = reader.readLine();

                    if(line != null) {
                        String[] values = line.split(",");
                        ArrayList<Float> lineData = new ArrayList<>();

                        Float timestamp = Float.parseFloat(values[0]);

                        float fsrL = Float.parseFloat(values[1]);
                        float fsrM = Float.parseFloat(values[2]);
                        float fsrR = Float.parseFloat(values[3]);
                        if (fsrL > max) {
                            max = fsrL;
                            index = i;
                        }
                        if (fsrM > max) {
                            max = fsrM;
                            index = i;
                        }
                        if (fsrR > max) {
                            max = fsrR;
                            index = i;
                        }

                        i++;

                        lineData.add(timestamp);

                        lineData.add(fsrL);
                        lineData.add(fsrM);
                        lineData.add(fsrR);
                        lineData.add(Float.parseFloat(values[4]));
                        lineData.add(Float.parseFloat(values[5]));
                        lineData.add(Float.parseFloat(values[6]));
                        lineData.add(Float.parseFloat(values[7]));
                        lineData.add(Float.parseFloat(values[8]));
                        lineData.add(Float.parseFloat(values[9]));

                        fsrLeft.add(new Entry(timestamp, fsrL));
                        fsrMiddle.add(new Entry(timestamp, fsrM));
                        fsrRight.add(new Entry(timestamp, fsrR));
                        xpin.add(new Entry(timestamp, lineData.get(4)));
                        ypin.add(new Entry(timestamp, lineData.get(5)));
                        zpin.add(new Entry(timestamp, lineData.get(6)));
                        ax.add(new Entry(timestamp, lineData.get(7)));
                        ay.add(new Entry(timestamp, lineData.get(8)));
                        az.add(new Entry(timestamp, lineData.get(9)));

                        data_1.add(lineData);
                    }
                }

                if (max >= threshold) {
                    int start = index - range;
                    int end = index + range;

                    if (start < 0) start = 0;
                    if (end > i) end = i;

                    try {
                        File headDataComplete = FileOperation.FYP();
                        FileWriter writer = new FileWriter(headDataComplete);
                        writer.append(titlesComplete);

                        for (int j = start; j <= end; j++) {
                            String newLine = new String();

                            for (Float f : data_1.get(j)) {
                                newLine =newLine.concat(f.toString() + ",");
                            }

                            for (Float f : data_2.get(j)) {
                                newLine = newLine.concat(f.toString() + ",");
                            }

                            newLine = newLine.concat("\n");

                            writer.append(newLine);
                        }

                        writer.flush();
                        writer.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Add lines to graphs
                    LimitLine llStart = new LimitLine(data_1.get(start).get(0), "Start");
                    LimitLine llEnd = new LimitLine(data_1.get(end).get(0), "End");
                    HEADChart.getXAxis().addLimitLine(llStart);
                    HEADChart.getXAxis().addLimitLine(llEnd);

                    NECKChart.getXAxis().addLimitLine(llStart);
                    NECKChart.getXAxis().addLimitLine(llEnd);

                    BALLChart.getXAxis().addLimitLine(llStart);
                    BALLChart.getXAxis().addLimitLine(llEnd);
/*
                    LimitLine peak = new LimitLine(data_1.max, "Threshold");
                    peak.setLineWidth(4f);
                    peak.enableDashedLine(10f, 10f, 0f);
                    peak.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                    YAxis leftAxis = HEADChart.getAxisLeft();
                    YAxis middleAxis = NECKChart.getAxisLeft();
                    YAxis rightAxis = BALLChart.getAxisLeft();

                    middleAxis.addLimitLine(peak);
                    rightAxis.addLimitLine(peak);
                    leftAxis.addLimitLine(peak);

*/
                }
            }

            catch(Exception e){
                e.printStackTrace();
            }


            LineDataSet setFSRL = new LineDataSet(fsrLeft, "FSR LEFT");
            LineDataSet setFSRM = new LineDataSet(fsrMiddle, "FSR MIDDLE");
            LineDataSet setFSRR = new LineDataSet(fsrRight, "FSR RIGHT");
            LineDataSet setHeadX = new LineDataSet(xpin, "X");
            LineDataSet setHeadY = new LineDataSet(ypin, "Y");
            LineDataSet setHeadZ = new LineDataSet(zpin, "Z");

            setFSRL.setColor(RED);
            setFSRM.setColor(BLUE);
            setFSRR.setColor(GREEN);
            setHeadX.setColor(BLACK);
            setHeadY.setColor(MAGENTA);
            setHeadZ.setColor(YELLOW);

            setFSRL.setDrawCircles(false);
            setFSRM.setDrawCircles(false);
            setFSRR.setDrawCircles(false);
            setHeadX.setDrawCircles(false);
            setHeadY.setDrawCircles(false);
            setHeadZ.setDrawCircles(false);

            ArrayList<ILineDataSet> HeadDataSet = new ArrayList<>();
            HeadDataSet.add(setFSRL); // add the data sets
            HeadDataSet.add(setFSRM);
            HeadDataSet.add(setFSRR);
            HeadDataSet.add(setHeadX); // add the data sets
            HeadDataSet.add(setHeadY);
            HeadDataSet.add(setHeadZ);

            // create a data object with the data sets
            LineData HeadData = new LineData(HeadDataSet);

            // set data
            HEADChart.setData(HeadData);



            LineDataSet setNeckX = new LineDataSet(ax, "X");
            LineDataSet setNeckY = new LineDataSet(ay, "Y");
            LineDataSet setNeckZ = new LineDataSet(az, "Z");

            setNeckX.setColor(BLACK);
            setNeckY.setColor(MAGENTA);
            setNeckZ.setColor(YELLOW);
            setNeckX.setDrawCircles(false);
            setNeckY.setDrawCircles(false);
            setNeckZ.setDrawCircles(false);

            ArrayList<ILineDataSet> NeckDataSet = new ArrayList<>();
            NeckDataSet.add(setFSRL); // add the data sets
            NeckDataSet.add(setFSRM);
            NeckDataSet.add(setFSRR);
            NeckDataSet.add(setNeckX); // add the data sets
            NeckDataSet.add(setNeckY);
            NeckDataSet.add(setNeckZ);

            // create a data object with the data sets
            LineData NeckData = new LineData(NeckDataSet);

            // set data
            NECKChart.setData(NeckData);

            LineDataSet setBallX = new LineDataSet(bx, "X");
            LineDataSet setBallY = new LineDataSet(by, "Y");
            LineDataSet setBallZ = new LineDataSet(bz, "Z");

            setBallX.setColor(BLACK);
            setBallY.setColor(MAGENTA);
            setBallZ.setColor(YELLOW);
            setBallX.setDrawCircles(false);
            setBallY.setDrawCircles(false);
            setBallZ.setDrawCircles(false);



            ArrayList<ILineDataSet> BallDataSet = new ArrayList<>();
            BallDataSet.add(setFSRL); // add the data sets
            BallDataSet.add(setFSRM);
            BallDataSet.add(setFSRR);
            BallDataSet.add(setBallX); // add the data sets
            BallDataSet.add(setBallY);
            BallDataSet.add(setBallZ);

            // create a data object with the data sets
            LineData BallData = new LineData(BallDataSet);

            // set data
            BALLChart.setData(BallData);
        }
    }


}
