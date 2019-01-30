package com.reactnativemqtt.mqtt;

import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.Nullable;

public class MQTTNativeModule extends ReactContextBaseJavaModule {

    private final static String EVENT_RECONNECTED = "reconnected";
    private final static String EVENT_DISCONNECTED = "disconnected";
    private final static String EVENT_MESSAGE_RECEIVED = "message_received";
    private String host;
    private int port;
    private String protocol;
    private boolean tls;
    private boolean auth;
    private String user;
    private String pass;
    private String clientId;
    private int qos;
    private boolean clean;
    private boolean automaticReconnect;
    private MqttAndroidClient mqttAndroidClient;


    public MQTTNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }


    @ReactMethod
    public void init(ReadableMap conf) {
        if (conf.hasKey("host")) host = conf.getString("host");
        else throw new IllegalArgumentException("You must provide valid MQTT Broker host");
        if (conf.hasKey("port")) port = conf.getInt("port");
        else throw new IllegalArgumentException("You must provide valid MQTT Broker port");
        if (conf.hasKey("protocol")) protocol = conf.getString("protocol");
        else throw new IllegalArgumentException("You must provide valid MQTT Broker protocol");
        if (conf.hasKey("tls")) tls = conf.getBoolean("tls");
        if (conf.hasKey("clean")) clean = conf.getBoolean("clean");
        if (conf.hasKey("automaticReconnect"))
            automaticReconnect = conf.getBoolean("automaticReconnect");
        if (conf.hasKey("auth")) auth = conf.getBoolean("auth");
        if (conf.hasKey("clientId")) clientId = conf.getString("clientId");
        if (conf.hasKey("user")) user = conf.getString("user");
        if (conf.hasKey("pass")) pass = conf.getString("pass");
        if (conf.hasKey("qos")) qos = conf.getInt("qos");

        mqttAndroidClient = new MqttAndroidClient(getReactApplicationContext(), protocol + "://" + host + ":" + port, clientId);
        initClientCallback();
    }

    public void initClientCallback() {

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    sendEvent(getReactApplicationContext(), EVENT_RECONNECTED, null);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("error", cause.getMessage());
                sendEvent(getReactApplicationContext(), EVENT_DISCONNECTED, writableMap);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("topic", topic);
                writableMap.putString("message", new String(message.getPayload()));
                sendEvent(getReactApplicationContext(), EVENT_MESSAGE_RECEIVED, writableMap);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    @ReactMethod
    public void connect(final Promise promise) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(automaticReconnect);
        mqttConnectOptions.setCleanSession(clean);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    promise.resolve(asyncActionToken.getClient().getClientId());
                    setupDisconnectedBuffers();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    promise.reject(exception);
                }
            });
        } catch (MqttException e) {
            promise.reject(e);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void subscribeToTopic(String topic) {
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        //promise.resolve(asyncActionToken.getTopics());
                        Toast.makeText(getCurrentActivity(), "Subscribed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        //promise.reject(exception);
                        Toast.makeText(getCurrentActivity(), "Error in subscription", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        } catch (MqttException ex) {
            //promise.reject(ex);
        }
    }

    public void setupDisconnectedBuffers() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
    }

    @ReactMethod
    public void publish(ReadableMap conf) {
        String topic = null, payload = null;
        int qos = 0;
        boolean retain = false;
        byte[] encodedPayload;

        if (conf.hasKey("topic")) topic = conf.getString("topic");
        if (conf.hasKey("payload")) payload = conf.getString("payload");
        if (conf.hasKey("qos")) qos = conf.getInt("qos");
        if (conf.hasKey("retain")) retain = conf.getBoolean("retain");

        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setQos(qos);
            message.setRetained(retain);
            mqttAndroidClient.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "RNMQTT";
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }


    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return super.getConstants();
    }
}
