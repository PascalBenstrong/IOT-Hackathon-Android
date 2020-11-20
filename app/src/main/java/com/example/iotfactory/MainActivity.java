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

        InfluxDbClientHelper.queryAsync(new InfluxDbClientHelper.OnQueryResponseListener() {
            @Override
            public void OnResponse(List<FluxTable> tables) {
                Toast.makeText(getBaseContext(), String.format("tables %s", tables.size()), Toast.LENGTH_LONG).show();
            }
        });

    }
}


