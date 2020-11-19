package com.example.iotfactory;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxTable;

import java.util.List;

public final class InfluxDbClientHelper {

    private static final String token = "MsDxKvOAA_73votUMVlm0txdBai5mDJNTam2hr_Odh-BDvR2ZPRtWVDUBSRbejH2WrEzcUyhV1NMGfw3VSDrSA==";
    private static final String bucket = "3261957's Bucket";
    private static final String org = "3261957@myuwc.ac.za";

    private static InfluxDBClient instance = null;

    private InfluxDbClientHelper(){
        instance = InfluxDBClientFactory.create("https://eu-central-1-1.aws.cloud2.influxdata.com", token.toCharArray());
    }

    public static List<FluxTable> query(){
        String query = String.format("from(bucket: \"%s\") |> range(start: -1h)", bucket);

        return instance.getQueryApi().query(query, org);
    }
}
