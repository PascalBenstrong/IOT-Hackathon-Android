package com.example.iotfactory;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.influxdb.query.FluxTable;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String BrightnessQuery = "from(bucket: \"3261957's Bucket\")\n" +
                "  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"Brightness Sensor\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"br\")\n" +
                "  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)\n" +
                "  |> yield(name: \"mean\")";
        String TempAndAirQuery = "from(bucket: \"3261957's Bucket\")\n" +
                "  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"Brightness Sensor\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"br\")\n" +
                "  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)\n" +
                "  |> yield(name: \"mean\")";

        InfluxDbClientHelper.queryAsync(BrightnessQuery, new InfluxDbClientHelper.OnQueryResponseListener() {
            @Override
            public void OnResponse(List<FluxTable> tables) {
                Toast.makeText(getBaseContext(), String.format("tables %s", tables.size()), Toast.LENGTH_LONG).show();
            }
        });
    }
}


