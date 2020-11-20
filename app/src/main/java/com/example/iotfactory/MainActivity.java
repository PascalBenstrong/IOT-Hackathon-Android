package com.example.iotfactory;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waveLoadingView = findViewById(R.id.wave_progress);
        lineChart = findViewById(R.id.line_graph);

        loadEnvironmentData();
    }

    private void loadEnvironmentData(){

        String environment = "from(bucket: \"3261957's Bucket\")\n" +
                "  |> range(start: -6h)\n" +
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

                waveLoadingView.setProgressValue((int)airQuality);


                Toast.makeText(getBaseContext(), String.format("%s %s", tables.get(1).getRecords().get(0).getField(),tables.get(1).getRecords().get(0).getValue()), Toast.LENGTH_LONG).show();
            }
        });

    }

    public final class LineData{


    }

}


