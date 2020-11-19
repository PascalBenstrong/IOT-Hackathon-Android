package com.example.iotfactory;

import android.os.Handler;
import android.os.Looper;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxTable;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class InfluxDbClientHelper {

    // background worker because we can not make network call on ui/main thread
    private static final Executor networkExecutor = Executors.newSingleThreadExecutor();
    // handler to push data back to main thread
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    private static final String token = "MsDxKvOAA_73votUMVlm0txdBai5mDJNTam2hr_Odh-BDvR2ZPRtWVDUBSRbejH2WrEzcUyhV1NMGfw3VSDrSA==";
    private static final String bucket = "3261957's Bucket";
    private static final String org = "3261957@myuwc.ac.za";

    // singleton instance
    private static final InfluxDbClientHelper instance = new InfluxDbClientHelper();
    // influx db client
    private InfluxDBClient client;

    private InfluxDbClientHelper(){

        // initialize influx db client with credentials
        client = InfluxDBClientFactory.create("https://eu-central-1-1.aws.cloud2.influxdata.com", token.toCharArray());
    }

    public final InfluxDBClient getClient(){
        return client;
    }

    public static void queryAsync(String query, OnQueryResponseListener listener){

        networkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                InfluxDBClient client = instance.getClient();

                // a simple query to request data from a bucket in the last 24 hours
                String Query = String.format(query, bucket);

                // make the query
                List<FluxTable> Tables = client.getQueryApi().query(Query, org);


                // send data back to ui thread
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.OnResponse(Tables);

                    }
                });
            }
        });

    }

    public static abstract class OnQueryResponseListener{

        public abstract void OnResponse(List<FluxTable> tables);
    }
}
