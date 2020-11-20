package com.example.iotfactory;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
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
                "  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"Environment Sensor\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"t\" or r[\"_field\"] == \"iaq\")\n" +
                "  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)\n" +
                "  |> yield(name: \"mean\")";


        InfluxDbClientHelper.queryAsync(environment, new InfluxDbClientHelper.OnQueryResponseListener() {
            @Override
            public void OnResponse(List<FluxTable> tables) {
                Toast.makeText(getBaseContext(), String.format("tables %s", tables.size()), Toast.LENGTH_LONG).show();
            }
        });

    }

}


