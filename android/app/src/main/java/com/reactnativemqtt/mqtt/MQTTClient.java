package com.reactnativemqtt.mqtt;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class MQTTClient {

    private MqttAndroidClient mqttAndroidClient;

    private Context context;

    private String serverUri;

    private String clientId;

    public MQTTClient(Context context, String serverUri, String clientId){
        this.context = context;

        this.serverUri = serverUri;

        this.clientId = clientId;

        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
    }


}


