package com.example.iotfactory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.google.gson.Gson;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.itangqi.waveloadingview.WaveLoadingView;

public class MainActivity extends AppCompatActivity {

    private WaveLoadingView waveLoadingView;
    private LineChart lineChart;
    private TextView bottom_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waveLoadingView = findViewById(R.id.wave_progress);
        lineChart = findViewById(R.id.line_graph);
        bottom_title = findViewById(R.id.bottom_text_view);
        bottom_title.setVisibility(View.GONE);


        // background color
        lineChart.setBackgroundColor(Color.WHITE);

        // disable description text
        lineChart.getDescription().setEnabled(false);

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // set listeners
        //lineChart.setOnChartValueSelectedListener(this);
        lineChart.setDrawGridBackground(false);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        // lineChart.setScaleXEnabled(true);
        // lineChart.setScaleYEnabled(true);

        // force pinch zoom along both axis
        lineChart.setPinchZoom(true);

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(200f);
            yAxis.setAxisMinimum(-10f);
        }

       

        loadEnvironmentData();
    }

    private void loadEnvironmentData(){

        String environment = "from(bucket: \"3261957's Bucket\")\n" +
                "  |> range(start: -24h)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"Environment Sensor\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"t\" or r[\"_field\"] == \"iaq\")\n" +
                "  |> aggregateWindow(every: 30m, fn: mean, createEmpty: false)\n" +
                "  |> map(fn: (r) => ({ r with _time: uint(v: r._time) }))" +
                "  |> yield(name: \"mean\")";


        InfluxDbClientHelper.queryAsync(environment, new InfluxDbClientHelper.OnQueryResponseListener() {
            @Override
            public void OnResponse(List<FluxTable> tables) {

                List<FluxRecord> airQualityRecords = tables.get(1).getRecords();
                List<FluxRecord> temperatureRecords = tables.get(tables.size()-1).getRecords();

                // get the last air quality
                double airQuality = (double)airQualityRecords.get(airQualityRecords.size() - 1).getValue();

                setWaveProgress(airQuality);
                setData(temperatureRecords);
                //setLineChartData(temperatureRecords);

            }
        });

    }

    private void setWaveProgress(double progress){
        waveLoadingView.setProgressValue((int)(progress*100/500));
        bottom_title.setVisibility(View.GONE);

        progress = (int)progress;
        if(progress < 51){
            waveLoadingView.setBorderColor(Color.parseColor("#35D15F"));
            waveLoadingView.setWaveColor(Color.parseColor("#35D15F"));
            waveLoadingView.setTopTitle("Good");
            //bottom_title.setText("Good");
            waveLoadingView.setCenterTitle(""+(int)progress);

        }else if(progress < 101){
            waveLoadingView.setBorderColor(Color.parseColor("#3583D1"));
            waveLoadingView.setWaveColor(Color.parseColor("#3583D1"));
            waveLoadingView.setTopTitle("Moderate");
            //bottom_title.setText("Moderate");
            waveLoadingView.setCenterTitle(""+(int)progress);

        }else if(progress < 151){
            waveLoadingView.setBorderColor(Color.parseColor("#7700FF"));
            waveLoadingView.setWaveColor(Color.parseColor("#7700FF"));
            waveLoadingView.setTopTitle("");
            bottom_title.setText("Unhealthy for Sensitive Groups");
            bottom_title.setVisibility(View.VISIBLE);
            waveLoadingView.setCenterTitle(""+(int)progress);

        }else if(progress < 201){
            waveLoadingView.setBorderColor(Color.parseColor("#E8C61A"));
            waveLoadingView.setWaveColor(Color.parseColor("#E8C61A"));
            waveLoadingView.setTopTitle("Unhealthy");
            //bottom_title.setText("Unhealthy");
            waveLoadingView.setCenterTitle(""+(int)progress);

        }else if(progress < 301){
            waveLoadingView.setBorderColor(Color.parseColor("#F59300"));
            waveLoadingView.setWaveColor(Color.parseColor("#F59300"));
            waveLoadingView.setTopTitle("Very Unhealthy");
            //bottom_title.setText("Very Unhealthy");
            waveLoadingView.setCenterTitle(""+(int)progress);

        }else{
            waveLoadingView.setBorderColor(Color.parseColor("#D13535"));
            waveLoadingView.setWaveColor(Color.parseColor("#D13535"));
            waveLoadingView.setTopTitle("Hazardous");
            //bottom_title.setText("Hazardous");
            waveLoadingView.setCenterTitle(""+(int)progress);
        }
    }

    private void setLineChartData(List<FluxRecord> temperatureRecords){

        ArrayList<Entry> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for(int i = 0; i < temperatureRecords.size(); i++){

            FluxRecord record = temperatureRecords.get(i);
            long unixNanoSeconds = Long.parseLong(record.getValueByKey("_time").toString());
            Date date = new Date(unixNanoSeconds/1000000);

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
            Entry data = new Entry(Float.parseFloat(record.getValue().toString()),i);
            values.add(data);
            labels.add(formatter.format(date));
        }


        // the labels that should be drawn on the XAxis
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return labels.get((int) value);
            }
        };
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        // draw points over time
        lineChart.animateX(1500);
        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
    }


    private void setData(List<FluxRecord> temperatureRecords) {

        ArrayList<Entry> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        float max = 0;
        float min = 0;

        for (int i = 0; i < temperatureRecords.size(); i++){
            FluxRecord record = temperatureRecords.get(i);
            long unixNanoSeconds = Long.parseLong(record.getValueByKey("_time").toString());
            Date date = new Date(unixNanoSeconds/1000000);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-HH:mm");
            labels.add(formatter.format(date));


            // values
            float value = Float.parseFloat(record.getValue().toString()) ;

            if(value > max){
                max = value;
            }else if(value < min){
                min = value;
            }
            Entry entry = new Entry(i, value);

            //System.out.println("value: "+value);

            values.add(entry);
        }

        // set max and min values

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMaximum((int)(max + max * 0.5) );
        yAxis.setAxisMinimum((int)(min - min * 0.25) );

        // the labels that should be drawn on the XAxis
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return labels.get((int) value);
            }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        LineDataSet set1;

        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "Temperature");

            set1.setDrawIcons(false);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(Color.parseColor("#E07979"));
            set1.setCircleColor(Color.parseColor("#E07979"));

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return lineChart.getAxisLeft().getAxisMinimum();
                }
            });

            set1.setFillColor(Color.parseColor("#A8E07979"));

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);
        }
    }

}


