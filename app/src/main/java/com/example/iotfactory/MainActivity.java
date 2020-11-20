package com.example.iotfactory;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.Gson;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

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

        loadEnvironmentData();
    }

    private void loadEnvironmentData(){

        String environment = "from(bucket: \"3261957's Bucket\")\n" +
                "  |> range(start: -10h, stop: -5h)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"Environment Sensor\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"t\" or r[\"_field\"] == \"iaq\")\n" +
                "  |> aggregateWindow(every: 4m, fn: mean, createEmpty: false)\n" +
                "  |> yield(name: \"mean\")";


        InfluxDbClientHelper.queryAsync(environment, new InfluxDbClientHelper.OnQueryResponseListener() {
            @Override
            public void OnResponse(List<FluxTable> tables) {

                List<FluxRecord> airQualityRecords = tables.get(1).getRecords();
                List<FluxRecord> temperatureRecords = tables.get(0).getRecords();
                // get the last air quality
                double airQuality = (double)airQualityRecords.get(airQualityRecords.size() - 1).getValue();

                setWaveProgress(airQuality);

                //Toast.makeText(getBaseContext(), String.format("%s %s", tables.get(1).getRecords().get(0).getField(),tables.get(1).getRecords().get(0).getValue()), Toast.LENGTH_LONG).show();
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
            waveLoadingView.setTopTitle("Unhealthy for Sensitive Groups");
            //bottom_title.setText("Unhealthy for Sensitive Groups");
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

    public final class LineData{

    }

}


